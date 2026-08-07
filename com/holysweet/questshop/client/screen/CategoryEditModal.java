package com.holysweet.questshop.client.screen;

import com.holysweet.questshop.api.ShopCategory;
import com.holysweet.questshop.network.payload.AdminUpdateCategoryPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.PacketDistributor;

public class CategoryEditModal {

    private final ShopMenuScreen parent;
    private final ShopCategory category;
    private final boolean isNew;

    private EditBox nameBox;
    private Button unlockToggleBtn;
    private Button saveBtn;
    private Button deleteBtn;
    private Button cancelBtn;

    private boolean unlockedByDefault = true;

    public CategoryEditModal(ShopMenuScreen parent, ShopCategory category) {
        this.parent = parent;
        this.category = category;
        this.isNew = category == null;
        if (category != null) {
            this.unlockedByDefault = category.unlockedByDefault();
        }
    }

    public void init(int leftPos, int topPos) {
        int modalWidth = 210;
        int modalHeight = 145;
        int modalX = leftPos + (280 - modalWidth) / 2;
        int modalY = topPos + (230 - modalHeight) / 2;

        this.nameBox = new EditBox(Minecraft.getInstance().font, modalX + 15, modalY + 32, 180, 16, Component.literal("Name"));
        this.nameBox.setHint(Component.literal("Category Name (e.g. Magic)"));
        if (category != null) {
            this.nameBox.setValue(category.display());
        }

        int toggleY = modalY + 58;
        this.unlockToggleBtn = Button.builder(
            Component.literal(unlockedByDefault ? "Unlocked by Default: YES" : "Unlocked by Default: NO"),
            b -> {
                unlockedByDefault = !unlockedByDefault;
                b.setMessage(Component.literal(unlockedByDefault ? "Unlocked by Default: YES" : "Unlocked by Default: NO"));
            }
        ).bounds(modalX + 15, toggleY, 180, 16).build();

        int actionY = modalY + 110;
        this.saveBtn = Button.builder(Component.literal(isNew ? "Create" : "Save"), b -> onSave()).bounds(modalX + 15, actionY, 55, 18).build();

        if (!isNew) {
            this.deleteBtn = Button.builder(Component.literal("Delete"), b -> onDelete()).bounds(modalX + 76, actionY, 55, 18).build();
        }
        int cancelX = !isNew ? modalX + 137 : modalX + 76;
        this.cancelBtn = Button.builder(Component.literal("Cancel"), b -> parent.closeCategoryModal()).bounds(cancelX, actionY, 55, 18).build();
    }

    private void onSave() {
        if (nameBox == null) return;
        String name = nameBox.getValue().trim();
        if (name.isEmpty()) return;

        ResourceLocation catId;
        int order = 0;
        if (category != null) {
            catId = category.id();
            order = category.order();
        } else {
            String path = name.toLowerCase().replaceAll("[^a-z0-9_]", "_");
            if (path.isEmpty()) path = "cat_" + System.currentTimeMillis();
            catId = ResourceLocation.fromNamespaceAndPath("questshop", path);
        }

        PacketDistributor.sendToServer(new AdminUpdateCategoryPayload(catId, name, unlockedByDefault, order, false));
        parent.closeCategoryModal();
    }

    private void onDelete() {
        if (category != null) {
            PacketDistributor.sendToServer(new AdminUpdateCategoryPayload(category.id(), "", false, 0, true));
        }
        parent.closeCategoryModal();
    }

    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, int leftPos, int topPos) {
        int modalWidth = 210;
        int modalHeight = 145;
        int modalX = leftPos + (280 - modalWidth) / 2;
        int modalY = topPos + (230 - modalHeight) / 2;

        // Dark background overlay
        guiGraphics.fill(leftPos - 110, topPos, leftPos + 280, topPos + 230, 0xCC000000);

        // Modal background
        guiGraphics.fill(modalX, modalY, modalX + modalWidth, modalY + modalHeight, 0xF0141420);
        guiGraphics.fill(modalX + 1, modalY + 1, modalX + modalWidth - 1, modalY + modalHeight - 1, 0xF01E1E2E);

        // Header Title
        String title = isNew ? "Create New Category" : "Edit Category";
        guiGraphics.drawString(Minecraft.getInstance().font, title, modalX + 15, modalY + 12, 0xFFD700, false);

        if (nameBox != null) nameBox.render(guiGraphics, mouseX, mouseY, partialTick);
        if (unlockToggleBtn != null) unlockToggleBtn.render(guiGraphics, mouseX, mouseY, partialTick);
        if (saveBtn != null) saveBtn.render(guiGraphics, mouseX, mouseY, partialTick);
        if (deleteBtn != null) deleteBtn.render(guiGraphics, mouseX, mouseY, partialTick);
        if (cancelBtn != null) cancelBtn.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (nameBox != null && nameBox.mouseClicked(mouseX, mouseY, button)) return true;
        if (unlockToggleBtn != null && unlockToggleBtn.mouseClicked(mouseX, mouseY, button)) return true;
        if (saveBtn != null && saveBtn.mouseClicked(mouseX, mouseY, button)) return true;
        if (deleteBtn != null && deleteBtn.mouseClicked(mouseX, mouseY, button)) return true;
        if (cancelBtn != null && cancelBtn.mouseClicked(mouseX, mouseY, button)) return true;
        return true;
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) { // ESC
            parent.closeCategoryModal();
            return true;
        }
        if (nameBox != null && nameBox.isFocused() && nameBox.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        return true;
    }

    public boolean charTyped(char codePoint, int modifiers) {
        if (nameBox != null && nameBox.isFocused() && nameBox.charTyped(codePoint, modifiers)) {
            return true;
        }
        return true;
    }
}
