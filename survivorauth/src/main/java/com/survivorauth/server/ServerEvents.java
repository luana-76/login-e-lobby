package com.survivorauth.server;

import java.util.Locale;

import com.survivorauth.SurvivorAuth;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.CommandEvent;
import net.neoforged.neoforge.event.ServerChatEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.item.ItemTossEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/** Liga o AuthService aos eventos do jogo e bloqueia tudo que um jogador "pendente" não pode fazer. */
@EventBusSubscriber(modid = SurvivorAuth.MODID)
public final class ServerEvents {

    // ------------------------------------------------------------------ ciclo de vida

    @SubscribeEvent
    public static void onServerStarting(ServerStartingEvent event) {
        AuthService.onServerStarting(event.getServer());
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        AuthService.onServerStopped();
    }

    @SubscribeEvent
    public static void onJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) AuthService.onJoin(player);
    }

    @SubscribeEvent
    public static void onLeave(PlayerEvent.PlayerLoggedOutEvent event) {
        AuthService.onLeave(event.getEntity().getUUID());
    }

    @SubscribeEvent
    public static void onTick(ServerTickEvent.Post event) {
        AuthService.onTick(event.getServer());
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        AuthCommands.register(event.getDispatcher());
    }

    // ------------------------------------------------------------------ restrições

    private static boolean locked(Entity entity) {
        return entity instanceof Player player && AuthService.isPending(player);
    }

    @SubscribeEvent
    public static void onChat(ServerChatEvent event) {
        if (AuthService.isPending(event.getPlayer())) {
            event.setCanceled(true);
            event.getPlayer().sendSystemMessage(Msg.chatBlocked());
        }
    }

    /** Só /login e /register passam enquanto o jogador não se identificou. */
    @SubscribeEvent
    public static void onCommand(CommandEvent event) {
        ServerPlayer player = event.getParseResults().getContext().getSource().getPlayer();
        if (player == null || !AuthService.isPending(player)) return;

        String input = event.getParseResults().getReader().getString().trim();
        if (input.startsWith("/")) input = input.substring(1);
        String first = input.split("\\s+")[0].toLowerCase(Locale.ROOT);

        if (!first.equals("login") && !first.equals("register")) {
            event.setCanceled(true);
            player.sendSystemMessage(Msg.chatBlocked());
        }
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (locked(event.getEntity())) event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        if (locked(event.getEntity())) event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        if (locked(event.getEntity())) event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (locked(event.getEntity())) event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onAttack(AttackEntityEvent event) {
        if (locked(event.getEntity())) event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onBreak(BlockEvent.BreakEvent event) {
        if (locked(event.getPlayer())) event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onPlace(BlockEvent.EntityPlaceEvent event) {
        if (locked(event.getEntity())) event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onToss(ItemTossEvent event) {
        if (locked(event.getPlayer())) event.setCanceled(true);
    }

    /** Ninguém toma dano enquanto está na tela de login. */
    @SubscribeEvent
    public static void onDamage(LivingIncomingDamageEvent event) {
        if (locked(event.getEntity())) event.setCanceled(true);
    }

    private ServerEvents() {}
}
