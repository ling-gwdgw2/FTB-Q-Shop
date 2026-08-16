package com.holysweet.questshop.integrations.ftbteams;

import com.holysweet.questshop.QuestShop;
import com.holysweet.questshop.api.categories.CategorySetting;
import dev.ftb.mods.ftbteams.api.Team;
import dev.ftb.mods.ftbteams.api.event.PlayerJoinedPartyTeamEvent;
import dev.ftb.mods.ftbteams.api.event.PlayerLeftPartyTeamEvent;
import dev.ftb.mods.ftbteams.api.property.StringProperty;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public final class TeamCategories {
    public static final StringProperty CATEGORY_SETTINGS = new StringProperty(ResourceLocation.fromNamespaceAndPath(QuestShop.MODID, "category_settings"), "");

    private TeamCategories() {}

    private static Map<ResourceLocation, CategorySetting> parse(Team team) {
        Map<ResourceLocation, CategorySetting> map = new HashMap<>();
        if (team == null) return map;
        String raw = team.getProperty(CATEGORY_SETTINGS);
        if (raw == null || raw.isEmpty()) return map;

        for (String entry : raw.split(";")) {
            String[] parts = entry.split("=");
            if (parts.length == 2) {
                ResourceLocation id = ResourceLocation.tryParse(parts[0]);
                if (id != null) {
                    try {
                        map.put(id, CategorySetting.valueOf(parts[1]));
                    } catch (IllegalArgumentException ignored) {}
                }
            }
        }
        return map;
    }

    private static void write(Team team, Map<ResourceLocation, CategorySetting> map) {
        if (team == null) return;
        String serialized = map.entrySet().stream()
                .map(e -> e.getKey().toString() + "=" + e.getValue().name())
                .collect(Collectors.joining(";"));
        team.setProperty(CATEGORY_SETTINGS, serialized);
    }

    public static CategorySetting getSetting(Team team, ResourceLocation category) {
        return parse(team).getOrDefault(category, CategorySetting.DEFAULT);
    }

    public static boolean setSetting(Team team, ResourceLocation category, CategorySetting setting) {
        if (team == null || category == null) return false;
        Map<ResourceLocation, CategorySetting> map = parse(team);
        if (setting == CategorySetting.DEFAULT) {
            map.remove(category);
        } else {
            map.put(category, setting);
        }
        write(team, map);
        return true;
    }

    public static void PlayerJoinedPartyTeamEvent(PlayerJoinedPartyTeamEvent event) {
        Team personal = event.getPreviousTeam();
        Team party = event.getTeam();
        if (personal != null && party != null) {
            Map<ResourceLocation, CategorySetting> personalMap = parse(personal);
            Map<ResourceLocation, CategorySetting> partyMap = parse(party);
            for (Map.Entry<ResourceLocation, CategorySetting> e : personalMap.entrySet()) {
                if (e.getValue() == CategorySetting.UNLOCKED) {
                    partyMap.put(e.getKey(), CategorySetting.UNLOCKED);
                }
            }
            write(party, partyMap);
        }
    }

    public static void PlayerLeftPartyTeamEvent(PlayerLeftPartyTeamEvent event) {
        // Player leaves party
    }
}
