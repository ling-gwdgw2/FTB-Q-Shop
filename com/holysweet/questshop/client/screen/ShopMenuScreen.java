package com.holysweet.questshop.client.screen;

import com.holysweet.questshop.QuestShop;
import com.holysweet.questshop.api.ShopCategory;
import com.holysweet.questshop.api.ShopEntry;
import com.holysweet.questshop.client.ClientCategories;
import com.holysweet.questshop.client.ClientCoins;
import com.holysweet.questshop.client.ClientFX;
import com.holysweet.questshop.client.ClientPurchases;
import com.holysweet.questshop.client.ClientShopData;
import com.holysweet.questshop.menu.ShopMenu;
import com.holysweet.questshop.network.payload.AdminRemoveEntryPayload;
import com.holysweet.questshop.network.payload.AdminUpdateEntryPayload;
import com.holysweet.questshop.network.payload.BuyEntryPayload;
import com.holysweet.questshop.network.payload.BuyResultPayload;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.logging.LogUtils;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ShopMenuScreen extends AbstractContainerScreen<ShopMenu> {

    public static final Logger LOGGER = LogUtils.getLogger();
    private ShopList list;
    private Button buyButton;
    private Button closeBtn;
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
        this.imageWidth = 560;
        this.imageHeight = 315;
    }

    // 16:9 responsive layout metrics
    public int getSidebarX() { return this.leftPos + 6; }
    public int getSidebarW() { return Math.max(85, (int) (this.imageWidth * 0.19)); }
    public int getSidebarH() { return this.imageHeight - 34; }

    public int getInspectorW() { return Math.max(125, (int) (this.imageWidth * 0.28)); }
    public int getInspectorX() { return this.leftPos + this.imageWidth - getInspectorW() - 6; }
    public int getInspectorH() { return this.imageHeight - 34; }

    public int getCatalogX() { return getSidebarX() + getSidebarW() + 6; }
    public int getCatalogW() { return getInspectorX() - getCatalogX() - 6; }
    public int getCatalogH() { return this.imageHeight - 34; }

    public int getHeaderH() { return 26; }
    public int getContentY() { return this.topPos + getHeaderH(); }

    @Override
    protected void init() {
        // Calculate 16:9 Responsive Clamped Dimensions
        int maxW = (int) (this.width * 0.94);
        int maxH = (int) (this.height * 0.90);
        if ((double) maxW / maxH > (16.0 / 9.0)) {
            this.imageHeight = maxH;
            this.imageWidth = (int) (maxH * (16.0 / 9.0));
        } else {
            this.imageWidth = maxW;
            this.imageHeight = (int) (maxW * (9.0 / 16.0));
        }

        // Safety bounds for small displays or high GUI scales
        this.imageWidth = Math.max(380, Math.min(this.imageWidth, this.width - 8));
        this.imageHeight = Math.max(215, Math.min(this.imageHeight, this.height - 8));

        this.leftPos = (this.width - this.imageWidth) / 2;
        this.topPos = (this.height - this.imageHeight) / 2;

        super.init();

        int contentY = getContentY();
        int catalogX = getCatalogX();
        int catalogW = getCatalogW();
        int inspectorX = getInspectorX();
        int inspectorW = getInspectorW();
        int inspectorH = getInspectorH();

        // 0. Close Button (Header Top-Right)
        int closeBtnX = this.leftPos + this.imageWidth - 22;
        this.closeBtn = Button.builder(Component.literal("✕"), b -> this.onClose())
                .bounds(closeBtnX, this.topPos + 5, 16, 16)
                .tooltip(Tooltip.create(Component.literal("Close (ESC)")))
                .build();
        this.addRenderableWidget(this.closeBtn);

        // 1. Column 1: Sidebar Category Buttons
        rebuildCategorySidebar();

        // 2. Column 2: Search Box
        int searchY = contentY + 2;
        this.searchBox = new EditBox(this.font, catalogX, searchY, catalogW, 16, Component.literal("Search"));
        this.searchBox.setHint(Component.literal("Search items..."));
        this.searchBox.setResponder(text -> refreshEntries());
        this.addRenderableWidget(this.searchBox);

        // 3. Column 2: Shop List Widget
        int listY = searchY + 18;
        int adminCatalogH = this.editMode ? 20 : 0;
        int listHeight = getCatalogH() - 22 - adminCatalogH;
        this.list = new ShopList(Minecraft.getInstance(), catalogW, listHeight, listY, 22);
        this.list.setX(catalogX);
        this.list.setOnSelectionChanged(this::updateButtonState);
        this.addRenderableWidget(this.list);

        // 4. Column 2: Admin Action Buttons (Browse Item, Add Hand)
        int adminY = contentY + getCatalogH() - 16;
        int halfW = (catalogW - 4) / 2;
        this.browseItemsBtn = Button.builder(
                Component.literal("+ Browse Items"),
                b -> openItemPicker()
        ).bounds(catalogX, adminY, halfW, 16).build();

        this.addHandItemBtn = Button.builder(
                Component.literal("+ Hand"),
                b -> addHandItemToShop()
        ).bounds(catalogX + halfW + 4, adminY, halfW, 16).build();

        this.addRenderableWidget(this.browseItemsBtn);
        this.addRenderableWidget(this.addHandItemBtn);

        // 5. Column 3: Inspector Panel - Purchase & Edit Controls
        int bottomH = this.editMode ? 66 : 46;
        int panelY = contentY + inspectorH - bottomH;

        // Quantity controls
        this.minusBtn = Button.builder(Component.literal("-"), b -> changeQty(-1))
                .bounds(inspectorX + 4, panelY, 14, 16).build();
        this.qtyBox = new EditBox(this.font, inspectorX + 20, panelY, 28, 16, Component.literal("Qty"));
        this.qtyBox.setValue("1");
        this.qtyBox.setHint(Component.literal("1"));
        this.qtyBox.setResponder(text -> updateButtonState());

        this.plusBtn = Button.builder(Component.literal("+"), b -> changeQty(1))
                .bounds(inspectorX + 50, panelY, 14, 16).build();
        this.stackBtn = Button.builder(Component.literal("x64"), b -> setQty(64))
                .bounds(inspectorX + 66, panelY, 24, 16).build();
        this.maxBtn = Button.builder(Component.literal("MAX"), b -> setMaxQty())
                .bounds(inspectorX + 92, panelY, 28, 16).build();

        this.addRenderableWidget(this.minusBtn);
        this.addRenderableWidget(this.qtyBox);
        this.addRenderableWidget(this.plusBtn);
        this.addRenderableWidget(this.stackBtn);
        this.addRenderableWidget(this.maxBtn);

        // Admin Edit Cost & Remove buttons
        int adminInspectY = panelY + 18;
        this.priceBox = new EditBox(this.font, inspectorX + 38, adminInspectY, 34, 16, Component.literal("Cost"));
        this.priceBox.setValue("10");
        this.priceBox.setHint(Component.literal("Cost"));
        this.priceBox.visible = false;

        this.editPriceBtn = Button.builder(
                Component.literal("Save"),
                b -> updateSelectedItem()
        ).bounds(inspectorX + 74, adminInspectY, 32, 16).build();

        this.removeBtn = Button.builder(
                Component.literal("Remove"),
                b -> removeSelectedItem()
        ).bounds(inspectorX + 108, adminInspectY, Math.max(40, inspectorW - 112), 16).build();

        this.addRenderableWidget(this.priceBox);
        this.addRenderableWidget(this.editPriceBtn);
        this.addRenderableWidget(this.removeBtn);

        // Buy Button
        int buyY = contentY + inspectorH - 22;
        this.buyButton = Button.builder(Component.literal("Buy Item"), this::buttonClick)
                .bounds(inspectorX + 4, buyY, inspectorW - 8, 20)
                .build();
        this.addRenderableWidget(this.buyButton);

        // Admin Mode Toggle Button in Header
        if (Minecraft.getInstance().player != null && (Minecraft.getInstance().player.isCreative() || Minecraft.getInstance().player.hasPermissions(2))) {
            this.editToggleBtn = Button.builder(
                Component.literal(this.editMode ? "[Edit: ON]" : "[Edit: OFF]"),
                b -> {
                    this.editMode = !this.editMode;
                    b.setMessage(Component.literal(this.editMode ? "[Edit: ON]" : "[Edit: OFF]"));
                    this.init(Minecraft.getInstance(), this.width, this.height);
                }
            ).bounds(this.leftPos + 120, this.topPos + 5, 65, 16).build();
            this.addRenderableWidget(this.editToggleBtn);
        }

        refreshEditUI();

        if (this.activePicker != null) {
            this.activePicker.init(this.leftPos, this.topPos, this.imageWidth, this.imageHeight);
        }

        if (this.activeCategoryModal != null) {
            this.activeCategoryModal.init(this.leftPos, this.topPos, this.imageWidth, this.imageHeight);
        }

        refreshEntries();
    }

    public void rebuildCategorySidebar() {
        for (Button btn : categoryButtons) {
            this.removeWidget(btn);
        }
        categoryButtons.clear();

        int catX = getSidebarX() + 3;
        int catY = getContentY() + 4;
        int catW = getSidebarW() - 6;
        int catH = 18;

        boolean allSelected = selectedCategory == null;
        int allBtnW = this.editMode ? catW - 24 : catW;
        Button allBtn = Button.builder(
            Component.literal((allSelected ? "> " : "") + "All Items"),
            b -> {
                selectedCategory = null;
                refreshEntries();
            }
        ).bounds(catX, catY, allBtnW, catH).build();
        categoryButtons.add(allBtn);
        this.addRenderableWidget(allBtn);

        if (this.editMode) {
            this.addCategoryBtn = Button.builder(
                Component.literal("+"),
                b -> openCategoryModal(null)
            ).bounds(catX + allBtnW + 2, catY, 22, catH).tooltip(Tooltip.create(Component.literal("Add New Category"))).build();
            this.addRenderableWidget(this.addCategoryBtn);
            categoryButtons.add(this.addCategoryBtn);
        }

        int currentY = catY + catH + 3;
        List<ShopCategory> catList = new ArrayList<>(ClientCategories.categories().values());
        int sidebarH = getSidebarH();
        int maxVisible = Math.max(1, (sidebarH - catH - 14) / 20);
        int maxScroll = Math.max(0, catList.size() - maxVisible);
        this.categoryScrollOffset = Math.max(0, Math.min(maxScroll, this.categoryScrollOffset));

        int startIndex = this.categoryScrollOffset;
        int endIndex = Math.min(catList.size(), startIndex + maxVisible);

        for (int i = startIndex; i < endIndex; i++) {
            ShopCategory cat = catList.get(i);
            ResourceLocation catId = cat.id();
            boolean isSelected = selectedCategory != null && selectedCategory.equals(catId);
            String label = (isSelected ? "> " : "") + cat.display();
            int btnW = this.editMode ? catW - 20 : catW;
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
            ).bounds(catX, currentY, btnW, catH).tooltip(Tooltip.create(Component.literal(tooltipText))).build();
            categoryButtons.add(catBtn);
            this.addRenderableWidget(catBtn);

            if (this.editMode) {
                Button editCatBtn = Button.builder(
                    Component.literal("E"),
                    b -> openCategoryModal(cat)
                ).bounds(catX + btnW + 2, currentY, 18, catH).tooltip(Tooltip.create(Component.literal("Edit: " + cat.display()))).build();
                categoryButtons.add(editCatBtn);
                this.addRenderableWidget(editCatBtn);
            }

            currentY += catH + 2;
        }
    }

    public void openItemPicker() {
        this.activePicker = new ShopItemPickerModal(this, this.selectedCategory);
        this.activePicker.init(this.leftPos, this.topPos, this.imageWidth, this.imageHeight);
    }

    public void closeItemPicker() {
        this.activePicker = null;
    }

    public void openCategoryModal(ShopCategory category) {
        this.activeCategoryModal = new CategoryEditModal(this, category);
        this.activeCategoryModal.init(this.leftPos, this.topPos, this.imageWidth, this.imageHeight);
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
    }

    public ShopEntry getSelectedEntry() {
        if (this.list != null && this.list.getSelected() != null) {
            ShopListEntry selected = (ShopListEntry) this.list.getSelected();
            if (selected != null) {
                return selected.data;
            }
        }
        return null;
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
        ShopEntry oldData = getSelectedEntry();
        if (oldData == null) return;

        int newAmount = getQuantity();
        int newCost = getPriceInput();

        PacketDistributor.sendToServer(new AdminUpdateEntryPayload(oldData.itemId(), newAmount, newCost, oldData.category(), oldData.dailyLimit(), oldData.totalStock()));
        ClientFX.purchaseOk(oldData.itemId(), newAmount, newCost);
    }

    private void removeSelectedItem() {
        ShopEntry oldData = getSelectedEntry();
        if (oldData == null) return;

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
        ShopEntry data = getSelectedEntry();
        if (data == null) return;
        
        int unitCost = data.effectiveCost();
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

        // Check if mouse is over Sidebar Column 1
        int sidebarX = getSidebarX();
        int sidebarW = getSidebarW();
        if (mouseX >= sidebarX && mouseX <= sidebarX + sidebarW && mouseY >= getContentY() && mouseY <= getContentY() + getSidebarH()) {
            int totalCats = ClientCategories.categories().size();
            int maxVisible = Math.max(1, (getSidebarH() - 18 - 14) / 20);
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
            this.renderBg(guiGraphics, partialTick, mouseX, mouseY);

            if (this.activeCategoryModal != null) {
                this.activeCategoryModal.render(guiGraphics, mouseX, mouseY, partialTick, this.leftPos, this.topPos, this.imageWidth, this.imageHeight);
            } else {
                this.activePicker.render(guiGraphics, mouseX, mouseY, partialTick, this.leftPos, this.topPos, this.imageWidth, this.imageHeight);
            }
        } else {
            super.render(guiGraphics, mouseX, mouseY, partialTick);
            renderInspectorDetails(guiGraphics, mouseX, mouseY, partialTick);
            this.renderTooltip(guiGraphics, mouseX, mouseY);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (this.activePicker != null || this.activeCategoryModal != null) return;

        // Title in Header
        guiGraphics.drawString(this.font, "§6§l✦ LING SHOP§r §7// 商店", 8, 8, 0xFFFFFF, false);

        // Primogems Coin Badge in Header
        String coinsStr = "💎 " + ClientCoins.get() + " G";
        int coinsW = this.font.width(coinsStr) + 12;
        int coinsX = this.imageWidth - 26 - coinsW;
        guiGraphics.fill(coinsX, 5, coinsX + coinsW, 21, 0x40D4AF37);
        guiGraphics.renderOutline(coinsX, 5, coinsW, 16, 0xFFFFD700);
        guiGraphics.drawString(this.font, coinsStr, coinsX + 6, 9, 0xFFFFEAA7, false);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        // 1. Full 16:9 Darkened Container Background
        guiGraphics.fill(this.leftPos, this.topPos, this.leftPos + this.imageWidth, this.topPos + this.imageHeight, 0xF00D0D15);
        guiGraphics.renderOutline(this.leftPos, this.topPos, this.imageWidth, this.imageHeight, 0xFF2A2A3E);

        // 2. Header Bar
        int headerH = getHeaderH();
        guiGraphics.fill(this.leftPos, this.topPos, this.leftPos + this.imageWidth, this.topPos + headerH, 0xF8151524);
        guiGraphics.fill(this.leftPos, this.topPos + headerH - 1, this.leftPos + this.imageWidth, this.topPos + headerH, 0xFF2E2E44);

        int contentY = getContentY();

        // 3. Column 1: Sidebar Background
        int sidebarX = getSidebarX();
        int sidebarW = getSidebarW();
        int sidebarH = getSidebarH();
        guiGraphics.fill(sidebarX, contentY, sidebarX + sidebarW, contentY + sidebarH, 0xF0131320);
        guiGraphics.renderOutline(sidebarX, contentY, sidebarW, sidebarH, 0xFF222234);

        // Sidebar Scrollbar indicator
        int totalCats = ClientCategories.categories().size();
        int maxVisible = Math.max(1, (sidebarH - 18 - 14) / 20);
        int maxScroll = Math.max(0, totalCats - maxVisible);
        int trackX = sidebarX + sidebarW - 4;
        int trackY = contentY + 24;
        int trackWidth = 2;
        int trackHeight = sidebarH - 28;

        guiGraphics.fill(trackX, trackY, trackX + trackWidth, trackY + trackHeight, 0xFF0B0B14);
        if (maxScroll > 0) {
            int thumbHeight = Math.max(10, (int) ((float) maxVisible / (totalCats + 1) * trackHeight));
            float scrollProgress = (float) this.categoryScrollOffset / maxScroll;
            int thumbY = trackY + (int) (scrollProgress * (trackHeight - thumbHeight));
            guiGraphics.fill(trackX, thumbY, trackX + trackWidth, thumbY + thumbHeight, 0xFFFFD700);
        } else {
            guiGraphics.fill(trackX, trackY, trackX + trackWidth, trackY + trackHeight, 0xFF282838);
        }

        // 4. Column 2: Catalog Background
        int catalogX = getCatalogX();
        int catalogW = getCatalogW();
        int catalogH = getCatalogH();
        guiGraphics.fill(catalogX, contentY, catalogX + catalogW, contentY + catalogH, 0xF010101A);
        guiGraphics.renderOutline(catalogX, contentY, catalogW, catalogH, 0xFF222234);

        // 5. Column 3: Inspector Panel Background
        int inspectorX = getInspectorX();
        int inspectorW = getInspectorW();
        int inspectorH = getInspectorH();
        guiGraphics.fill(inspectorX, contentY, inspectorX + inspectorW, contentY + inspectorH, 0xF0141424);
        guiGraphics.renderOutline(inspectorX, contentY, inspectorW, inspectorH, 0xFF2A2A42);

        // Render 3D Model showcase box in Inspector
        renderInspectorModelBox(guiGraphics, inspectorX, contentY, inspectorW, inspectorH, partialTick, mouseX, mouseY);
    }

    private void renderInspectorModelBox(GuiGraphics guiGraphics, int inspectorX, int inspectorY, int inspectorW, int inspectorH, float partialTick, int mouseX, int mouseY) {
        int boxX = inspectorX + 4;
        int boxY = inspectorY + 4;
        int boxW = inspectorW - 8;
        int boxH = Math.min(85, (int) (inspectorH * 0.32));

        // Showcase Box Background & Inner Glow
        guiGraphics.fillGradient(boxX, boxY, boxX + boxW, boxY + boxH, 0xFF18182A, 0xFF0E0E18);
        guiGraphics.renderOutline(boxX, boxY, boxW, boxH, 0xFF2C2C42);

        ShopEntry selectedEntry = getSelectedEntry();
        if (selectedEntry == null) {
            String emptyText = "Select item";
            guiGraphics.drawString(this.font, emptyText, boxX + (boxW - this.font.width(emptyText)) / 2, boxY + boxH / 2 - 4, 0xFF666677, false);
            return;
        }

        Optional<Item> itemOpt = BuiltInRegistries.ITEM.getOptional(selectedEntry.itemId());
        if (itemOpt.isEmpty()) return;
        ItemStack previewStack = new ItemStack(itemOpt.get(), selectedEntry.amount());

        // Pedestal Holographic Glow
        int pedestalCenterX = boxX + boxW / 2;
        int pedestalCenterY = boxY + boxH - 12;
        int ringRx = Math.min(30, boxW / 3);
        guiGraphics.fill(pedestalCenterX - ringRx, pedestalCenterY, pedestalCenterX + ringRx, pedestalCenterY + 2, 0xFFFFB703);
        guiGraphics.fill(pedestalCenterX - ringRx + 4, pedestalCenterY - 1, pedestalCenterX + ringRx - 4, pedestalCenterY + 3, 0x88FFD166);

        // 3D Model Rotation & Bobbing
        float time = (System.currentTimeMillis() % 3600000L) / 1000.0F;
        float rotationAngle = (time * 45.0F) % 360.0F;
        float bobbing = (float) Math.sin(time * 2.2F) * 2.5F;
        float modelScale = Math.min(36.0F, Math.min(boxW * 0.38F, boxH * 0.44F));

        PoseStack pose = guiGraphics.pose();
        pose.pushPose();
        pose.translate(pedestalCenterX, pedestalCenterY - 16 + bobbing, 150.0F);
        pose.scale(modelScale, -modelScale, modelScale);
        pose.mulPose(Axis.XP.rotationDegrees(16.0F));
        pose.mulPose(Axis.YP.rotationDegrees(rotationAngle));

        Minecraft.getInstance().getItemRenderer().renderStatic(
                previewStack,
                ItemDisplayContext.FIXED,
                15728880,
                OverlayTexture.NO_OVERLAY,
                pose,
                guiGraphics.bufferSource(),
                Minecraft.getInstance().level,
                0
        );
        guiGraphics.flush();
        pose.popPose();
    }

    private void renderInspectorDetails(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        ShopEntry selectedEntry = getSelectedEntry();
        if (selectedEntry == null) return;

        Optional<Item> itemOpt = BuiltInRegistries.ITEM.getOptional(selectedEntry.itemId());
        if (itemOpt.isEmpty()) return;
        ItemStack stack = new ItemStack(itemOpt.get(), selectedEntry.amount());

        int inspectorX = getInspectorX();
        int inspectorY = getContentY();
        int inspectorW = getInspectorW();
        int inspectorH = getInspectorH();
        int boxH = Math.min(85, (int) (inspectorH * 0.32));

        int infoY = inspectorY + boxH + 8;

        // 1. Item Name (Bold Gold)
        Component name = stack.getItem().getName(stack);
        String nameStr = name.getString();
        int maxW = inspectorW - 12;
        if (this.font.width(nameStr) > maxW) {
            while (nameStr.length() > 3 && this.font.width(nameStr + "..") > maxW) {
                nameStr = nameStr.substring(0, nameStr.length() - 1);
            }
            nameStr = nameStr + "..";
        }
        guiGraphics.drawString(this.font, nameStr, inspectorX + 6, infoY, 0xFFFFD700, false);
        infoY += 12;

        // 2. Mod and Item ID
        String modId = selectedEntry.itemId().getNamespace();
        String itemPath = selectedEntry.itemId().getPath();
        String idStr = "§8" + modId + ":" + (itemPath.length() > 14 ? itemPath.substring(0, 12) + ".." : itemPath);
        guiGraphics.drawString(this.font, idStr, inspectorX + 6, infoY, 0xFFAAAAAA, false);
        infoY += 11;

        // 3. Badges (Discount / Daily Limit / Stock)
        int badgeX = inspectorX + 6;
        if (selectedEntry.hasDiscount()) {
            String dStr = "-" + selectedEntry.discountPercent() + "%";
            int bw = this.font.width(dStr) + 4;
            guiGraphics.fill(badgeX, infoY, badgeX + bw, infoY + 11, 0xFFCC2222);
            guiGraphics.drawString(this.font, dStr, badgeX + 2, infoY + 2, 0xFFFFFFFF, false);
            badgeX += bw + 3;
        }
        if (selectedEntry.hasDailyLimit()) {
            String lStr = "1/Day";
            int bw = this.font.width(lStr) + 4;
            guiGraphics.fill(badgeX, infoY, badgeX + bw, infoY + 11, 0xFF1E88E5);
            guiGraphics.drawString(this.font, lStr, badgeX + 2, infoY + 2, 0xFFFFFFFF, false);
            badgeX += bw + 3;
        }
        if (selectedEntry.hasTotalStock()) {
            String sStr = "Stock: " + selectedEntry.totalStock();
            int bw = this.font.width(sStr) + 4;
            guiGraphics.fill(badgeX, infoY, badgeX + bw, infoY + 11, 0xFF7B2CBF);
            guiGraphics.drawString(this.font, sStr, badgeX + 2, infoY + 2, 0xFFFFFFFF, false);
            badgeX += bw + 3;
        }
        if (badgeX > inspectorX + 6) {
            infoY += 14;
        }

        // 4. Subtle separator line
        guiGraphics.fill(inspectorX + 6, infoY, inspectorX + inspectorW - 6, infoY + 1, 0xFF2A2A3E);
        infoY += 4;

        // 5. Total Price & Amount summary (above purchase buttons)
        int bottomH = this.editMode ? 66 : 46;
        int purchaseY = inspectorY + inspectorH - bottomH;
        int costY = purchaseY - 14;
        int qty = getQuantity();
        int totalCost = selectedEntry.effectiveCost() * qty;
        int totalItems = selectedEntry.amount() * qty;
        String costStr = "§7Total: §e" + totalCost + " Gems §8(" + totalItems + "x)";
        guiGraphics.drawString(this.font, costStr, inspectorX + 6, costY, 0xFFFFFFFF, false);

        if (this.editMode) {
            guiGraphics.drawString(this.font, "§7Cost:", inspectorX + 6, purchaseY + 22, 0xFFAAAAAA, false);
        }

        // Hover over 3D model box shows full tooltip
        int boxX = inspectorX + 4;
        int boxY = inspectorY + 4;
        int boxW = inspectorW - 8;
        if (mouseX >= boxX && mouseX <= boxX + boxW && mouseY >= boxY && mouseY <= boxY + boxH) {
            guiGraphics.renderTooltip(this.font, stack, mouseX, mouseY);
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
        ShopEntry selected = getSelectedEntry();
        if (selected != null && selected.itemId().equals(itemId)) {
            ClientPurchases.recordPurchase(selected.category(), itemId, amount);
        }
        updateButtonState();
        ClientFX.purchaseOk(itemId, amount, cost);
    }

    public void updateButtonState() {
        if (this.buyButton == null) return;
        boolean canBuy = false;
        int qty = getQuantity();
        ShopEntry data = getSelectedEntry();

        if (!this.purchasePending && data != null) {
            int totalCost = data.effectiveCost() * qty;
            int totalItems = data.amount() * qty;

            if (ClientPurchases.isSoldOut(data)) {
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

            if (this.priceBox != null && this.priceBox.visible && !this.priceBox.isFocused()) {
                this.priceBox.setValue(String.valueOf(data.cost()));
            }
        } else {
            this.buyButton.setMessage(Component.literal("Select Item"));
        }
        this.buyButton.active = canBuy;
    }

    private void buttonClick(Button btn) {
        if (this.purchasePending) return;
        this.purchasePending = true;
        btn.active = false;
        ShopEntry data = getSelectedEntry();
        if (data == null) {
            this.purchasePending = false;
            updateButtonState();
            return;
        }
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
