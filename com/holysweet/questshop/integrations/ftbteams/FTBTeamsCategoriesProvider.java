package com.holysweet.questshop.integrations.ftbteams;

import com.holysweet.questshop.api.categories.CategoriesProvider;
import com.holysweet.questshop.api.categories.CategorySetting;
import dev.ftb.mods.ftbteams.api.FTBTeamsAPI;
import dev.ftb.mods.ftbteams.api.Team;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.Optional;

public final class FTBTeamsCategoriesProvider implements CategoriesProvider {

    @Override
    public String id() {
        return "ftbteams";
    }

    private static Team teamFor(ServerPlayer player) {
        if (player == null) return null;
        Optional<Team> teamOpt = FTBTeamsAPI.api().getManager().getTeamForPlayer(player);
        return teamOpt.orElse(null);
    }

    @Override
    public CategorySetting getSetting(ServerPlayer player, ResourceLocation category) {
        Team team = teamFor(player);
        return TeamCategories.getSetting(team, category);
    }

    @Override
    public boolean setSetting(ServerPlayer player, ResourceLocation category, CategorySetting setting) {
        Team team = teamFor(player);
        return TeamCategories.setSetting(team, category, setting);
    }
}
