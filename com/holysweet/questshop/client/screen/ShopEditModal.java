package com.holysweet.questshop.client.screen;

import com.holysweet.questshop.api.ShopEntry;
import com.holysweet.questshop.network.payload.AdminRemoveEntryPayload;
import com.holysweet.questshop.network.payload.AdminUpdateEntryPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.Optional;

public class ShopEditModal {

    private final ShopMenuScreen parent;
    private final ShopEntry entry;
    private final ItemStack iconStack;
    private final Component itemTitle;

    private EditBox costBox;
    private EditBox amountBox;

    private Button saveBtn;
    private Button deleteBtn;
    private Button cancelBtn;

    private Button p10Btn, p50Btn, p100Btn, p500Btn, p1000Btn;

    public ShopEditModal(ShopMenuScreen parent, ShopEntry entry) {
        this.parent = parent;
        this.entry = entry;

        Optional<Item> itemOpt = BuiltInRegistries.ITEM.getOptional(entry.itemId());
        if (itemOpt.isPresent()) {
            this.iconStack = new ItemStack(itemOpt.get(), entry.amount());
            this.itemTitle = itemOpt.get().getName(this.iconStack);
        } else {
            this.iconStack = ItemStack.EMPTY;
            this.itemTitle = Component.literal(entry.itemId().toString());
        }
    }

    public void init(int leftPos, int topPos) {
        int modalWidth = 200;
        int modalHeight = 130;
        int modalX = leftPos + (280 - modalWidth) / 2;
        int modalY = topPos + (230 - modalHeight) / 2;

        // Cost & Amount inputs
        this.costBox = new EditBox(Minecraft.getInstance().font, modalX + 50, modalY + 30, 50, 16, Component.literal("Cost"));
        this.costBox.setValue(String.valueOf(entry.cost()));

        this.amountBox = new EditBox(Minecraft.getInstance().font, modalX + 140, modalY + 30, 40, 16, Component.literal("Amount"));
        this.amountBox.setValue(String.valueOf(entry.amount()));

        // Preset price buttons: [10] [50] [100] [500] [1k]
        int presetY = modalY + 54;
        this.p10Btn = Button.builder(Component.literal("10"), b -> setCost(10)).bounds(modalX + 15, presetY, 30, 16).build();
        this.p50Btn = Button.builder(Component.literal("50"), b -> setCost(50)).bounds(modalX + 48, presetY, 30, 16).build();
        this.p100Btn = Button.builder(Component.literal("100"), b -> setCost(100)).bounds(modalX + 81, presetY, 34, 16).build();
        this.p500Btn = Button.builder(Component.literal("500"), b -> setCost(500)).bounds(modalX + 118, presetY, 34, 16).build();
        this.p1000Btn = Button.builder(Component.literal("1k"), b -> setCost(1000)).bounds(modalX + 155, presetY, 30, 16).build();

        // Action Buttons: [Save] [Delete] [Cancel]
        int actionY = modalY + 95;
        this.saveBtn = Button.builder(Component.literal("Save"), b -> onSave()).bounds(modalX + 15, actionY, 55, 18).build();
        this.deleteBtn = Button.builder(Component.literal("Delete"), b -> onDelete()).bounds(modalX + 73, actionY, 55, 18).build();
        this.cancelBtn = Button.builder(Component.literal("Cancel"), b -> parent.closeModal()).bounds(modalX + 131, actionY, 55, 18).build();
    }

    private void setCost(int cost) {
        if (this.costBox != null) {
            this.costBox.setValue(String.valueOf(cost));
        }
    }

    private void onSave() {
        try {
            int newCost = Math.max(0, Integer.parseInt(costBox.getValue().trim()));
            int newAmount = Math.max(1, Integer.parseInt(amountBox.getValue().trim()));
            PacketDistributor.sendToServer(new AdminUpdateEntryPayload(entry.itemId(), newAmount, newCost, entry.category()));
        } catch (NumberFormatException ignored) {}
        parent.closeModal();
    }

