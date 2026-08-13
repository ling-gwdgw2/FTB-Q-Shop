package com.holysweet.questshop.client.screen;

import com.holysweet.questshop.api.ShopEntry;
import com.holysweet.questshop.client.ClientCategories;
import com.holysweet.questshop.client.ClientCoins;
import com.holysweet.questshop.item.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

public class ShopListEntry extends ObjectSelectionList.Entry<ShopListEntry> {

    private final Minecraft mc;
    public final ShopEntry data;
    private final ItemStack icon;
    private final ItemStack coinIcon;
    private final Component name;

    public ShopListEntry(ShopEntry data) {
        this.mc = Minecraft.getInstance();
        this.data = data;
        this.coinIcon = new ItemStack(ModItems.COIN.get());

        Optional<Item> itemOpt = BuiltInRegistries.ITEM.getOptional(data.itemId());
        if (itemOpt.isPresent()) {
            Item item = itemOpt.get();
            this.icon = new ItemStack(item, data.amount());
            this.name = item.getName(this.icon);
        } else {
            this.icon = ItemStack.EMPTY;
            this.name = Component.literal(data.itemId().toString());
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean isHovered, float partialTick) {
        boolean unlocked = ClientCategories.isUnlocked(this.data.category());
        boolean affordable = ClientCoins.get() >= this.data.cost();

        int bgY = top + height;
        int bgColor = isHovered ? 0x34000000 : 0x22000000;
        guiGraphics.fill(left, top, left + width, bgY, bgColor);

        // 1. Item icon
        int iconX = left + 2;
        int iconY = top + 1;
        guiGraphics.renderItem(this.icon, iconX, iconY);
        guiGraphics.renderItemDecorations(this.mc.font, this.icon, iconX, iconY);

        // 2. Item Name
        int textY = top + 6;
        int textColor = unlocked ? 0xFFFFFFFF : 0xFFB0B0B0;
        guiGraphics.drawString(this.mc.font, this.name, left + 24, textY, textColor, false);

        // 3. Price & Coin Icon
        String costStr = Math.max(1, this.data.amount()) + "x  " + this.data.cost();
        int costColor = !unlocked ? 0xFFB0B0B0 : (affordable ? 0xFF55FF55 : 0xFFFF5555);
        int costWidth = this.mc.font.width(costStr);
        int coinX = left + width - 18;
        int costX = coinX - 4 - costWidth;

        guiGraphics.drawString(this.mc.font, costStr, costX, textY, costColor, false);
        guiGraphics.renderItem(this.coinIcon, coinX, top + 1);

        if (!unlocked) {
            guiGraphics.fill(left, top, left + width, bgY, 0x66000000);
        } else if (!affordable) {
            guiGraphics.fill(left, top, left + width, bgY, 0x1B000000);
        }

        // 4. Hover Tooltip
        if (isHovered && mouseX >= iconX && mouseX <= iconX + 18 && mouseY >= iconY && mouseY <= iconY + 18 && !this.icon.isEmpty()) {
            guiGraphics.renderTooltip(this.mc.font, this.icon, mouseX, mouseY);
        }
    }

    @Override
    public Component getNarration() {
        return Component.literal(this.name.getString());
    }
}
