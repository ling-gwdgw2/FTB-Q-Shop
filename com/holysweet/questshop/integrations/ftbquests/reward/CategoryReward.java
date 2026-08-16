package com.holysweet.questshop.integrations.ftbquests.reward;

import com.holysweet.questshop.QuestShop;
import com.holysweet.questshop.data.ShopCatalog;
import com.holysweet.questshop.service.CategoriesService;
import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.icon.Icons;
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

import java.util.List;

public class CategoryReward extends Reward {
    public static final RewardType TYPE = RewardTypes.register(
            ResourceLocation.fromNamespaceAndPath(QuestShop.MODID, "category"),
            CategoryReward::new,
            () -> Icons.SHIELD
    );

    private ResourceLocation category = ResourceLocation.fromNamespaceAndPath(QuestShop.MODID, "general");
    private boolean unlock = true;

    public CategoryReward(long id, Quest quest) {
        super(id, quest);
    }

    @Override
    public RewardType getType() {
        return TYPE;
    }

    @Override
    public void writeData(CompoundTag tag, HolderLookup.Provider provider) {
        super.writeData(tag, provider);
        tag.putString("category", this.category.toString());
        tag.putBoolean("unlock", this.unlock);
    }

    @Override
    public void readData(CompoundTag tag, HolderLookup.Provider provider) {
        super.readData(tag, provider);
        this.category = ResourceLocation.parse(tag.getString("category"));
        this.unlock = !tag.contains("unlock") || tag.getBoolean("unlock");
    }

    @Override
    public void writeNetData(RegistryFriendlyByteBuf buffer) {
        super.writeNetData(buffer);
        buffer.writeResourceLocation(this.category);
        buffer.writeBoolean(this.unlock);
    }

    @Override
    public void readNetData(RegistryFriendlyByteBuf buffer) {
        super.readNetData(buffer);
        this.category = buffer.readResourceLocation();
        this.unlock = buffer.readBoolean();
    }

    @Override
    public void fillConfigGroup(ConfigGroup config) {
        super.fillConfigGroup(config);
        List<ResourceLocation> categories = ShopCatalog.INSTANCE.categories().keySet().stream().toList();
        config.addEnum("category", this.category, v -> this.category = v, dev.ftb.mods.ftblibrary.config.NameMap.of(this.category, categories).create());
        config.addBool("unlock", this.unlock, v -> this.unlock = v, true);
    }

    @Override
    public void claim(ServerPlayer player, boolean notify) {
        CategoriesService.setUnlocked(player, this.category, this.unlock);
    }

    @Override
    public Component getAltTitle() {
        String key = this.unlock ? "reward.ling_q_shop.category.title.unlock" : "reward.ling_q_shop.category.title.lock";
        return Component.translatable(key, this.category.toString());
    }

    @Override
    public Icon getAltIcon() {
        return Icons.SHIELD;
    }

    public static void bootstrap() {}
}
