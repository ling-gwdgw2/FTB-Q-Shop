package com.holysweet.questshop.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;

public final class LingShopCommands {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext buildContext) {
        // 1. Primary command: /lingshop
        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal("lingshop")
                .executes(ShopCommands::openShop);

        // /lingshop shop - Opens shop GUI
        root.then(Commands.literal("shop")
                .executes(ShopCommands::openShop));

        // /lingshop balance - Checks Primogem balance
        root.then(Commands.literal("balance")
                .executes(CoinsCommands::showSelf));

        // /lingshop addcoins <player> <amount> (OP Level 2)
        root.then(Commands.literal("addcoins")
                .requires(src -> src.hasPermission(2))
                .then(Commands.argument("player", EntityArgument.player())
                        .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                .executes(CoinsCommands::addCoins))));

        // /lingshop removecoins <player> <amount> (OP Level 2)
        root.then(Commands.literal("removecoins")
                .requires(src -> src.hasPermission(2))
                .then(Commands.argument("player", EntityArgument.player())
                        .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                .executes(CoinsCommands::removeCoins))));

        // /lingshop setcoins <player> <amount> (OP Level 2)
        root.then(Commands.literal("setcoins")
                .requires(src -> src.hasPermission(2))
                .then(Commands.argument("player", EntityArgument.player())
                        .then(Commands.argument("amount", IntegerArgumentType.integer(0))
                                .executes(CoinsCommands::setCoins))));

        // Subtrees for existing coins and categories commands
        root.then(CoinsCommands.subtree());
        root.then(CategoriesCommands.subtree());

        dispatcher.register(root);

        // 2. Direct shorthand command: /shop
        dispatcher.register(Commands.literal("shop").executes(ShopCommands::openShop));
    }
}
