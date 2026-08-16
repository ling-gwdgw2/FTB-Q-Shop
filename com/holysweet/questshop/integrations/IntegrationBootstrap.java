package com.holysweet.questshop.integrations;

import com.holysweet.questshop.integrations.ftbquests.FTBQuestsIntegration;
import com.holysweet.questshop.integrations.ftbteams.FTBTeamsIntegration;
import com.mojang.logging.LogUtils;
import net.neoforged.fml.ModList;
import org.slf4j.Logger;

public final class IntegrationBootstrap {
    private static final Logger LOGGER = LogUtils.getLogger();

    private IntegrationBootstrap() {}

    public static void bootstrap() {
        init();
    }

    public static void init() {
        if (ModList.get().isLoaded("ftbteams")) {
            LOGGER.info("FTB Teams found. Enabling FTB Teams integration.");
            FTBTeamsIntegration.bootstrap();
        } else {
            LOGGER.info("FTB Teams not found. Using Vanilla accounts.");
        }

        if (ModList.get().isLoaded("ftbquests")) {
            LOGGER.info("FTB Quests found. Enabling FTB Quests rewards.");
            FTBQuestsIntegration.bootstrap();
        }
    }
}
