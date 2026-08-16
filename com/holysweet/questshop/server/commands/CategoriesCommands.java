package com.holysweet.questshop.server.commands;

import com.holysweet.questshop.api.ShopCategory;
import com.holysweet.questshop.server.commands.util.CategoriesCommandUtil;
import com.holysweet.questshop.service.CategoriesService;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.Map;

public final class CategoriesCommands {

    private CategoriesCommands() {}

    public static LiteralArgumentBuilder<CommandSourceStack> subtree() {
        return Commands.literal("categories")
                .then(Commands.literal("list")
                        .executes(ctx -> list(ctx.getSource())))
                .then(buildToggleCommand("unlock", true))
                .then(buildToggleCommand("lock", false));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> buildToggleCommand(String name, boolean unlock) {
        return Commands.literal(name)
                .requires(src -> src.hasPermission(2))
                .then(Commands.argument("player", EntityArgument.player())
                        .then(Commands.argument("category", ResourceLocationArgument.id())
                                .suggests(CategoriesCommandUtil.CATEGORY_SUGGESTIONS)
                                .executes(ctx -> execToggle(ctx, EntityArgument.getPlayer(ctx, "player"), unlock))));
    }

    private static int list(CommandSourceStack src) {
        Map<ResourceLocation, ShopCategory> map = CategoriesService.categories();
        src.sendSuccess(() -> Component.literal("=== Shop Categories (" + map.size() + ") ==="), false);
        for (ShopCategory cat : map.values()) {
            src.sendSuccess(() -> Component.literal("- " + cat.id() + " ('" + cat.display() + "') order=" + cat.order()), false);
        }
        return map.size();
    }

    private static int execToggle(CommandContext<CommandSourceStack> ctx, ServerPlayer player, boolean unlock) {
        ResourceLocation id = CategoriesCommandUtil.coerceToQS(ResourceLocationArgument.getId(ctx, "category"));
        return doSet(ctx.getSource(), player, id, unlock);
    }

    private static int doSet(CommandSourceStack src, ServerPlayer player, ResourceLocation category, boolean unlock) {
        boolean ok = CategoriesService.setUnlocked(player, category, unlock);
        if (ok) {
            src.sendSuccess(() -> Component.literal((unlock ? "Unlocked " : "Locked ") + category + " for " + player.getScoreboardName()), true);
            return 1;
        } else {
            src.sendFailure(Component.literal("Failed to update category " + category));
            return 0;
        }
    }
}
