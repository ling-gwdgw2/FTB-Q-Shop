package com.holysweet.questshop.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;

public final class HqsCommands {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext buildContext) {
        // 1. Primary command: /lingshop
        dispatcher.register(buildShopCommandRoot("lingshop"));

        // 2. Compatibility alias: /hqs
        dispatcher.register(buildShopCommandRoot("hqs"));

        // 3. Shorthand command: /shop
        dispatcher.register(Commands.literal("shop").executes(ShopCommands::openShop));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> buildShopCommandRoot(String rootLiteral) {
        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal(rootLiteral)
                .executes(ShopCommands::openShop);

        // /<root> shop - Opens shop GUI
        root.then(Commands.literal("shop")
                .executes(ShopCommands::openShop));

        // /<root> balance - Checks Primogem balance
        root.then(Commands.literal("balance")
                .executes(CoinsCommands::showSelf));

        // /<root> addcoins <player> <amount> (OP Level 2)
        root.then(Commands.literal("addcoins")
                .requires(src -> src.hasPermission(2))
                .then(Commands.argument("player", EntityArgument.player())
                        .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                .executes(CoinsCommands::addCoins))));

        // /<root> removecoins <player> <amount> (OP Level 2)
        root.then(Commands.literal("removecoins")
                .requires(src -> src.hasPermission(2))
                .then(Commands.argument("player", EntityArgument.player())
                        .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                .executes(CoinsCommands::removeCoins))));

        // /<root> setcoins <player> <amount> (OP Level 2)
        root.then(Commands.literal("setcoins")
                .requires(src -> src.hasPermission(2))
                .then(Commands.argument("player", EntityArgument.player())
                        .then(Commands.argument("amount", IntegerArgumentType.integer(0))
                                .executes(CoinsCommands::setCoins))));

        // Subtrees for existing coins and categories commands
        root.then(CoinsCommands.subtree());
        root.then(CategoriesCommands.subtree());

        return root;
    }
}
