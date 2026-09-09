package com.holysweet.questshop.client.screen;

import com.holysweet.questshop.api.ShopEntry;
import com.holysweet.questshop.client.ClientCategories;
import com.holysweet.questshop.client.ClientCoins;
import com.holysweet.questshop.client.ClientPurchases;
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
    private ShopList parentList;

    public void setParent(ShopList parentList) {
        this.parentList = parentList;
    }

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
        boolean isSoldOut = ClientPurchases.isSoldOut(this.data);
        int effectiveCost = this.data.effectiveCost();
        boolean affordable = ClientCoins.get() >= effectiveCost;
        boolean isSelected = this.parentList != null && this.parentList.getSelected() == this;

        int bgY = top + height;

        if (isSelected) {
            // Selected item: vibrant blue accent with gold border
            guiGraphics.fill(left, top, left + width, bgY, 0x551E88E5);
            int goldColor = 0xFFFFD700;
            guiGraphics.fill(left, top, left + width, top + 1, goldColor);
            guiGraphics.fill(left, bgY - 1, left + width, bgY, goldColor);
            guiGraphics.fill(left, top, left + 1, bgY, goldColor);
            guiGraphics.fill(left + width - 1, top, left + width, bgY, goldColor);
        } else if (isHovered && !isSoldOut) {
            // Hovered item: subtle highlight with bright border
            guiGraphics.fill(left, top, left + width, bgY, 0x33446699);
            int hoverColor = 0xAA64B5F6;
            guiGraphics.fill(left, top, left + width, top + 1, hoverColor);
            guiGraphics.fill(left, bgY - 1, left + width, bgY, hoverColor);
            guiGraphics.fill(left, top, left + 1, bgY, hoverColor);
            guiGraphics.fill(left + width - 1, top, left + width, bgY, hoverColor);
        } else {
            guiGraphics.fill(left, top, left + width, bgY, 0x22000000);
        }

        // 1. Item icon
        int iconX = left + 2;
        int iconY = top + 1;
        guiGraphics.renderItem(this.icon, iconX, iconY);
        guiGraphics.renderItemDecorations(this.mc.font, this.icon, iconX, iconY);

        // 2. Discount Badge
        int currentX = left + 24;
        if (this.data.hasDiscount()) {
            String discountStr = "-" + this.data.discountPercent() + "%";
            int badgeW = this.mc.font.width(discountStr) + 4;
            guiGraphics.fill(currentX, top + 4, currentX + badgeW, top + 14, isSoldOut ? 0xFF555555 : 0xFFCC2222);
            guiGraphics.drawString(this.mc.font, discountStr, currentX + 2, top + 5, 0xFFFFFFFF, false);
            currentX += badgeW + 3;
        }

        // 3. Stock / Daily Limit Badge
        if (this.data.hasDailyLimit()) {
            String limitStr = "1/Day";
            int limitW = this.mc.font.width(limitStr) + 4;
            guiGraphics.fill(currentX, top + 4, currentX + limitW, top + 14, isSoldOut ? 0xFF555555 : 0xFF1E88E5);
            guiGraphics.drawString(this.mc.font, limitStr, currentX + 2, top + 5, 0xFFFFFFFF, false);
            currentX += limitW + 3;
        }

        // 5. Price & Coin Icon OR Sold Out Badge
        int coinX = left + width - 18;
        int costX;
        if (isSoldOut) {
            String soldOutStr = "SOLD OUT";
            int soldW = this.mc.font.width(soldOutStr) + 6;
            costX = left + width - soldW - 4;
            guiGraphics.fill(costX, top + 3, costX + soldW, top + 15, 0xFF444444);
            guiGraphics.drawString(this.mc.font, soldOutStr, costX + 3, top + 6, 0xFFB0B0B0, false);
        } else {
            String costStr = Math.max(1, this.data.amount()) + "x  " + effectiveCost;
            int costColor = !unlocked ? 0xFFB0B0B0 : (affordable ? 0xFF55FF55 : 0xFFFF5555);
            int costWidth = this.mc.font.width(costStr);
            costX = coinX - 4 - costWidth;

            // If discounted, draw original price with strikethrough before discounted price
            if (this.data.hasDiscount()) {
                String origStr = String.valueOf(this.data.cost());
                int origW = this.mc.font.width(origStr);
                int origX = costX - 4 - origW;
                guiGraphics.drawString(this.mc.font, origStr, origX, top + 6, 0xFFAAAAAA, false);
                guiGraphics.fill(origX - 1, top + 10, origX + origW + 1, top + 11, 0xFFFF3333);
                costX = origX;
            }

            guiGraphics.drawString(this.mc.font, costStr, coinX - 4 - costWidth, top + 6, costColor, false);
            guiGraphics.renderItem(this.coinIcon, coinX, top + 1);
        }

        // 4. Item Name (safely truncated if it exceeds available space)
        int textY = top + 6;
        int textColor = isSoldOut ? 0xFF777777 : (unlocked ? 0xFFFFFFFF : 0xFFB0B0B0);
        int maxNameW = Math.max(20, costX - currentX - 8);
        Component displayName = this.name;
        if (this.mc.font.width(displayName) > maxNameW) {
            String raw = displayName.getString();
            while (raw.length() > 3 && this.mc.font.width(raw + "..") > maxNameW) {
                raw = raw.substring(0, raw.length() - 1);
            }
            displayName = Component.literal(raw + "..");
        }
        guiGraphics.drawString(this.mc.font, displayName, currentX, textY, textColor, false);

        // Overlay for locked or sold out or unaffordable
        if (isSoldOut) {
            guiGraphics.fill(left, top, left + width, bgY, 0x88111111);
        } else if (!unlocked) {
            guiGraphics.fill(left, top, left + width, bgY, 0x66000000);
        } else if (!affordable) {
            guiGraphics.fill(left, top, left + width, bgY, 0x1B000000);
        }

        // 6. Hover Tooltip
        if (isHovered && mouseX >= iconX && mouseX <= iconX + 18 && mouseY >= iconY && mouseY <= iconY + 18 && !this.icon.isEmpty()) {
            guiGraphics.renderTooltip(this.mc.font, this.icon, mouseX, mouseY);
        }
    }

    @Override
    public Component getNarration() {
        return Component.literal(this.name.getString());
    }
}
