package com.holysweet.questshop.integrations.ftbquests;

import com.holysweet.questshop.integrations.ftbquests.reward.CategoryReward;
import com.holysweet.questshop.integrations.ftbquests.reward.CoinsReward;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

public final class FTBQuestsIntegration {
    private static final Logger LOGGER = LogUtils.getLogger();

    private FTBQuestsIntegration() {}

    public static void bootstrap() {
        LOGGER.info("Registering FTB Quests reward types...");
        CoinsReward.bootstrap();
        CategoryReward.bootstrap();
    }
}
