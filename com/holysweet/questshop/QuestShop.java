package com.holysweet.questshop;

import com.holysweet.questshop.client.ClientInit;
import com.holysweet.questshop.data.ShopCatalog;
import com.holysweet.questshop.integrations.IntegrationBootstrap;
import com.holysweet.questshop.item.ModItems;
import com.holysweet.questshop.loot.conditions.LootConditions;
import com.holysweet.questshop.loot.ModLootModifiers;
import com.holysweet.questshop.registry.ModMenuTypes;
import com.mojang.logging.LogUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import org.slf4j.Logger;

@Mod(QuestShop.MODID)
public class QuestShop {
    public static final String MODID = "ling_q_shop";
    public static final Logger LOGGER = LogUtils.getLogger();
    private static final ResourceKey<CreativeModeTab> TOOLS_TAB = ResourceKey.create(Registries.CREATIVE_MODE_TAB, ResourceLocation.withDefaultNamespace("tools_and_utilities"));

    public QuestShop(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::addCreative);

        if (FMLEnvironment.dist.isClient()) {
            modEventBus.addListener(ClientInit::onRegisterScreens);
        }

        modEventBus.addListener(com.holysweet.questshop.network.NetworkHandler::registerPayloads);

        ModMenuTypes.MENU_TYPES.register(modEventBus);
        ModItems.register(modEventBus);
        LootConditions.CONDITIONS.register(modEventBus);
        ModLootModifiers.register(modEventBus);

        IntegrationBootstrap.bootstrap();
        NeoForge.EVENT_BUS.register(this);
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        LOGGER.info("FtbQshop initialized successfully!");
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == TOOLS_TAB) {
            event.accept(ModItems.COIN);
        }
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("[FtbQshop] Loading shop categories and entries from config disk...");
        ShopCatalog.INSTANCE.loadFromDisk(event.getServer());
        com.holysweet.questshop.service.DailyDealsService.checkAndRefreshDeals(event.getServer());
    }
}
