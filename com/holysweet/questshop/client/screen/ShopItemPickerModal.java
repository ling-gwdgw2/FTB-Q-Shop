package com.holysweet.questshop.client.screen;

import com.holysweet.questshop.QuestShop;
import com.holysweet.questshop.api.ShopEntry;
import com.holysweet.questshop.network.payload.AdminUpdateEntryPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.neoforged.neoforge.network.PacketDistributor;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;

public class ShopItemPickerModal {

    private final ShopMenuScreen parent;
    private final ResourceLocation category;

    private EditBox searchBox;
    private Button prevBtn;
    private Button nextBtn;
    private Button cancelBtn;

    private record ItemSearchEntry(Item item, String searchId, String searchName) {}
    private final List<ItemSearchEntry> allEntries = new ArrayList<>();
    private List<Item> filteredItems = new ArrayList<>();

    private int currentPage = 0;
    private static final int COLS = 8;
    private static final int ROWS = 5;
    private static final int PAGE_SIZE = COLS * ROWS; // 40 items per page

    public ShopItemPickerModal(ShopMenuScreen parent, ResourceLocation category) {
        this.parent = parent;
        this.category = category != null ? category : ResourceLocation.fromNamespaceAndPath(QuestShop.MODID, "create_tech");

        for (Item item : BuiltInRegistries.ITEM) {
            if (item != Items.AIR) {
                ResourceLocation key = BuiltInRegistries.ITEM.getKey(item);
                String idStr = key.toString().toLowerCase();
                String nameStr = item.getName(item.getDefaultInstance()).getString().toLowerCase();
                this.allEntries.add(new ItemSearchEntry(item, idStr, nameStr));
            }
        }
        this.filteredItems = this.allEntries.stream().map(ItemSearchEntry::item).toList();
    }

    public void init(int leftPos, int topPos, int imageWidth, int imageHeight) {
        int modalWidth = 240;
        int modalHeight = 190;
        int modalX = leftPos + (imageWidth - modalWidth) / 2;
        int modalY = topPos + (imageHeight - modalHeight) / 2;

        // Search bar
        this.searchBox = new EditBox(Minecraft.getInstance().font, modalX + 10, modalY + 8, modalWidth - 20, 16, Component.literal("Search"));
        this.searchBox.setHint(Component.literal("Search items... (e.g. create, diamond)"));
        this.searchBox.setResponder(text -> filterItems(text));
        this.searchBox.setFocused(true);

        // Navigation & Cancel buttons
        int btnY = modalY + modalHeight - 24;
        this.prevBtn = Button.builder(Component.literal("< Prev"), b -> changePage(-1)).bounds(modalX + 10, btnY, 50, 16).build();
        this.nextBtn = Button.builder(Component.literal("Next >"), b -> changePage(1)).bounds(modalX + 65, btnY, 50, 16).build();
        this.cancelBtn = Button.builder(Component.literal("Close"), b -> parent.closeItemPicker()).bounds(modalX + modalWidth - 60, btnY, 50, 16).build();

        updateNavButtons();
    }

    private void filterItems(String query) {
        String q = query.toLowerCase().trim();
        if (q.isEmpty()) {
            this.filteredItems = this.allEntries.stream().map(ItemSearchEntry::item).toList();
        } else {
            this.filteredItems = this.allEntries.stream()
                    .filter(e -> e.searchId().contains(q) || e.searchName().contains(q))
                    .map(ItemSearchEntry::item)
                    .toList();
        }
        this.currentPage = 0;
        updateNavButtons();
    }

    private void changePage(int delta) {
        int maxPages = Math.max(1, (int) Math.ceil((double) filteredItems.size() / PAGE_SIZE));
        this.currentPage = Math.max(0, Math.min(maxPages - 1, this.currentPage + delta));
        updateNavButtons();
    }

