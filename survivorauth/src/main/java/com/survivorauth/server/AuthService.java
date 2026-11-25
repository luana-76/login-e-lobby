package com.survivorauth.server;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;
import com.survivorauth.config.ServerConfig;
import com.survivorauth.net.AuthState;
import com.survivorauth.net.AuthStatePayload;
import com.survivorauth.net.Team;
import com.survivorauth.net.TeamCountsPayload;

import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.neoforge.network.PacketDistributor;

/** Núcleo do login: quem está "pendente", registro, entrada e troca de senha. Tudo roda na thread do servidor. */
public final class AuthService {
    private static final Logger LOGGER = LogUtils.getLogger();

    private static final Map<UUID, Pending> PENDING = new ConcurrentHashMap<>();
    private static AccountStore store;
    private static ExecutorService executor;

    private static final class Pending {
        final double x, y, z;
        long deadlineTick;
        long nextPromptTick;
        boolean teamPhase; // já se identificou, falta escolher o time
        int prompts;
        boolean busy;

        Pending(ServerPlayer p, long deadlineTick, long nextPromptTick) {
            this.x = p.getX();
            this.y = p.getY();
            this.z = p.getZ();
            this.deadlineTick = deadlineTick;
            this.nextPromptTick = nextPromptTick;
        }
    }

    // ------------------------------------------------------------------ ciclo de vida

