package com.holysweet.questshop.integrations.ftbquests.reward;

import com.holysweet.questshop.QuestShop;
import com.holysweet.questshop.item.ModItems;
import com.holysweet.questshop.service.CoinsService;
import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.icon.ItemIcon;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.reward.Reward;
import dev.ftb.mods.ftbquests.quest.reward.RewardType;
import dev.ftb.mods.ftbquests.quest.reward.RewardTypes;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class CoinsReward extends Reward {
    public static final RewardType TYPE = RewardTypes.register(
            ResourceLocation.fromNamespaceAndPath(QuestShop.MODID, "coins"),
            CoinsReward::new,
            () -> ItemIcon.getItemIcon(ModItems.COIN != null && ModItems.COIN.get() != null ? new ItemStack(ModItems.COIN.get()) : ItemStack.EMPTY)
    );

    private int amount = 10;

    public CoinsReward(long id, Quest quest) {
        super(id, quest);
    }

    @Override
    public RewardType getType() {
        return TYPE;
    }

    @Override
    public void writeData(CompoundTag tag, HolderLookup.Provider provider) {
        super.writeData(tag, provider);
        tag.putInt("amount", this.amount);
    }

    @Override
    public void readData(CompoundTag tag, HolderLookup.Provider provider) {
        super.readData(tag, provider);
        this.amount = tag.getInt("amount");
    }

    @Override
    public void writeNetData(RegistryFriendlyByteBuf buffer) {
        super.writeNetData(buffer);
        buffer.writeVarInt(this.amount);
    }

    @Override
    public void readNetData(RegistryFriendlyByteBuf buffer) {
        super.readNetData(buffer);
        this.amount = buffer.readVarInt();
    }

    @Override
    public void fillConfigGroup(ConfigGroup config) {
        super.fillConfigGroup(config);
        config.addInt("amount", this.amount, v -> this.amount = v, 1, 1, Integer.MAX_VALUE);
    }

    @Override
    public void claim(ServerPlayer player, boolean notify) {
        CoinsService.add(player.serverLevel(), player, this.amount);
    }

    @Override
    public Component getAltTitle() {
        return Component.translatable("reward.ling_q_shop.coins.title", this.amount);
    }

    @Override
    public Icon getAltIcon() {
        return ItemIcon.getItemIcon(ModItems.COIN != null && ModItems.COIN.get() != null ? new ItemStack(ModItems.COIN.get()) : ItemStack.EMPTY);
    }

    public static void bootstrap() {}
}
