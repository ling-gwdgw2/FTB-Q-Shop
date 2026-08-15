package com.holysweet.questshop.client;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import java.lang.reflect.Field;

public class InventoryScreenHelper {
    private static Field leftPosField;
    private static Field topPosField;

    static {
        try {
            leftPosField = AbstractContainerScreen.class.getDeclaredField("leftPos");
            leftPosField.setAccessible(true);
            topPosField = AbstractContainerScreen.class.getDeclaredField("topPos");
            topPosField.setAccessible(true);
        } catch (Exception ignored) {
        }
    }

    public static int getLeft(AbstractContainerScreen<?> screen) {
        if (leftPosField != null) {
            try {
                return leftPosField.getInt(screen);
            } catch (Exception ignored) {
            }
        }
        return (screen.width - 176) / 2;
    }

    public static int getTop(AbstractContainerScreen<?> screen) {
        if (topPosField != null) {
            try {
                return topPosField.getInt(screen);
            } catch (Exception ignored) {
            }
        }
        return (screen.height - 166) / 2;
    }
}
