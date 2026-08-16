package com.holysweet.questshop.integrations.ftbteams;

import com.holysweet.questshop.api.coins.AccountRef;
import com.holysweet.questshop.api.coins.CoinsProvider;
import dev.ftb.mods.ftbteams.api.FTBTeamsAPI;
import dev.ftb.mods.ftbteams.api.Team;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.Optional;
import java.util.UUID;

public final class FTBTeamsCoinsProvider implements CoinsProvider {

    @Override
    public String id() {
        return "ftbteams";
    }

    @Override
    public AccountRef handleFor(ServerPlayer player) {
        Optional<Team> teamOpt = FTBTeamsAPI.api().getManager().getTeamForPlayer(player);
        return teamOpt.map(t -> (AccountRef) new AccountRef.Team(t.getId()))
                .orElseGet(() -> new AccountRef.Player(player.getUUID()));
    }

    @Override
    public int get(ServerLevel level, AccountRef ref) {
        if (ref instanceof AccountRef.Team t) {
            Team team = FTBTeamsAPI.api().getManager().getTeamByID(t.teamId()).orElse(null);
            return TeamCoins.get(team);
        } else if (ref instanceof AccountRef.Player p) {
            Team team = FTBTeamsAPI.api().getManager().getTeamByID(p.uuid()).orElse(null);
            return TeamCoins.get(team);
        }
        return 0;
    }

    @Override
    public int set(ServerLevel level, AccountRef ref, int amount) {
        if (ref instanceof AccountRef.Team t) {
            Team team = FTBTeamsAPI.api().getManager().getTeamByID(t.teamId()).orElse(null);
            TeamCoins.set(team, amount);
            return TeamCoins.get(team);
        } else if (ref instanceof AccountRef.Player p) {
            Team team = FTBTeamsAPI.api().getManager().getTeamByID(p.uuid()).orElse(null);
            TeamCoins.set(team, amount);
            return TeamCoins.get(team);
        }
        return 0;
    }

    @Override
    public int add(ServerLevel level, AccountRef ref, int delta) {
        if (ref instanceof AccountRef.Team t) {
            Team team = FTBTeamsAPI.api().getManager().getTeamByID(t.teamId()).orElse(null);
            TeamCoins.add(team, delta);
            return TeamCoins.get(team);
        } else if (ref instanceof AccountRef.Player p) {
            Team team = FTBTeamsAPI.api().getManager().getTeamByID(p.uuid()).orElse(null);
            TeamCoins.add(team, delta);
            return TeamCoins.get(team);
        }
        return 0;
    }
}