    private void onDelete() {
        PacketDistributor.sendToServer(new AdminRemoveEntryPayload(entry.itemId(), entry.category()));
        parent.closeModal();
    }

    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, int leftPos, int topPos) {
        int modalWidth = 200;
        int modalHeight = 130;
        int modalX = leftPos + (280 - modalWidth) / 2;
        int modalY = topPos + (230 - modalHeight) / 2;

        // Dark background overlay
        guiGraphics.fill(leftPos - 110, topPos, leftPos + 280, topPos + 230, 0xAA000000);

        // Modal box background
        guiGraphics.fill(modalX, modalY, modalX + modalWidth, modalY + modalHeight, 0xF0181824);
        guiGraphics.fill(modalX + 1, modalY + 1, modalX + modalWidth - 1, modalY + modalHeight - 1, 0xF0222234);

        // Header: Icon + Title
        guiGraphics.renderItem(iconStack, modalX + 10, modalY + 8);
        guiGraphics.drawString(Minecraft.getInstance().font, itemTitle, modalX + 32, modalY + 12, 0xFFFFFF, false);

        // Field labels
        guiGraphics.drawString(Minecraft.getInstance().font, "Cost:", modalX + 15, modalY + 34, 0xFFD700, false);
        guiGraphics.drawString(Minecraft.getInstance().font, "Qty:", modalX + 115, modalY + 34, 0xAAAAAA, false);

        // Render widgets
        if (costBox != null) costBox.render(guiGraphics, mouseX, mouseY, partialTick);
        if (amountBox != null) amountBox.render(guiGraphics, mouseX, mouseY, partialTick);

        if (p10Btn != null) p10Btn.render(guiGraphics, mouseX, mouseY, partialTick);
        if (p50Btn != null) p50Btn.render(guiGraphics, mouseX, mouseY, partialTick);
        if (p100Btn != null) p100Btn.render(guiGraphics, mouseX, mouseY, partialTick);
        if (p500Btn != null) p500Btn.render(guiGraphics, mouseX, mouseY, partialTick);
        if (p1000Btn != null) p1000Btn.render(guiGraphics, mouseX, mouseY, partialTick);

        if (saveBtn != null) saveBtn.render(guiGraphics, mouseX, mouseY, partialTick);
        if (deleteBtn != null) deleteBtn.render(guiGraphics, mouseX, mouseY, partialTick);
        if (cancelBtn != null) cancelBtn.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (costBox != null && costBox.mouseClicked(mouseX, mouseY, button)) return true;
        if (amountBox != null && amountBox.mouseClicked(mouseX, mouseY, button)) return true;

        if (p10Btn != null && p10Btn.mouseClicked(mouseX, mouseY, button)) return true;
        if (p50Btn != null && p50Btn.mouseClicked(mouseX, mouseY, button)) return true;
        if (p100Btn != null && p100Btn.mouseClicked(mouseX, mouseY, button)) return true;
        if (p500Btn != null && p500Btn.mouseClicked(mouseX, mouseY, button)) return true;
        if (p1000Btn != null && p1000Btn.mouseClicked(mouseX, mouseY, button)) return true;

        if (saveBtn != null && saveBtn.mouseClicked(mouseX, mouseY, button)) return true;
        if (deleteBtn != null && deleteBtn.mouseClicked(mouseX, mouseY, button)) return true;
        if (cancelBtn != null && cancelBtn.mouseClicked(mouseX, mouseY, button)) return true;

        return true; // Absorb click inside modal
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) { // ESC key
            parent.closeModal();
            return true;
        }
        if (costBox != null && costBox.isFocused() && costBox.keyPressed(keyCode, scanCode, modifiers)) return true;
        if (amountBox != null && amountBox.isFocused() && amountBox.keyPressed(keyCode, scanCode, modifiers)) return true;
        return true;
    }

    public boolean charTyped(char codePoint, int modifiers) {
        if (costBox != null && costBox.isFocused() && Character.isDigit(codePoint)) {
            return costBox.charTyped(codePoint, modifiers);
        }
        if (amountBox != null && amountBox.isFocused() && Character.isDigit(codePoint)) {
            return amountBox.charTyped(codePoint, modifiers);
        }
        return true;
    }
}
