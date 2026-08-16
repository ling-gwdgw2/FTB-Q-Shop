package com.holysweet.questshop.integrations.ftbteams;

import com.holysweet.questshop.service.CategoriesService;
import com.holysweet.questshop.service.CoinsService;
import dev.ftb.mods.ftbteams.api.event.TeamCollectPropertiesEvent;
import dev.ftb.mods.ftbteams.api.event.TeamEvent;

public final class FTBTeamsIntegration {

    private FTBTeamsIntegration() {}

    public static void bootstrap() {
        CoinsService.swapBackend(new FTBTeamsCoinsProvider());
        CategoriesService.swapProvider(new FTBTeamsCategoriesProvider());

        TeamEvent.COLLECT_PROPERTIES.register(FTBTeamsIntegration::onCollectProperties);
        TeamEvent.PLAYER_JOINED_PARTY.register(TeamCoins::PlayerJoinedPartyTeamEvent);
        TeamEvent.PLAYER_JOINED_PARTY.register(TeamCategories::PlayerJoinedPartyTeamEvent);
        TeamEvent.PLAYER_LEFT_PARTY.register(TeamCoins::PlayerLeftPartyTeamEvent);
        TeamEvent.PLAYER_LEFT_PARTY.register(TeamCategories::PlayerLeftPartyTeamEvent);
    }

    private static void onCollectProperties(TeamCollectPropertiesEvent event) {
        event.add(TeamCoins.COINS);
        event.add(TeamCategories.CATEGORY_SETTINGS);
    }
}
