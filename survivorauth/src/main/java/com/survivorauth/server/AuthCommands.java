package com.survivorauth.server;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public final class AuthCommands {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        // /register <senha> <senha>
        dispatcher.register(Commands.literal("register")
                .then(Commands.argument("password", StringArgumentType.word())
                        .then(Commands.argument("confirm", StringArgumentType.word())
                                .executes(ctx -> {
                                    ServerPlayer p = ctx.getSource().getPlayerOrException();
                                    AuthService.register(p,
                                            StringArgumentType.getString(ctx, "password"),
                                            StringArgumentType.getString(ctx, "confirm"));
                                    return 1;
                                }))));

        // /login <senha>
        dispatcher.register(Commands.literal("login")
                .then(Commands.argument("password", StringArgumentType.word())
                        .executes(ctx -> {
                            ServerPlayer p = ctx.getSource().getPlayerOrException();
                            AuthService.login(p, StringArgumentType.getString(ctx, "password"));
                            return 1;
                        })));

        // /changepassword <atual> <nova>
        dispatcher.register(Commands.literal("changepassword")
                .then(Commands.argument("current", StringArgumentType.word())
                        .then(Commands.argument("new", StringArgumentType.word())
                                .executes(ctx -> {
                                    ServerPlayer p = ctx.getSource().getPlayerOrException();
                                    AuthService.changePassword(p,
                                            StringArgumentType.getString(ctx, "current"),
                                            StringArgumentType.getString(ctx, "new"));
                                    return 1;
                                }))));

        // Admin: /sauth unregister <nome> | /sauth logout <jogador>
        dispatcher.register(Commands.literal("sauth")
                .requires(src -> src.hasPermission(3))
                .then(Commands.literal("unregister")
                        .then(Commands.argument("name", StringArgumentType.word())
                                .executes(ctx -> {
                                    String name = StringArgumentType.getString(ctx, "name");
                                    boolean removed = AuthService.unregister(ctx.getSource().getServer(), name);
                                    ctx.getSource().sendSuccess(() -> Component.literal(removed
                                            ? "Conta de " + name + " removida."
                                            : "Nenhuma conta encontrada para " + name + "."), true);
                                    return removed ? 1 : 0;
                                })))
                .then(Commands.literal("logout")
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(ctx -> {
                                    ServerPlayer target = EntityArgument.getPlayer(ctx, "player");
                                    AuthService.forceLogout(target);
                                    ctx.getSource().sendSuccess(() -> Component.literal(
                                            target.getGameProfile().getName() + " precisa se identificar de novo."), true);
                                    return 1;
                                }))));
    }

    private AuthCommands() {}
}
