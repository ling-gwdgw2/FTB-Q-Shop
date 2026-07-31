package com.holysweet.questshop.client.screen;

import com.holysweet.questshop.api.ShopCategory;
import com.holysweet.questshop.api.ShopEntry;
import com.holysweet.questshop.client.ClientCategories;
import com.holysweet.questshop.client.ClientCoins;
import com.holysweet.questshop.client.ClientFX;
import com.holysweet.questshop.client.ClientShopData;
import com.holysweet.questshop.menu.ShopMenu;
import com.holysweet.questshop.network.payload.BuyEntryPayload;
import com.holysweet.questshop.network.payload.BuyResultPayload;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ShopMenuScreen extends AbstractContainerScreen<ShopMenu> {

    public static final Logger LOGGER = LogUtils.getLogger();
    private ShopList list;
    private Button buyButton;
    private EditBox searchBox;
    private ResourceLocation selectedCategory = null;
    private final List<Button> categoryButtons = new ArrayList<>();
    private boolean purchasePending = false;

    public ShopMenuScreen(ShopMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 280;
        this.imageHeight = 220;
    }

    @Override
    protected void init() {
        super.init();

        // 1. Sidebar Category Buttons (Left Panel)
        categoryButtons.clear();
        int catX = this.leftPos - 105;
        int catY = this.topPos + 8;
        int catWidth = 100;
        int catHeight = 18;

        // "All Items" Category Button
        Button allBtn = Button.builder(
            Component.literal("All Items"),
            b -> {
                selectedCategory = null;
                refreshEntries();
            }
        ).bounds(catX, catY, catWidth, catHeight).build();
        categoryButtons.add(allBtn);
        this.addRenderableWidget(allBtn);

        int currentY = catY + catHeight + 2;
        Map<ResourceLocation, ShopCategory> categories = ClientCategories.categories();
        for (ShopCategory cat : categories.values()) {
            String label = cat.display();
            if (label.length() > 14) {
                label = label.substring(0, 12) + "..";
            }
            ResourceLocation catId = cat.id();
            Button catBtn = Button.builder(
                Component.literal(label),
                b -> {
                    selectedCategory = catId;
                    refreshEntries();
                }
            ).bounds(catX, currentY, catWidth, catHeight).tooltip(Tooltip.create(Component.literal(cat.display()))).build();
            categoryButtons.add(catBtn);
            this.addRenderableWidget(catBtn);
            currentY += catHeight + 2;
            if (currentY > this.topPos + this.imageHeight - 20) {
                break;
            }
        }

        // 2. Search Box (EditBox at top)
        int searchX = this.leftPos + 8;
        int searchY = this.topPos + 22;
        int searchWidth = this.imageWidth - 16;
        this.searchBox = new EditBox(this.font, searchX, searchY, searchWidth, 16, Component.literal("Search"));
        this.searchBox.setHint(Component.literal("Search items..."));
        this.searchBox.setResponder(text -> refreshEntries());
        this.addRenderableWidget(this.searchBox);

        // 3. Shop List Widget
        int listY = searchY + 20;
        int listHeight = this.imageHeight - 72;
        this.list = new ShopList(Minecraft.getInstance(), this.imageWidth - 16, listHeight, listY, 20);
        this.list.setX(this.leftPos + 8);
        this.list.setOnSelectionChanged(this::updateButtonState);
        this.addRenderableWidget(this.list);

        // 4. Buy Button
        int buyWidth = (int) Math.round(this.imageWidth * 0.6);
        int buyX = this.leftPos + (this.imageWidth - buyWidth) / 2;
        int buyY = this.topPos + this.imageHeight - 26;
        this.buyButton = Button.builder(Component.literal("Buy Item"), this::buttonClick)
                .bounds(buyX, buyY, buyWidth, 20)
                .build();
        this.addRenderableWidget(this.buyButton);

        refreshEntries();
    }

    public void refreshEntries() {
        List<ShopEntry> rawEntries = ClientShopData.get();
        String query = searchBox != null ? searchBox.getValue().toLowerCase().trim() : "";

        List<ShopListEntry> filtered = rawEntries.stream()
                .filter(entry -> {
                    // Category filter
                    if (selectedCategory != null && !entry.category().equals(selectedCategory)) {
                        return false;
                    }
                    // Search query filter
                    if (!query.isEmpty()) {
                        String name = entry.itemId().getPath().toLowerCase();
                        return name.contains(query) || entry.itemId().toString().toLowerCase().contains(query);
                    }
                    return true;
                })
                .map(ShopListEntry::new)
                .toList();

        if (this.list != null) {
            this.list.setEntries(filtered);
        }
        updateButtonState();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.title, 8, 6, 0xFFFFFF, false);
        String coinsText = "Primogems: " + ClientCoins.get();
        int coinsX = this.imageWidth - 8 - this.font.width(coinsText);
        guiGraphics.drawString(this.font, coinsText, coinsX, 6, 0xFFD700, false);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        // Main window background
        guiGraphics.fill(this.leftPos, this.topPos, this.leftPos + this.imageWidth, this.topPos + this.imageHeight, 0xF0101018);
        // Sidebar background
        guiGraphics.fill(this.leftPos - 110, this.topPos, this.leftPos - 2, this.topPos + this.imageHeight, 0xF0151522);
    }

    public void onPurchaseResult(BuyResultPayload.Code code) {
        this.purchasePending = false;
        switch (code) {
            case INVALID_ENTRY -> ClientFX.purchaseError(Component.translatable("questshop.buy.invalid"));
            case NOT_ENOUGH_COINS -> ClientFX.purchaseError(Component.translatable("questshop.buy.no_coins"));
            case NO_INVENTORY_SPACE -> ClientFX.purchaseError(Component.translatable("questshop.buy.no_space"));
            case LOCKED_CATEGORY -> ClientFX.purchaseError(Component.translatable("questshop.buy.locked"));
        }
        updateButtonState();
    }

    public void onPurchaseOk(ResourceLocation itemId, int amount, int cost) {
        this.purchasePending = false;
        ClientFX.purchaseOk(itemId, amount, cost);
        updateButtonState();
    }

    public void updateButtonState() {
        if (this.buyButton == null) return;
        boolean canBuy = false;
        if (!this.purchasePending && this.list != null && this.list.getSelected() != null) {
            ShopListEntry selected = (ShopListEntry) this.list.getSelected();
            if (selected != null) {
                ShopEntry data = selected.data;
                if (ClientCategories.isUnlocked(data.category()) && ClientCoins.get() >= data.cost()) {
                    canBuy = true;
                }
            }
        }
        this.buyButton.active = canBuy;
    }

    private void buttonClick(Button btn) {
        if (this.purchasePending) return;
        this.purchasePending = true;
        btn.active = false;
        if (this.list == null || this.list.getSelected() == null) {
            this.purchasePending = false;
            updateButtonState();
            return;
        }
        ShopListEntry selected = (ShopListEntry) this.list.getSelected();
        if (selected == null) {
            this.purchasePending = false;
            updateButtonState();
            return;
        }
        ShopEntry data = selected.data;
        PacketDistributor.sendToServer(new BuyEntryPayload(data.itemId(), data.amount(), data.cost(), data.category()));
    }

    @Override
    public void removed() {
        super.removed();
        this.purchasePending = false;
    }
}
