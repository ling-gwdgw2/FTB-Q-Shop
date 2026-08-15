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

        int bgY = top + height;
        int bgColor = isHovered ? 0x45000000 : 0x22000000;
        guiGraphics.fill(left, top, left + width, bgY, bgColor);

        // Gold Highlight Frame on Hover
        if (isHovered && !isSoldOut) {
            int goldColor = 0xFFFFD700;
            guiGraphics.fill(left, top, left + width, top + 1, goldColor);
            guiGraphics.fill(left, bgY - 1, left + width, bgY, goldColor);
            guiGraphics.fill(left, top, left + 1, bgY, goldColor);
            guiGraphics.fill(left + width - 1, top, left + width, bgY, goldColor);
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

        // 4. Item Name
        int textY = top + 6;
        int textColor = isSoldOut ? 0xFF777777 : (unlocked ? 0xFFFFFFFF : 0xFFB0B0B0);
        guiGraphics.drawString(this.mc.font, this.name, currentX, textY, textColor, false);

        // 5. Price & Coin Icon OR Sold Out Badge
        int coinX = left + width - 18;
        if (isSoldOut) {
            String soldOutStr = "SOLD OUT";
            int soldW = this.mc.font.width(soldOutStr) + 6;
            int soldX = left + width - soldW - 4;
            guiGraphics.fill(soldX, top + 3, soldX + soldW, top + 15, 0xFF444444);
            guiGraphics.drawString(this.mc.font, soldOutStr, soldX + 3, textY, 0xFFB0B0B0, false);
        } else {
            String costStr = Math.max(1, this.data.amount()) + "x  " + effectiveCost;
            int costColor = !unlocked ? 0xFFB0B0B0 : (affordable ? 0xFF55FF55 : 0xFFFF5555);
            int costWidth = this.mc.font.width(costStr);
            int costX = coinX - 4 - costWidth;

            // If discounted, draw original price with strikethrough before discounted price
            if (this.data.hasDiscount()) {
                String origStr = String.valueOf(this.data.cost());
                int origW = this.mc.font.width(origStr);
                int origX = costX - 4 - origW;
                guiGraphics.drawString(this.mc.font, origStr, origX, textY, 0xFFAAAAAA, false);
                guiGraphics.fill(origX - 1, textY + 4, origX + origW + 1, textY + 5, 0xFFFF3333);
            }

            guiGraphics.drawString(this.mc.font, costStr, costX, textY, costColor, false);
            guiGraphics.renderItem(this.coinIcon, coinX, top + 1);
        }

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
