package com.holysweet.questshop.client.screen;

import com.holysweet.questshop.QuestShop;
import com.holysweet.questshop.api.ShopCategory;
import com.holysweet.questshop.api.ShopEntry;
import com.holysweet.questshop.client.ClientCategories;
import com.holysweet.questshop.client.ClientCoins;
import com.holysweet.questshop.client.ClientFX;
import com.holysweet.questshop.client.ClientShopData;
import com.holysweet.questshop.menu.ShopMenu;
import com.holysweet.questshop.network.payload.AdminRemoveEntryPayload;
import com.holysweet.questshop.network.payload.AdminUpdateEntryPayload;
import com.holysweet.questshop.network.payload.BuyEntryPayload;
import com.holysweet.questshop.network.payload.BuyResultPayload;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.PacketDistributor;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
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
    private EditBox priceBox;
    private Button minusBtn;
    private Button plusBtn;
    private Button stackBtn;
    private Button maxBtn;

    // Creative / Admin Edit Mode Controls
    private boolean editMode = false;
    private Button editToggleBtn;
    private Button browseItemsBtn;
    private Button addHandItemBtn;
    private Button editPriceBtn;
    private Button removeBtn;
    private Button addCategoryBtn;

    private ShopItemPickerModal activePicker = null;
    private CategoryEditModal activeCategoryModal = null;

    private ResourceLocation selectedCategory = null;
    private final List<Button> categoryButtons = new ArrayList<>();
    private int categoryScrollOffset = 0;
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
        rebuildCategorySidebar();

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
        int listHeight = this.imageHeight - 92;
        this.list = new ShopList(Minecraft.getInstance(), this.imageWidth - 16, listHeight, listY, 20);
        this.list.setX(this.leftPos + 8);
        this.list.setOnSelectionChanged(this::updateButtonState);
        this.addRenderableWidget(this.list);

        // 4. Quantity Controls & Buy Button Panel
        int panelY = this.topPos + this.imageHeight - 24;
        
        // Quantity label and EditBox
        this.qtyBox = new EditBox(this.font, this.leftPos + 8, panelY, 32, 16, Component.literal("Qty"));
        this.qtyBox.setValue("1");
        this.qtyBox.setHint(Component.literal("Qty"));
        this.qtyBox.setResponder(text -> updateButtonState());
        this.addRenderableWidget(this.qtyBox);

        // Price EditBox (for Admin Edit Mode)
        this.priceBox = new EditBox(this.font, this.leftPos + 44, panelY, 40, 16, Component.literal("Cost"));
        this.priceBox.setValue("10");
        this.priceBox.setHint(Component.literal("Gems"));
        this.priceBox.visible = false;
        this.addRenderableWidget(this.priceBox);

        // [-] [+] [x64] [MAX] buttons
        this.minusBtn = Button.builder(Component.literal("-"), b -> changeQty(-1))
                .bounds(this.leftPos + 44, panelY, 14, 16).build();
        this.plusBtn = Button.builder(Component.literal("+"), b -> changeQty(1))
                .bounds(this.leftPos + 59, panelY, 14, 16).build();
        this.stackBtn = Button.builder(Component.literal("x64"), b -> setQty(64))
                .bounds(this.leftPos + 75, panelY, 26, 16).build();
        this.maxBtn = Button.builder(Component.literal("MAX"), b -> setMaxQty())
                .bounds(this.leftPos + 103, panelY, 30, 16).build();

        this.addRenderableWidget(this.minusBtn);
        this.addRenderableWidget(this.plusBtn);
        this.addRenderableWidget(this.stackBtn);
        this.addRenderableWidget(this.maxBtn);

        // Buy Button
        int buyX = this.leftPos + 136;
        int buyWidth = this.imageWidth - 144;
        this.buyButton = Button.builder(Component.literal("Buy Item"), this::buttonClick)
                .bounds(buyX, panelY, buyWidth, 16)
                .build();
        this.addRenderableWidget(this.buyButton);

        // Creative Mode / Admin Controls Initialization
        if (Minecraft.getInstance().player != null && (Minecraft.getInstance().player.isCreative() || Minecraft.getInstance().player.hasPermissions(2))) {
            int toggleX = this.leftPos + 70;
            int toggleY = this.topPos + 4;
            this.editToggleBtn = Button.builder(
                Component.literal("[Edit: OFF]"),
                b -> {
                    this.editMode = !this.editMode;
                    b.setMessage(Component.literal(this.editMode ? "[Edit: ON]" : "[Edit: OFF]"));
                    refreshEditUI();
                    rebuildCategorySidebar();
                }
            ).bounds(toggleX, toggleY, 65, 14).build();
            this.addRenderableWidget(this.editToggleBtn);

            int adminPanelY = this.topPos + this.imageHeight - 44;
            this.browseItemsBtn = Button.builder(
                Component.literal("+ Browse Items"),
                b -> openItemPicker()
            ).bounds(this.leftPos + 8, adminPanelY, 95, 16).build();

            this.addHandItemBtn = Button.builder(
                Component.literal("+ Hand"),
                b -> addHandItemToShop()
            ).bounds(this.leftPos + 107, adminPanelY, 50, 16).build();

            this.editPriceBtn = Button.builder(
                Component.literal("Save"),
                b -> updateSelectedItem()
            ).bounds(this.leftPos + 161, adminPanelY, 40, 16).build();

            this.removeBtn = Button.builder(
                Component.literal("Remove"),
                b -> removeSelectedItem()
            ).bounds(this.leftPos + 205, adminPanelY, 50, 16).build();

            this.addRenderableWidget(this.browseItemsBtn);
            this.addRenderableWidget(this.addHandItemBtn);
            this.addRenderableWidget(this.editPriceBtn);
            this.addRenderableWidget(this.removeBtn);

            refreshEditUI();
        }

        if (this.activePicker != null) {
            this.activePicker.init(this.leftPos, this.topPos);
        }

        if (this.activeCategoryModal != null) {
            this.activeCategoryModal.init(this.leftPos, this.topPos);
        }

        refreshEntries();
    }

    public void rebuildCategorySidebar() {
        for (Button btn : categoryButtons) {
            this.removeWidget(btn);
        }
        categoryButtons.clear();

        int catX = this.leftPos - 110;
        int catY = this.topPos + 8;
        int catWidth = 106;
        int catHeight = 18;

        boolean allSelected = selectedCategory == null;
        Button allBtn = Button.builder(
            Component.literal((allSelected ? "> " : "") + "All Items"),
            b -> {
                selectedCategory = null;
                refreshEntries();
            }
        ).bounds(catX, catY, catWidth - (this.editMode ? 38 : 0), catHeight).build();
        categoryButtons.add(allBtn);
        this.addRenderableWidget(allBtn);

        if (this.editMode) {
            this.addCategoryBtn = Button.builder(
                Component.literal("+ Cat"),
                b -> openCategoryModal(null)
            ).bounds(catX + 70, catY, 36, catHeight).tooltip(Tooltip.create(Component.literal("Add New Category"))).build();
            this.addRenderableWidget(this.addCategoryBtn);
            categoryButtons.add(this.addCategoryBtn);
        }

        int currentY = catY + catHeight + 2;
        List<ShopCategory> catList = new ArrayList<>(ClientCategories.categories().values());
        int maxVisible = (this.imageHeight - 35) / 20;
        int maxScroll = Math.max(0, catList.size() - maxVisible);
        this.categoryScrollOffset = Math.max(0, Math.min(maxScroll, this.categoryScrollOffset));

        int startIndex = this.categoryScrollOffset;
        int endIndex = Math.min(catList.size(), startIndex + maxVisible);

        for (int i = startIndex; i < endIndex; i++) {
            ShopCategory cat = catList.get(i);
            ResourceLocation catId = cat.id();
            boolean isSelected = selectedCategory != null && selectedCategory.equals(catId);
            String label = (isSelected ? "> " : "") + cat.display();
            int btnW = this.editMode ? catWidth - 20 : catWidth;
            int maxTextW = btnW - 8;
            if (this.font.width(label) > maxTextW) {
                while (label.length() > 3 && this.font.width(label + "..") > maxTextW) {
                    label = label.substring(0, label.length() - 1);
                }
                label = label + "..";
            }
            String tooltipText = cat.display() + (cat.unlockedByDefault() ? "" : " (Locked)");

            Button catBtn = Button.builder(
                Component.literal(label),
                b -> {
                    selectedCategory = catId;
                    refreshEntries();
                }
            ).bounds(catX, currentY, btnW, catHeight).tooltip(Tooltip.create(Component.literal(tooltipText))).build();
            categoryButtons.add(catBtn);
            this.addRenderableWidget(catBtn);

            if (this.editMode) {
                Button editCatBtn = Button.builder(
                    Component.literal("E"),
                    b -> openCategoryModal(cat)
                ).bounds(catX + btnW + 2, currentY, 18, catHeight).tooltip(Tooltip.create(Component.literal("Edit/Delete Category: " + cat.display()))).build();
                categoryButtons.add(editCatBtn);
                this.addRenderableWidget(editCatBtn);
            }

            currentY += catHeight + 2;
        }
    }

    public void openItemPicker() {
        this.activePicker = new ShopItemPickerModal(this, this.selectedCategory);
        this.activePicker.init(this.leftPos, this.topPos);
    }

    public void closeItemPicker() {
        this.activePicker = null;
    }

    public void openCategoryModal(ShopCategory category) {
        this.activeCategoryModal = new CategoryEditModal(this, category);
        this.activeCategoryModal.init(this.leftPos, this.topPos);
    }

    public void closeCategoryModal() {
        this.activeCategoryModal = null;
    }

    private void refreshEditUI() {
        boolean showEdit = this.editMode;

        if (this.addCategoryBtn != null) this.addCategoryBtn.visible = showEdit;
        if (this.browseItemsBtn != null) this.browseItemsBtn.visible = showEdit;
        if (this.addHandItemBtn != null) this.addHandItemBtn.visible = showEdit;
        if (this.editPriceBtn != null) this.editPriceBtn.visible = showEdit;
        if (this.removeBtn != null) this.removeBtn.visible = showEdit;
        if (this.priceBox != null) this.priceBox.visible = showEdit;

        // Hide normal qty buttons when priceBox is shown
        if (this.minusBtn != null) this.minusBtn.visible = !showEdit;
        if (this.plusBtn != null) this.plusBtn.visible = !showEdit;
        if (this.stackBtn != null) this.stackBtn.visible = !showEdit;
        if (this.maxBtn != null) this.maxBtn.visible = !showEdit;
    }

    private void addHandItemToShop() {
        if (Minecraft.getInstance().player == null) return;
        ItemStack held = Minecraft.getInstance().player.getMainHandItem();
        if (held.isEmpty()) {
            ClientFX.purchaseError(Component.literal("Hold an item in main hand!"));
            return;
        }

        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(held.getItem());
        ResourceLocation catId = selectedCategory != null ? selectedCategory : ResourceLocation.fromNamespaceAndPath(QuestShop.MODID, "general");

        int amount = getQuantity();
        int cost = getPriceInput();

        PacketDistributor.sendToServer(new AdminUpdateEntryPayload(itemId, amount, cost, catId));
        ClientFX.purchaseOk(itemId, amount, cost);
    }

    private void updateSelectedItem() {
        if (this.list == null || this.list.getSelected() == null) return;
        ShopListEntry selected = (ShopListEntry) this.list.getSelected();
        if (selected == null || selected.data == null) return;

        ShopEntry oldData = selected.data;
        int newAmount = getQuantity();
        int newCost = getPriceInput();

        PacketDistributor.sendToServer(new AdminUpdateEntryPayload(oldData.itemId(), newAmount, newCost, oldData.category(), oldData.dailyLimit(), oldData.totalStock()));
        ClientFX.purchaseOk(oldData.itemId(), newAmount, newCost);
    }

    private void removeSelectedItem() {
        if (this.list == null || this.list.getSelected() == null) return;
        ShopListEntry selected = (ShopListEntry) this.list.getSelected();
        if (selected == null || selected.data == null) return;

        ShopEntry oldData = selected.data;
        PacketDistributor.sendToServer(new AdminRemoveEntryPayload(oldData.itemId(), oldData.category()));
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

    private int getPriceInput() {
        if (this.priceBox == null) return 10;
        try {
            String val = this.priceBox.getValue().trim();
            if (val.isEmpty()) return 10;
            int p = Integer.parseInt(val);
            return Math.max(0, Math.min(99999, p));
        } catch (NumberFormatException e) {
            return 10;
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
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (this.activeCategoryModal != null) {
            return this.activeCategoryModal.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
        }
        if (this.activePicker != null) {
            return this.activePicker.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
        }

        // Check if mouse is over Sidebar panel
        if (mouseX >= this.leftPos - 110 && mouseX <= this.leftPos - 2 && mouseY >= this.topPos && mouseY <= this.topPos + this.imageHeight) {
            int totalCats = ClientCategories.categories().size();
            int maxVisible = (this.imageHeight - 35) / 20;
            int maxScroll = Math.max(0, totalCats - maxVisible);
            if (maxScroll > 0) {
                if (scrollY > 0) { // Scroll up
                    this.categoryScrollOffset = Math.max(0, this.categoryScrollOffset - 1);
                    rebuildCategorySidebar();
                    return true;
                } else if (scrollY < 0) { // Scroll down
                    this.categoryScrollOffset = Math.min(maxScroll, this.categoryScrollOffset + 1);
                    rebuildCategorySidebar();
                    return true;
                }
            }
        }

        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.activeCategoryModal != null) {
            return this.activeCategoryModal.mouseClicked(mouseX, mouseY, button);
        }
        if (this.activePicker != null) {
            return this.activePicker.mouseClicked(mouseX, mouseY, button);
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.activeCategoryModal != null) {
            return this.activeCategoryModal.keyPressed(keyCode, scanCode, modifiers);
        }
        if (this.activePicker != null) {
            return this.activePicker.keyPressed(keyCode, scanCode, modifiers);
        }

        boolean searchFocused = this.searchBox != null && this.searchBox.isFocused();
        boolean qtyFocused = this.qtyBox != null && this.qtyBox.isFocused();
        boolean priceFocused = this.priceBox != null && this.priceBox.isFocused();

        if (searchFocused || qtyFocused || priceFocused) {
            if (keyCode == 256) { // ESC key
                if (searchFocused) this.searchBox.setFocused(false);
                if (qtyFocused) this.qtyBox.setFocused(false);
                if (priceFocused) this.priceBox.setFocused(false);
                return true;
            }
            if (searchFocused && this.searchBox.keyPressed(keyCode, scanCode, modifiers)) {
                return true;
            }
            if (qtyFocused && this.qtyBox.keyPressed(keyCode, scanCode, modifiers)) {
                return true;
            }
            if (priceFocused && this.priceBox.keyPressed(keyCode, scanCode, modifiers)) {
                return true;
            }
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (this.activeCategoryModal != null) {
            return this.activeCategoryModal.charTyped(codePoint, modifiers);
        }
        if (this.activePicker != null) {
            return this.activePicker.charTyped(codePoint, modifiers);
        }

        if (this.qtyBox != null && this.qtyBox.isFocused()) {
            if (Character.isDigit(codePoint)) {
                return this.qtyBox.charTyped(codePoint, modifiers);
            }
            return true;
        }
        if (this.priceBox != null && this.priceBox.isFocused()) {
            if (Character.isDigit(codePoint)) {
                return this.priceBox.charTyped(codePoint, modifiers);
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
        rebuildCategorySidebar();
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

        if (this.activeCategoryModal != null || this.activePicker != null) {
            // When a modal is open: only render the background panel (not widgets/labels)
            // to prevent buttons, text, and list items from bleeding through the overlay
            this.renderBg(guiGraphics, partialTick, mouseX, mouseY);

            if (this.activeCategoryModal != null) {
                this.activeCategoryModal.render(guiGraphics, mouseX, mouseY, partialTick, this.leftPos, this.topPos);
            } else {
                this.activePicker.render(guiGraphics, mouseX, mouseY, partialTick, this.leftPos, this.topPos);
            }
        } else {
            super.render(guiGraphics, mouseX, mouseY, partialTick);
            this.renderTooltip(guiGraphics, mouseX, mouseY);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        // Skip labels when any modal overlay is open to prevent text bleeding through
        if (this.activePicker != null || this.activeCategoryModal != null) return;

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

        // Sidebar Vertical Scrollbar Indicator
        int totalCats = ClientCategories.categories().size();
        int maxVisible = (this.imageHeight - 35) / 20;
        int maxScroll = Math.max(0, totalCats - maxVisible);

        int trackX = this.leftPos - 5;
        int trackY = this.topPos + 8;
        int trackWidth = 3;
        int trackHeight = this.imageHeight - 16;

        // Draw scrollbar background track
        guiGraphics.fill(trackX, trackY, trackX + trackWidth, trackY + trackHeight, 0xFF0B0B12);

        if (maxScroll > 0) {
            int thumbHeight = Math.max(12, (int) ((float) maxVisible / (totalCats + 1) * trackHeight));
            float scrollProgress = (float) this.categoryScrollOffset / maxScroll;
            int thumbY = trackY + (int) (scrollProgress * (trackHeight - thumbHeight));

            // Draw golden scrollbar thumb
            guiGraphics.fill(trackX, thumbY, trackX + trackWidth, thumbY + thumbHeight, 0xFFFFD700);
        } else {
            // Draw dimmed full thumb when no scrolling needed
            guiGraphics.fill(trackX, trackY, trackX + trackWidth, trackY + trackHeight, 0xFF3A3A50);
        }
    }

    public void onPurchaseResult(BuyResultPayload.Code code) {
        this.purchasePending = false;
        updateButtonState();
        if (code != BuyResultPayload.Code.OK) {
            String transKey = switch (code) {
                case NOT_ENOUGH_COINS -> "ling_q_shop.buy.no_coins";
                case NO_INVENTORY_SPACE -> "ling_q_shop.buy.no_space";
                case LOCKED_CATEGORY -> "ling_q_shop.buy.locked";
                case OUT_OF_STOCK -> "ling_q_shop.buy.out_of_stock";
                case DAILY_LIMIT_REACHED -> "ling_q_shop.buy.daily_limit";
                default -> "ling_q_shop.buy.invalid";
            };
            ClientFX.purchaseError(Component.translatable(transKey));
        }
    }

    public void onPurchaseOk(ResourceLocation itemId, int amount, int cost) {
        this.purchasePending = false;
        if (this.list != null && this.list.getSelected() != null) {
            ShopListEntry selected = (ShopListEntry) this.list.getSelected();
            if (selected != null && selected.data != null && selected.data.itemId().equals(itemId)) {
                com.holysweet.questshop.client.ClientPurchases.recordPurchase(selected.data.category(), itemId, amount);
            }
        }
        updateButtonState();
        ClientFX.purchaseOk(itemId, amount, cost);
    }

    public void updateButtonState() {
        if (this.buyButton == null) return;
        boolean canBuy = false;
        int qty = getQuantity();

        if (!this.purchasePending && this.list != null && this.list.getSelected() != null) {
            ShopListEntry selected = (ShopListEntry) this.list.getSelected();
            if (selected != null && selected.data != null) {
                ShopEntry data = selected.data;
                int totalCost = data.effectiveCost() * qty;
                int totalItems = data.amount() * qty;

                if (com.holysweet.questshop.client.ClientPurchases.isSoldOut(data)) {
                    this.buyButton.setMessage(Component.literal("SOLD OUT"));
                    canBuy = false;
                } else {
                    String btnLabel = "Buy x" + totalItems + " (" + totalCost + " Gems)";
                    if (data.hasDiscount()) {
                        btnLabel = "Buy x" + totalItems + " (" + totalCost + " Gems [-" + data.discountPercent() + "%])";
                    }
                    this.buyButton.setMessage(Component.literal(btnLabel));

                    if (ClientCategories.isUnlocked(data.category()) && ClientCoins.get() >= totalCost) {
                        canBuy = true;
                    }
                }

                if (this.priceBox != null && this.priceBox.visible) {
                    this.priceBox.setValue(String.valueOf(data.cost()));
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
        int totalCost = data.effectiveCost() * qty;

        PacketDistributor.sendToServer(new BuyEntryPayload(data.itemId(), totalAmount, totalCost, data.category()));
    }

    @Override
    public void removed() {
        super.removed();
        this.purchasePending = false;
    }

    public int getGuiLeftPos() {
        return this.leftPos;
    }

    public int getGuiTopPos() {
        return this.topPos;
    }
}
