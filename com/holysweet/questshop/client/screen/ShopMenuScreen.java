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
    private EditBox qtyBox;
    private Button minusBtn;
    private Button plusBtn;
    private Button stackBtn;
    private Button maxBtn;

    private ResourceLocation selectedCategory = null;
    private final List<Button> categoryButtons = new ArrayList<>();
    private boolean purchasePending = false;

    public ShopMenuScreen(ShopMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 280;
        this.imageHeight = 230;
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
        int listHeight = this.imageHeight - 88;
        this.list = new ShopList(Minecraft.getInstance(), this.imageWidth - 16, listHeight, listY, 20);
        this.list.setX(this.leftPos + 8);
        this.list.setOnSelectionChanged(this::updateButtonState);
        this.addRenderableWidget(this.list);

        // 4. Quantity Controls & Buy Button Panel
        int panelY = this.topPos + this.imageHeight - 24;
        
        // Quantity label and EditBox
        this.qtyBox = new EditBox(this.font, this.leftPos + 8, panelY, 36, 16, Component.literal("Qty"));
        this.qtyBox.setValue("1");
        this.qtyBox.setResponder(text -> updateButtonState());
        this.addRenderableWidget(this.qtyBox);

        // [-] [+] [x64] [MAX] buttons
        this.minusBtn = Button.builder(Component.literal("-"), b -> changeQty(-1))
                .bounds(this.leftPos + 46, panelY, 14, 16).build();
        this.plusBtn = Button.builder(Component.literal("+"), b -> changeQty(1))
                .bounds(this.leftPos + 61, panelY, 14, 16).build();
        this.stackBtn = Button.builder(Component.literal("x64"), b -> setQty(64))
                .bounds(this.leftPos + 77, panelY, 26, 16).build();
        this.maxBtn = Button.builder(Component.literal("MAX"), b -> setMaxQty())
                .bounds(this.leftPos + 105, panelY, 30, 16).build();

        this.addRenderableWidget(this.minusBtn);
        this.addRenderableWidget(this.plusBtn);
        this.addRenderableWidget(this.stackBtn);
        this.addRenderableWidget(this.maxBtn);

        // Buy Button
        int buyX = this.leftPos + 138;
        int buyWidth = this.imageWidth - 146;
        this.buyButton = Button.builder(Component.literal("Buy Item"), this::buttonClick)
                .bounds(buyX, panelY, buyWidth, 16)
                .build();
        this.addRenderableWidget(this.buyButton);

        refreshEntries();
    }

    private int getQuantity() {
        if (this.qtyBox == null) return 1;
        try {
            String val = this.qtyBox.getValue().trim();
            if (val.isEmpty()) return 1;
            int q = Integer.parseInt(val);
            return Math.max(1, Math.min(999, q));
        } catch (NumberFormatException e) {
            return 1;
        }
    }

    private void changeQty(int delta) {
        int current = getQuantity();
        int next = Math.max(1, Math.min(999, current + delta));
        if (this.qtyBox != null) {
            this.qtyBox.setValue(String.valueOf(next));
        }
    }

    private void setQty(int qty) {
        if (this.qtyBox != null) {
            this.qtyBox.setValue(String.valueOf(Math.max(1, Math.min(999, qty))));
        }
    }

    private void setMaxQty() {
        if (this.list == null || this.list.getSelected() == null) return;
        ShopListEntry selected = (ShopListEntry) this.list.getSelected();
        if (selected == null || selected.data == null) return;
        
        int unitCost = selected.data.cost();
        int userCoins = ClientCoins.get();
        if (unitCost <= 0) {
            setQty(999);
            return;
        }
        int maxAfford = userCoins / unitCost;
        setQty(Math.max(1, Math.min(999, maxAfford)));
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        boolean searchFocused = this.searchBox != null && this.searchBox.isFocused();
        boolean qtyFocused = this.qtyBox != null && this.qtyBox.isFocused();

        if (searchFocused || qtyFocused) {
            if (keyCode == 256) { // ESC key
                if (searchFocused) this.searchBox.setFocused(false);
                if (qtyFocused) this.qtyBox.setFocused(false);
                return true;
            }
            if (searchFocused && this.searchBox.keyPressed(keyCode, scanCode, modifiers)) {
                return true;
            }
            if (qtyFocused && this.qtyBox.keyPressed(keyCode, scanCode, modifiers)) {
                return true;
            }
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (this.qtyBox != null && this.qtyBox.isFocused()) {
            if (Character.isDigit(codePoint)) {
                return this.qtyBox.charTyped(codePoint, modifiers);
            }
            return true;
        }
        if (this.searchBox != null && this.searchBox.isFocused()) {
            if (this.searchBox.charTyped(codePoint, modifiers)) {
                return true;
            }
        }
        return super.charTyped(codePoint, modifiers);
    }

    public void refreshEntries() {
        List<ShopEntry> rawEntries = ClientShopData.get();
        String query = searchBox != null ? searchBox.getValue().toLowerCase().trim() : "";

        List<ShopListEntry> filtered = rawEntries.stream()
                .filter(entry -> {
                    if (selectedCategory != null && !entry.category().equals(selectedCategory)) {
                        return false;
                    }
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
        int qty = getQuantity();

        if (!this.purchasePending && this.list != null && this.list.getSelected() != null) {
            ShopListEntry selected = (ShopListEntry) this.list.getSelected();
            if (selected != null && selected.data != null) {
                ShopEntry data = selected.data;
                int totalCost = data.cost() * qty;
                int totalItems = data.amount() * qty;

                this.buyButton.setMessage(Component.literal("Buy x" + totalItems + " (" + totalCost + " Gems)"));

                if (ClientCategories.isUnlocked(data.category()) && ClientCoins.get() >= totalCost) {
                    canBuy = true;
                }
            } else {
                this.buyButton.setMessage(Component.literal("Buy Item"));
            }
        } else {
            this.buyButton.setMessage(Component.literal("Buy Item"));
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
        if (selected == null || selected.data == null) {
            this.purchasePending = false;
            updateButtonState();
            return;
        }
        ShopEntry data = selected.data;
        int qty = getQuantity();
        int totalAmount = data.amount() * qty;
        int totalCost = data.cost() * qty;

        PacketDistributor.sendToServer(new BuyEntryPayload(data.itemId(), totalAmount, totalCost, data.category()));
    }

    @Override
    public void removed() {
        super.removed();
        this.purchasePending = false;
    }
}