    public static void onServerStarting(MinecraftServer server) {
        store = new AccountStore(server.getWorldPath(LevelResource.ROOT).resolve("survivorauth").resolve("accounts.json"));
        executor = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "survivorauth-hash");
            t.setDaemon(true);
            return t;
        });
        PENDING.clear();
        LoginAttempts.clear();
    }

    public static void onServerStopped() {
        if (executor != null) executor.shutdownNow();
        executor = null;
        store = null;
        PENDING.clear();
    }

    public static boolean isPending(Player player) {
        return PENDING.containsKey(player.getUUID());
    }

    private static boolean needsLogin(MinecraftServer server) {
        return ServerConfig.REQUIRE_LOGIN.get() == ServerConfig.RequireLogin.ALWAYS || !server.usesAuthentication();
    }

    public static void onJoin(ServerPlayer player) {
        MinecraftServer server = player.getServer();
        if (server == null || store == null || !needsLogin(server)) return;

        long locked = LoginAttempts.lockedMinutes(ipOf(player));
        if (locked > 0) {
            player.connection.disconnect(Msg.lockedOut(locked));
            return;
        }
        beginPending(player);
    }

    public static void onLeave(UUID id) {
        PENDING.remove(id);
    }

    private static void beginPending(ServerPlayer player) {
        MinecraftServer server = player.getServer();
        long now = server.getTickCount();
        long deadline = now + ServerConfig.LOGIN_TIMEOUT_SECONDS.get() * 20L;
        PENDING.put(player.getUUID(), new Pending(player, deadline, now + 10));
    }

    // ------------------------------------------------------------------ tick

    public static void onTick(MinecraftServer server) {
        if (PENDING.isEmpty()) return;
        long now = server.getTickCount();

        for (Map.Entry<UUID, Pending> entry : new ArrayList<>(PENDING.entrySet())) {
            ServerPlayer player = server.getPlayerList().getPlayer(entry.getKey());
            if (player == null) {
                PENDING.remove(entry.getKey());
                continue;
            }
            Pending pending = entry.getValue();

            if (now >= pending.deadlineTick) {
                player.connection.disconnect(Msg.timeout());
                continue;
            }

            // congela o jogador onde ele entrou (pode olhar em volta, mas não andar)
            if (player.distanceToSqr(pending.x, pending.y, pending.z) > 0.01D) {
                player.connection.teleport(pending.x, pending.y, pending.z, player.getYRot(), player.getXRot());
            }
            player.fallDistance = 0;

            if (now >= pending.nextPromptTick) {
                sendPrompt(player, pending, now);
                pending.nextPromptTick = now + 200; // repete a cada 10s (cobre pacote perdido)
            }
        }
    }

    private static void sendTeamCounts(ServerPlayer player) {
        if (store == null) return;
        Map<String, Integer> counts = store.teamCounts();
        PacketDistributor.sendToPlayer(player, new TeamCountsPayload(
                counts.getOrDefault("leste", 0), counts.getOrDefault("oeste", 0),
                counts.getOrDefault("norte", 0), counts.getOrDefault("sul", 0)));
    }

    private static void sendPrompt(ServerPlayer player, Pending pending, long now) {
        boolean registered = store.has(player.getGameProfile().getName());
        int secondsLeft = (int) Math.max(0, (pending.deadlineTick - now) / 20);

        if (pending.teamPhase) {
            if (hasMod(player)) {
                sendTeamCounts(player);
                PacketDistributor.sendToPlayer(player, new AuthStatePayload(AuthState.PROMPT_TEAM, 0, secondsLeft));
            } else {
                player.displayClientMessage(Msg.teamHint(), true);
                if (pending.prompts == 0) player.sendSystemMessage(Msg.teamHint());
            }
            pending.prompts++;
            return;
        }

        if (hasMod(player)) {
            PacketDistributor.sendToPlayer(player, new AuthStatePayload(
                    registered ? AuthState.PROMPT_LOGIN : AuthState.PROMPT_REGISTER, 0, secondsLeft));
        } else {
            player.displayClientMessage(Msg.hint(registered), true);
            if (pending.prompts == 0) player.sendSystemMessage(Msg.hint(registered));
        }
        pending.prompts++;
    }

    // ------------------------------------------------------------------ ações

    public static void login(ServerPlayer player, String password) {
        Pending pending = PENDING.get(player.getUUID());
        if (pending == null) {
            player.sendSystemMessage(Msg.alreadyLoggedIn());
            return;
        }
        if (pending.teamPhase) {
            send(player, AuthState.PROMPT_TEAM, 0, Msg.teamFirst());
            return;
        }
        if (pending.busy) {
            send(player, AuthState.BUSY, 0, Msg.busy());
            return;
        }
        AccountStore.Account account = store.get(player.getGameProfile().getName());
        if (account == null) {
            send(player, AuthState.PROMPT_REGISTER, 0, Msg.notRegistered());
            return;
        }

        pending.busy = true;
        MinecraftServer server = player.getServer();
        UUID id = player.getUUID();
        submit(server, () -> Passwords.verify(password, account), ok -> finishLogin(server, id, ok));
    }

    private static void finishLogin(MinecraftServer server, UUID id, Boolean ok) {
        ServerPlayer player = server.getPlayerList().getPlayer(id);
        Pending pending = PENDING.get(id);
        if (player == null || pending == null) return;
        pending.busy = false;

        if (Boolean.TRUE.equals(ok)) {
            LoginAttempts.reset(ipOf(player));
            completeAuth(server, player, pending, Msg.welcome(player.getGameProfile().getName()));
            return;
        }

        int left = LoginAttempts.fail(ipOf(player));
        if (left <= 0) {
            player.connection.disconnect(Msg.tooManyAttempts(ServerConfig.LOCKOUT_MINUTES.get()));
            return;
        }
        send(player, AuthState.WRONG_PASSWORD, left, Msg.wrongPassword(left));
    }

    public static void register(ServerPlayer player, String password, String confirm) {
        Pending pending = PENDING.get(player.getUUID());
        if (pending == null) {
            player.sendSystemMessage(Msg.alreadyLoggedIn());
            return;
        }
        if (pending.busy) {
            send(player, AuthState.BUSY, 0, Msg.busy());
            return;
        }
        if (pending.teamPhase) {
            send(player, AuthState.PROMPT_TEAM, 0, Msg.teamFirst());
            return;
        }
        String name = player.getGameProfile().getName();
        if (store.has(name)) {
            send(player, AuthState.PROMPT_LOGIN, 0, Msg.alreadyRegistered());
            return;
        }
        int min = ServerConfig.MIN_PASSWORD_LENGTH.get();
        if (password.length() < min) {
            send(player, AuthState.TOO_SHORT, min, Msg.tooShort(min));
            return;
        }
        if (!password.equals(confirm)) {
            send(player, AuthState.MISMATCH, 0, Msg.mismatch());
            return;
        }

        pending.busy = true;
        int iterations = ServerConfig.HASH_ITERATIONS.get();
        MinecraftServer server = player.getServer();
        UUID id = player.getUUID();
        submit(server, () -> Passwords.create(password, iterations), account -> finishRegister(server, id, name, account));
    }

    private static void finishRegister(MinecraftServer server, UUID id, String name, AccountStore.Account account) {
        ServerPlayer player = server.getPlayerList().getPlayer(id);
        Pending pending = PENDING.get(id);
        if (player == null || pending == null) return;
        pending.busy = false;

        if (account == null) {
            send(player, AuthState.BUSY, 0, Msg.internalError());
            return;
        }
        store.put(name, account);
        completeAuth(server, player, pending, Msg.registered());
    }

    // ------------------------------------------------------------------ time

    /**
     * Fim do login/registro. Se a conta ainda não tem time, o jogador continua "pendente"
     * (congelado) na fase de escolha do time; só depois de escolher ele cai no lobby.
     */
    private static void completeAuth(MinecraftServer server, ServerPlayer player, Pending pending, Component doneMsg) {
        AccountStore.Account account = store.get(player.getGameProfile().getName());
        Team team = account == null ? null : Team.byId(account.team());

        if (team == null) {
            long now = server.getTickCount();
            pending.teamPhase = true;
            pending.deadlineTick = now + ServerConfig.LOGIN_TIMEOUT_SECONDS.get() * 20L; // prazo novo p/ escolher
            pending.nextPromptTick = now + 200;
            send(player, AuthState.PROMPT_TEAM, 0, Msg.teamHint());
            return;
        }

        PENDING.remove(player.getUUID());
        send(player, AuthState.SUCCESS, team.wire(), null);
        player.sendSystemMessage(doneMsg);
    }

    /**
     * Escolha do time permitida exclusivamente durante a etapa de cadastro/login.
     * Depois que a autenticação termina, qualquer nova tentativa é recusada.
     * O time salvo na conta nunca é substituído por uma escolha enviada pelo cliente.
     */
    public static void chooseTeam(ServerPlayer player, String teamId) {
        if (store == null) return;
        String name = player.getGameProfile().getName();
        AccountStore.Account account = store.get(name);
        Pending pending = PENDING.get(player.getUUID());

        // A seleção só é aceita enquanto o servidor estiver aguardando a escolha
        // no fluxo de autenticação. Abrir/tentar usar a tela depois do login não funciona.
        if (pending == null || !pending.teamPhase) {
            Team savedTeam = account == null ? null : Team.byId(account.team());
            if (savedTeam != null) {
                player.sendSystemMessage(Msg.teamAlready(label(savedTeam)));
            } else {
                player.sendSystemMessage(Msg.notLoggedIn());
            }
            return;
        }

        // Mesmo durante uma tentativa de pacote repetido, nunca sobrescreve um time salvo.
        if (account != null && Team.byId(account.team()) != null) {
            player.sendSystemMessage(Msg.teamAlready(label(Team.byId(account.team()))));
            return;
        }
        if (account == null) {
            player.sendSystemMessage(Msg.notLoggedIn());
            return;
        }

        Team team = Team.byId(teamId);
        if (team == null) {
            send(player, AuthState.PROMPT_TEAM, 0, Msg.teamInvalid());
            return;
        }

        store.put(name, account.withTeam(team.id));
        PENDING.remove(player.getUUID());
        send(player, AuthState.SUCCESS, team.wire(), null);
        player.sendSystemMessage(Msg.teamChosen(name, label(team)));
        LOGGER.info("[SurvivorAuth] {} escolheu o time {}", name, team.id);
    }

    /** Time atual do jogador (null se ainda não escolheu). */
    public static Team teamOf(ServerPlayer player) {
        if (store == null) return null;
        AccountStore.Account account = store.get(player.getGameProfile().getName());
        return account == null ? null : Team.byId(account.team());
    }

    public static String label(Team team) {
        return team.id.toUpperCase(Locale.ROOT);
    }

    /** Admin: apaga o time da conta para o jogador poder escolher de novo. Retorna false se não há conta. */
    public static boolean resetTeam(MinecraftServer server, String name) {
        if (store == null) return false;
        AccountStore.Account account = store.get(name);
        if (account == null) return false;
        store.put(name, account.withTeam(null));
        ServerPlayer online = server.getPlayerList().getPlayerByName(name);
        if (online != null && !isPending(online)) {
            beginPending(online);
            PENDING.get(online.getUUID()).teamPhase = true; // pula o login, vai direto pra escolha
        }
        return true;
    }

    public static void changePassword(ServerPlayer player, String oldPassword, String newPassword) {
        if (isPending(player)) {
            player.sendSystemMessage(Msg.notLoggedIn());
            return;
        }
        String name = player.getGameProfile().getName();
        AccountStore.Account account = store == null ? null : store.get(name);
        if (account == null) {
            player.sendSystemMessage(Msg.notRegistered());
            return;
        }
        int min = ServerConfig.MIN_PASSWORD_LENGTH.get();
        if (newPassword.length() < min) {
            player.sendSystemMessage(Msg.tooShort(min));
            return;
        }
        int iterations = ServerConfig.HASH_ITERATIONS.get();
        MinecraftServer server = player.getServer();
        UUID id = player.getUUID();
        submit(server,
                () -> Passwords.verify(oldPassword, account)
                        ? Passwords.create(newPassword, iterations).withTeam(account.team())
                        : null,
                updated -> {
                    ServerPlayer p = server.getPlayerList().getPlayer(id);
                    if (p == null) return;
                    if (updated == null) {
                        p.sendSystemMessage(Msg.wrongCurrent());
                    } else {
                        store.put(name, updated);
                        p.sendSystemMessage(Msg.passwordChanged());
                    }
                });
    }

    // ------------------------------------------------------------------ admin

    /** Remove a conta. Se o jogador estiver online, ele volta a ser pedido para registrar. Retorna true se existia. */
    public static boolean unregister(MinecraftServer server, String name) {
        if (store == null) return false;
        boolean removed = store.remove(name);
        ServerPlayer online = server.getPlayerList().getPlayerByName(name);
        if (removed && online != null && !isPending(online)) beginPending(online);
        return removed;
    }

    /** Obriga um jogador online a se identificar de novo, sem expulsar. */
    public static void forceLogout(ServerPlayer player) {
        if (store != null && !isPending(player)) beginPending(player);
    }

    // ------------------------------------------------------------------ utilitários

    private static boolean hasMod(ServerPlayer player) {
        return player.connection.hasChannel(AuthStatePayload.TYPE);
    }

    private static int secondsLeft(ServerPlayer player) {
        Pending pending = PENDING.get(player.getUUID());
        if (pending == null) return 0;
        return (int) Math.max(0, (pending.deadlineTick - player.getServer().getTickCount()) / 20);
    }

    /** Cliente com o mod recebe o estado (a tela mostra a mensagem); cliente sem o mod recebe o chat. */
    private static void send(ServerPlayer player, int state, int arg, Component chat) {
        if (hasMod(player)) {
            PacketDistributor.sendToPlayer(player, new AuthStatePayload(state, arg, secondsLeft(player)));
        } else if (chat != null) {
            player.sendSystemMessage(chat);
        }
    }

    /** Roda o trabalho pesado (hash) fora da thread do servidor e devolve o resultado nela. */
    private static <T> void submit(MinecraftServer server, Supplier<T> work, Consumer<T> done) {
        if (executor == null) {
            done.accept(null);
            return;
        }
        executor.execute(() -> {
            T result = null;
            try {
                result = work.get();
            } catch (Exception e) {
                LOGGER.error("[SurvivorAuth] Erro ao processar senha", e);
            }
            final T finalResult = result;
            server.execute(() -> done.accept(finalResult));
        });
    }

    private static String ipOf(ServerPlayer player) {
        SocketAddress address = player.connection.getRemoteAddress();
        if (address instanceof InetSocketAddress inet && inet.getAddress() != null) {
            return inet.getAddress().getHostAddress().toLowerCase(Locale.ROOT);
        }
        return String.valueOf(address);
    }

    private AuthService() {}
}
