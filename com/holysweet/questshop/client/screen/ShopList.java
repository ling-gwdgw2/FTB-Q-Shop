package com.holysweet.questshop.client.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;

import java.util.List;

public class ShopList extends ObjectSelectionList<ShopListEntry> {

    private Runnable onSelectionChanged;

    public ShopList(Minecraft minecraft, int width, int height, int top, int bottom) {
        super(minecraft, width, height, top, 20);
    }

    public void setPosition(int x, int y) {
        super.setPosition(x, y);
    }

    public void setEntries(List<ShopListEntry> entries) {
        ShopListEntry previous = this.getSelected();
        this.clearEntries();
        ShopListEntry toSelect = null;
        for (ShopListEntry entry : entries) {
            entry.setParent(this);
            this.addEntry(entry);
            if (previous != null && previous.data != null && previous.data.itemId().equals(entry.data.itemId())) {
                toSelect = entry;
            }
        }
        if (toSelect != null) {
            this.setSelected(toSelect);
        } else if (!entries.isEmpty() && this.getSelected() == null) {
            this.setSelected(entries.get(0));
        }
    }

    public void setOnSelectionChanged(Runnable onSelectionChanged) {
        this.onSelectionChanged = onSelectionChanged;
    }

    @Override
    public void setSelected(ShopListEntry selected) {
        super.setSelected(selected);
        if (this.onSelectionChanged != null) {
            this.onSelectionChanged.run();
        }
    }

    @Override
    public int getRowWidth() {
        return this.width - 10;
    }

    @Override
    protected int getScrollbarPosition() {
        return this.getX() + this.width - 6;
    }
}
