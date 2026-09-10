package com.holysweet.questshop.client.screen;

import com.holysweet.questshop.client.ClientTeamData;
import com.holysweet.questshop.network.payload.RequestTeamMembersPayload;
import com.holysweet.questshop.network.payload.TeamMembersPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;

public class TeamMembersModal {

    private final ShopMenuScreen parent;
    private Button closeBtn;
    private Button refreshBtn;
    private int scrollOffset = 0;

    public TeamMembersModal(ShopMenuScreen parent) {
        this.parent = parent;
    }

    public void init(int leftPos, int topPos, int imageWidth, int imageHeight) {
        int modalWidth = 270;
        int modalHeight = 195;
        int modalX = leftPos + (imageWidth - modalWidth) / 2;
        int modalY = topPos + (imageHeight - modalHeight) / 2;

        // Close button at top right
        this.closeBtn = Button.builder(Component.literal("✕"), b -> parent.closeTeamModal())
                .bounds(modalX + modalWidth - 22, modalY + 6, 16, 16)
                .build();

        // Refresh button at bottom
        this.refreshBtn = Button.builder(Component.literal("↻ Refresh"), b -> {
            PacketDistributor.sendToServer(new RequestTeamMembersPayload());
        }).bounds(modalX + modalWidth - 85, modalY + modalHeight - 24, 70, 16).build();

        // Automatically request latest team stats when opening
        PacketDistributor.sendToServer(new RequestTeamMembersPayload());
    }

    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, int leftPos, int topPos, int imageWidth, int imageHeight) {
        int modalWidth = 270;
        int modalHeight = 195;
        int modalX = leftPos + (imageWidth - modalWidth) / 2;
        int modalY = topPos + (imageHeight - modalHeight) / 2;

        Font font = Minecraft.getInstance().font;

        // 1. Dark overlay covering full window
        guiGraphics.fill(0, 0, Minecraft.getInstance().getWindow().getGuiScaledWidth(), Minecraft.getInstance().getWindow().getGuiScaledHeight(), 0xCC000000);

        // 2. Modal Card Frame
        guiGraphics.fill(modalX, modalY, modalX + modalWidth, modalY + modalHeight, 0xF012121C);
        guiGraphics.renderOutline(modalX, modalY, modalWidth, modalHeight, 0xFF2E2E44);

        // 3. Header Accent Bar
        guiGraphics.fill(modalX, modalY, modalX + modalWidth, modalY + 26, 0xF818182A);
        guiGraphics.fill(modalX, modalY + 25, modalX + modalWidth, modalY + 26, 0xFFFFD700);

        TeamMembersPayload data = ClientTeamData.get();
        String titleStr = data.hasTeam() ? ("👥 Team: " + data.teamName()) : "👤 Personal Account";
        guiGraphics.drawString(font, "§6" + titleStr, modalX + 10, modalY + 9, 0xFFFFFF, false);

        if (this.closeBtn != null) this.closeBtn.render(guiGraphics, mouseX, mouseY, partialTick);

        // 4. Team Total Pot Banner
        int bannerY = modalY + 32;
        guiGraphics.fill(modalX + 10, bannerY, modalX + modalWidth - 10, bannerY + 22, 0x30D4AF37);
        guiGraphics.renderOutline(modalX + 10, bannerY, modalWidth - 20, 22, 0x60FFD700);

        String potStr = "💎 Total Team Pot: §e" + data.totalPot() + " Gems";
        guiGraphics.drawString(font, potStr, modalX + 16, bannerY + 4, 0xFFFFA000, false);
        guiGraphics.drawString(font, "§7Shared balance — all members can use to shop", modalX + 16, bannerY + 13, 0xFFAAAAAA, false);

        // 5. Members List
        List<TeamMembersPayload.MemberEntry> members = data.members();
        int listY = bannerY + 26;
        int listH = modalHeight - 88;
        int visibleRows = 3;
        int rowH = 32;

        if (members == null || members.isEmpty()) {
            String emptyText = "§7No team members found.";
            guiGraphics.drawString(font, emptyText, modalX + (modalWidth - font.width(emptyText)) / 2, listY + 20, 0x888888, false);
        } else {
            int maxScroll = Math.max(0, members.size() - visibleRows);
            this.scrollOffset = Math.max(0, Math.min(maxScroll, this.scrollOffset));

            int totalEarned = members.stream().mapToInt(TeamMembersPayload.MemberEntry::earned).sum();

            int startIndex = this.scrollOffset;
            int endIndex = Math.min(members.size(), startIndex + visibleRows);

            for (int i = startIndex; i < endIndex; i++) {
                TeamMembersPayload.MemberEntry m = members.get(i);
                int rY = listY + (i - startIndex) * (rowH + 2);

                // Row Background
                boolean hovered = mouseX >= modalX + 10 && mouseX <= modalX + modalWidth - 10 && mouseY >= rY && mouseY < rY + rowH;
                int rowBg = hovered ? 0x402A2A44 : 0x251C1C2A;
                guiGraphics.fill(modalX + 10, rY, modalX + modalWidth - 10, rY + rowH, rowBg);
                guiGraphics.renderOutline(modalX + 10, rY, modalWidth - 20, rowH, 0x30444466);

                // Online indicator + Name + Role
                String onlineDot = m.isOnline() ? "§a●" : "§7○";
                String roleTag = m.isOwner() ? " §6★" : "";
                String nameStr = onlineDot + " §f" + m.name() + roleTag;
                guiGraphics.drawString(font, nameStr, modalX + 16, rY + 4, 0xFFFFFF, false);

                // Earned and Spent stats on top right of row
                String statsStr = "§a+" + m.earned() + " G §7| §c-" + m.spent() + " G";
                int statsW = font.width(statsStr);
                guiGraphics.drawString(font, statsStr, modalX + modalWidth - 16 - statsW, rY + 4, 0xFFFFFF, false);

                // Contribution Progress Bar
                int barX = modalX + 16;
                int barY = rY + 17;
                int barW = modalWidth - 65;
                int barH = 7;
                int pct = totalEarned > 0 ? (int) Math.round(((double) m.earned() / totalEarned) * 100) : 0;
                int fillW = (int) ((pct / 100.0F) * barW);

                // Track & Fill
                guiGraphics.fill(barX, barY, barX + barW, barY + barH, 0xFF141420);
                if (fillW > 0) {
                    guiGraphics.fill(barX, barY, barX + fillW, barY + barH, 0xFFFFD700);
                }
                guiGraphics.renderOutline(barX, barY, barW, barH, 0xFF333348);

                // Percentage label
                String pctStr = pct + "%";
                guiGraphics.drawString(font, pctStr, barX + barW + 5, barY - 1, 0xFFFFEAA7, false);
            }

            // Scroll indicator if more members exist
            if (maxScroll > 0) {
                String scrollHint = "§8[Scroll: " + (this.scrollOffset + 1) + "-" + Math.min(members.size(), this.scrollOffset + visibleRows) + "/" + members.size() + "]";
                guiGraphics.drawString(font, scrollHint, modalX + 12, modalY + modalHeight - 19, 0x666677, false);
            }
        }

        // Footer buttons
        if (this.refreshBtn != null) this.refreshBtn.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        TeamMembersPayload data = ClientTeamData.get();
        if (data.members() != null) {
            int maxScroll = Math.max(0, data.members().size() - 3);
            if (maxScroll > 0) {
                if (scrollY > 0) {
                    this.scrollOffset = Math.max(0, this.scrollOffset - 1);
                    return true;
                } else if (scrollY < 0) {
                    this.scrollOffset = Math.min(maxScroll, this.scrollOffset + 1);
                    return true;
                }
            }
        }
        return false;
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.closeBtn != null && this.closeBtn.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        if (this.refreshBtn != null && this.refreshBtn.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        return true; // Absorb click inside modal overlay
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) { // ESC key
            parent.closeTeamModal();
            return true;
        }
        return false;
    }
}