    private void updateNavButtons() {
        int maxPages = Math.max(1, (int) Math.ceil((double) filteredItems.size() / PAGE_SIZE));
        if (this.prevBtn != null) this.prevBtn.active = this.currentPage > 0;
        if (this.nextBtn != null) this.nextBtn.active = this.currentPage < maxPages - 1;
    }

    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, int leftPos, int topPos, int imageWidth, int imageHeight) {
        int modalWidth = 240;
        int modalHeight = 190;
        int modalX = leftPos + (imageWidth - modalWidth) / 2;
        int modalY = topPos + (imageHeight - modalHeight) / 2;

        // Dark background overlay covering full game window
        guiGraphics.fill(0, 0, Minecraft.getInstance().getWindow().getGuiScaledWidth(), Minecraft.getInstance().getWindow().getGuiScaledHeight(), 0xCC000000);

        // Modal window background
        guiGraphics.fill(modalX, modalY, modalX + modalWidth, modalY + modalHeight, 0xF0141420);
        guiGraphics.fill(modalX + 1, modalY + 1, modalX + modalWidth - 1, modalY + modalHeight - 1, 0xF01E1E2E);

        // Search Box
        if (this.searchBox != null) this.searchBox.render(guiGraphics, mouseX, mouseY, partialTick);

        // Render Item Grid
        int startX = modalX + 16;
        int startY = modalY + 30;
        int slotSize = 25;

        int startIndex = this.currentPage * PAGE_SIZE;
        int endIndex = Math.min(startIndex + PAGE_SIZE, this.filteredItems.size());

        ItemStack hoveredStack = ItemStack.EMPTY;

        for (int i = startIndex; i < endIndex; i++) {
            int localIndex = i - startIndex;
            int col = localIndex % COLS;
            int row = localIndex / COLS;

            int slotX = startX + col * slotSize;
            int slotY = startY + row * slotSize;

            Item item = this.filteredItems.get(i);
            ItemStack stack = item.getDefaultInstance();

            boolean isHovered = mouseX >= slotX && mouseX < slotX + 22 && mouseY >= slotY && mouseY < slotY + 22;
            int slotBg = isHovered ? 0x60FFFFFF : 0x20FFFFFF;
            guiGraphics.fill(slotX, slotY, slotX + 22, slotY + 22, slotBg);

            guiGraphics.renderItem(stack, slotX + 3, slotY + 3);

            if (isHovered) {
                hoveredStack = stack;
            }
        }

        // Render Page Numbers
        int maxPages = Math.max(1, (int) Math.ceil((double) filteredItems.size() / PAGE_SIZE));
        String pageStr = (this.currentPage + 1) + "/" + maxPages;
        int pageStrX = modalX + 120 - Minecraft.getInstance().font.width(pageStr) / 2;
        guiGraphics.drawString(Minecraft.getInstance().font, pageStr, pageStrX, modalY + modalHeight - 20, 0xAAAAAA, false);

        // Buttons
        if (this.prevBtn != null) this.prevBtn.render(guiGraphics, mouseX, mouseY, partialTick);
        if (this.nextBtn != null) this.nextBtn.render(guiGraphics, mouseX, mouseY, partialTick);
        if (this.cancelBtn != null) this.cancelBtn.render(guiGraphics, mouseX, mouseY, partialTick);

        // Hovered Item Tooltip
        if (!hoveredStack.isEmpty()) {
            guiGraphics.renderTooltip(Minecraft.getInstance().font, hoveredStack, mouseX, mouseY);
        }
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (scrollY > 0) { // Scroll up -> Prev Page
            changePage(-1);
            return true;
        } else if (scrollY < 0) { // Scroll down -> Next Page
            changePage(1);
            return true;
        }
        return false;
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.searchBox != null && this.searchBox.mouseClicked(mouseX, mouseY, button)) return true;
        if (this.prevBtn != null && this.prevBtn.mouseClicked(mouseX, mouseY, button)) return true;
        if (this.nextBtn != null && this.nextBtn.mouseClicked(mouseX, mouseY, button)) return true;
        if (this.cancelBtn != null && this.cancelBtn.mouseClicked(mouseX, mouseY, button)) return true;

        int modalWidth = 240;
        int modalHeight = 190;
        int leftPos = this.parent.getGuiLeftPos();
        int topPos = this.parent.getGuiTopPos();
        int modalX = leftPos + (280 - modalWidth) / 2;
        int modalY = topPos + (230 - modalHeight) / 2;

        int startX = modalX + 16;
        int startY = modalY + 30;
        int slotSize = 25;

        int startIndex = this.currentPage * PAGE_SIZE;
        int endIndex = Math.min(startIndex + PAGE_SIZE, this.filteredItems.size());

        for (int i = startIndex; i < endIndex; i++) {
            int localIndex = i - startIndex;
            int col = localIndex % COLS;
            int row = localIndex / COLS;

            int slotX = startX + col * slotSize;
            int slotY = startY + row * slotSize;

            if (mouseX >= slotX && mouseX < slotX + 22 && mouseY >= slotY && mouseY < slotY + 22) {
                Item selectedItem = this.filteredItems.get(i);
                ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(selectedItem);
                PacketDistributor.sendToServer(new AdminUpdateEntryPayload(itemId, 1, 10, this.category, 0, -1));
                this.parent.closeItemPicker();
                return true;
            }
        }
        return true; // Consume click inside modal
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) { // ESC
            this.parent.closeItemPicker();
            return true;
        }
        if (this.searchBox != null && this.searchBox.isFocused() && this.searchBox.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        return true;
    }

    public boolean charTyped(char codePoint, int modifiers) {
        if (this.searchBox != null && this.searchBox.isFocused() && this.searchBox.charTyped(codePoint, modifiers)) {
            return true;
        }
        return true;
    }
}
