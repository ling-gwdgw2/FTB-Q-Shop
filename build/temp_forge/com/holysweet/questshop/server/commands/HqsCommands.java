package com.holysweet.questshop.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

import java.lang.reflect.Method;

public final class HqsCommands {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext buildContext) {
        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal("hqs");
        root.then(CoinsCommands.subtree());
        root.then(CategoriesCommands.subtree());
        root.then(ShopCommands.subtree());
        dispatcher.register(root);

        // Register /shop as a top-level command alias
        try {
            Method openCmdMethod = ShopCommands.class.getDeclaredMethod("openCommand", com.mojang.brigadier.context.CommandContext.class);
            openCmdMethod.setAccessible(true);
            
            LiteralArgumentBuilder<CommandSourceStack> shopAlias = Commands.literal("shop");
            shopAlias.executes(ctx -> {
                try {
                    return (Integer) openCmdMethod.invoke(null, ctx);
                } catch (Exception e) {
                    e.printStackTrace();
                    return 0;
                }
            });
            dispatcher.register(shopAlias);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
