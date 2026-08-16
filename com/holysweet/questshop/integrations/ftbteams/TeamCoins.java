package com.holysweet.questshop.integrations.ftbteams;

import com.holysweet.questshop.QuestShop;
import dev.ftb.mods.ftbteams.api.Team;
import dev.ftb.mods.ftbteams.api.event.PlayerJoinedPartyTeamEvent;
import dev.ftb.mods.ftbteams.api.event.PlayerLeftPartyTeamEvent;
import dev.ftb.mods.ftbteams.api.property.IntProperty;
import net.minecraft.resources.ResourceLocation;

public final class TeamCoins {
    public static final IntProperty COINS = new IntProperty(ResourceLocation.fromNamespaceAndPath(QuestShop.MODID, "coins"), 0);

    private TeamCoins() {}

    public static int get(Team team) {
        if (team == null) return 0;
        return Math.max(0, team.getProperty(COINS));
    }

    public static void set(Team team, int amount) {
        if (team == null) return;
        team.setProperty(COINS, Math.max(0, amount));
    }

    public static void add(Team team, int delta) {
        if (team == null) return;
        set(team, get(team) + delta);
    }

    public static void PlayerJoinedPartyTeamEvent(PlayerJoinedPartyTeamEvent event) {
        Team personal = event.getPreviousTeam();
        Team party = event.getTeam();
        if (personal != null && party != null) {
            int coins = get(personal);
            if (coins > 0) {
                add(party, coins);
                set(personal, 0);
            }
        }
    }

    public static void PlayerLeftPartyTeamEvent(PlayerLeftPartyTeamEvent event) {
        // Player leaves party
    }
}
