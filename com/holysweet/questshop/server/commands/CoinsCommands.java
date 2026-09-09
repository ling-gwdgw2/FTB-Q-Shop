package com.holysweet.questshop.server.commands;

import com.holysweet.questshop.service.CoinsService;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public final class CoinsCommands {

    private CoinsCommands() {}

    public static LiteralArgumentBuilder<CommandSourceStack> subtree() {
        return Commands.literal("coins")
                .executes(CoinsCommands::showSelf)
                .then(Commands.literal("show")
                        .then(Commands.argument("player", EntityArgument.player())
                                .requires(src -> src.hasPermission(2))
                                .executes(CoinsCommands::showOther)))
                .then(Commands.literal("add")
                        .requires(src -> src.hasPermission(2))
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                         .executes(CoinsCommands::addCoins))))
                .then(Commands.literal("remove")
                        .requires(src -> src.hasPermission(2))
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                         .executes(CoinsCommands::removeCoins))))
                .then(Commands.literal("set")
                        .requires(src -> src.hasPermission(2))
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.argument("amount", IntegerArgumentType.integer(0))
                                         .executes(CoinsCommands::setCoins))));
    }

    public static int showSelf(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        int coins = CoinsService.get(player.serverLevel(), player);
        ctx.getSource().sendSuccess(() -> Component.translatable("command.ling_q_shop.coins_show", player.getDisplayName(), coins), false);
        return coins;
    }

    public static int showOther(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = EntityArgument.getPlayer(ctx, "player");
        int coins = CoinsService.get(player.serverLevel(), player);
        ctx.getSource().sendSuccess(() -> Component.translatable("command.ling_q_shop.coins_show", player.getDisplayName(), coins), false);
        return coins;
    }

    public static int addCoins(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = EntityArgument.getPlayer(ctx, "player");
        int amount = IntegerArgumentType.getInteger(ctx, "amount");
        int updated = CoinsService.add(player.serverLevel(), player, amount);
        ctx.getSource().sendSuccess(() -> Component.translatable("command.ling_q_shop.coins_added", amount, player.getDisplayName(), updated), true);
        return updated;
    }

    public static int removeCoins(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = EntityArgument.getPlayer(ctx, "player");
        int amount = IntegerArgumentType.getInteger(ctx, "amount");
        int updated = CoinsService.add(player.serverLevel(), player, -amount);
        ctx.getSource().sendSuccess(() -> Component.translatable("command.ling_q_shop.coins_removed", amount, player.getDisplayName(), updated), true);
        return updated;
    }

    public static int setCoins(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = EntityArgument.getPlayer(ctx, "player");
        int amount = IntegerArgumentType.getInteger(ctx, "amount");
        int updated = CoinsService.set(player.serverLevel(), player, amount);
        ctx.getSource().sendSuccess(() -> Component.translatable("command.ling_q_shop.coins_set", player.getDisplayName(), updated), true);
        return updated;
    }
}
