package com.holysweet.questshop.client.toast;

import com.holysweet.questshop.Config;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;

public final class PurchaseToast implements Toast {
    private static final ResourceLocation BACKGROUND_SPRITE = ResourceLocation.withDefaultNamespace("toast/system");
    private final ItemStack icon;
    private final Component title;
    private final Component desc;
    private long startTime;
    private boolean playedSound;

    public PurchaseToast(ItemStack icon, Component title, Component desc) {
        this.icon = icon;
        this.title = title;
        this.desc = desc;
    }

    @Override
    public Visibility render(GuiGraphics guiGraphics, ToastComponent toastComponent, long timeSinceLastVisible) {
        if (this.startTime == 0L) {
            this.startTime = timeSinceLastVisible;
        }

        guiGraphics.blitSprite(BACKGROUND_SPRITE, 0, 0, this.width(), this.height());

        if (this.title != null) {
            guiGraphics.drawString(toastComponent.getMinecraft().font, this.title, 30, 7, 0xFFFF00, false);
        }
        if (this.desc != null) {
            guiGraphics.drawString(toastComponent.getMinecraft().font, this.desc, 30, 18, 0xFFFFFF, false);
        }

        if (!this.playedSound && timeSinceLastVisible > 0L) {
            this.playedSound = true;
            if (Config.ENABLE_SOUND_EFFECTS.get() && toastComponent.getMinecraft().player != null) {
                toastComponent.getMinecraft().player.playSound(SoundEvents.UI_TOAST_IN, 1.0F, 1.0F);
            }
        }

        guiGraphics.renderItem(this.icon, 8, 8);

        return (timeSinceLastVisible - this.startTime) >= 5000L ? Visibility.HIDE : Visibility.SHOW;
    }

    @Override
    public int width() {
        Minecraft mc = Minecraft.getInstance();
        int titleW = this.title != null ? mc.font.width(this.title) : 0;
        int descW = this.desc != null ? mc.font.width(this.desc) : 0;
        return Math.max(160, 34 + Math.max(titleW, descW) + 12);
    }

    @Override
    public int height() {
        return 32;
    }
}
