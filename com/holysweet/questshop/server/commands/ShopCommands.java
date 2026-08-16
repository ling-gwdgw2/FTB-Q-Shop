package com.holysweet.questshop.server.commands;

import com.holysweet.questshop.menu.ShopMenu;
import com.holysweet.questshop.network.Net;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;

public final class ShopCommands {

    private ShopCommands() {}

    public static LiteralArgumentBuilder<CommandSourceStack> subtree() {
        return Commands.literal("shop")
                .executes(ShopCommands::openShop);
    }

    private static int openShop(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        Net.sendCategoriesSnapshot(player);
        Net.sendShopData(player);

        player.openMenu(new SimpleMenuProvider(
                (windowId, inv, p) -> new ShopMenu(windowId, inv),
                Component.literal("Quest Shop")
        ));
        return 1;
    }
}
