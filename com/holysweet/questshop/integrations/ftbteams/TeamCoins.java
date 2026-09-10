package com.holysweet.questshop.integrations.ftbteams;

import com.holysweet.questshop.QuestShop;
import com.holysweet.questshop.network.payload.TeamMembersPayload;
import com.mojang.authlib.GameProfile;
import dev.ftb.mods.ftbteams.api.FTBTeamsAPI;
import dev.ftb.mods.ftbteams.api.Team;
import dev.ftb.mods.ftbteams.api.event.PlayerJoinedPartyTeamEvent;
import dev.ftb.mods.ftbteams.api.event.PlayerLeftPartyTeamEvent;
import dev.ftb.mods.ftbteams.api.property.IntProperty;
import dev.ftb.mods.ftbteams.api.property.StringProperty;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.*;
import java.util.stream.Collectors;

public final class TeamCoins {
    public static final IntProperty COINS = new IntProperty(ResourceLocation.fromNamespaceAndPath(QuestShop.MODID, "coins"), 0);
    public static final StringProperty MEMBER_CONTRIBUTIONS = new StringProperty(ResourceLocation.fromNamespaceAndPath(QuestShop.MODID, "member_contributions"), "");

    public record MemberStat(int earned, int spent) {
        public int net() {
            return Math.max(0, earned - spent);
        }
    }

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

    public static Map<UUID, MemberStat> getMemberStats(Team team) {
        Map<UUID, MemberStat> map = new HashMap<>();
        if (team == null) return map;
        String raw = team.getProperty(MEMBER_CONTRIBUTIONS);
        if (raw == null || raw.isEmpty()) return map;

        for (String entry : raw.split(";")) {
            String[] parts = entry.split("=");
            if (parts.length == 2) {
                try {
                    UUID uuid = UUID.fromString(parts[0]);
                    String[] vals = parts[1].split(",");
                    int earned = Integer.parseInt(vals[0]);
                    int spent = vals.length > 1 ? Integer.parseInt(vals[1]) : 0;
                    map.put(uuid, new MemberStat(Math.max(0, earned), Math.max(0, spent)));
                } catch (Exception ignored) {}
            }
        }
        return map;
    }

    private static void writeMemberStats(Team team, Map<UUID, MemberStat> map) {
        if (team == null) return;
        String serialized = map.entrySet().stream()
                .map(e -> e.getKey().toString() + "=" + e.getValue().earned() + "," + e.getValue().spent())
                .collect(Collectors.joining(";"));
        team.setProperty(MEMBER_CONTRIBUTIONS, serialized);
    }

    public static MemberStat getMemberStat(Team team, UUID uuid) {
        return getMemberStats(team).getOrDefault(uuid, new MemberStat(0, 0));
    }

    public static void recordMemberDelta(Team team, UUID uuid, int delta) {
        if (team == null || uuid == null || delta == 0) return;
        Map<UUID, MemberStat> map = new HashMap<>(getMemberStats(team));
        MemberStat current = map.getOrDefault(uuid, new MemberStat(0, 0));
        int newEarned = current.earned();
        int newSpent = current.spent();
        if (delta > 0) {
            newEarned += delta;
        } else {
            newSpent += Math.abs(delta);
        }
        map.put(uuid, new MemberStat(newEarned, newSpent));
        writeMemberStats(team, map);
    }

    public static TeamMembersPayload buildTeamPayload(ServerPlayer player) {
        if (player == null) {
            return new TeamMembersPayload(false, "Solo", 0, Collections.emptyList());
        }

        MinecraftServer server = player.getServer();
        Optional<Team> teamOpt = FTBTeamsAPI.api().getManager().getTeamForPlayer(player);

        if (teamOpt.isPresent()) {
            Team team = teamOpt.get();
            boolean isParty = team.isPartyTeam();
            String teamName = isParty ? team.getName().getString() : (player.getGameProfile().getName() + "'s Wallet");
            int totalPot = get(team);
            UUID ownerUuid = team.getOwner();

            Map<UUID, MemberStat> stats = getMemberStats(team);
            List<TeamMembersPayload.MemberEntry> members = new ArrayList<>();

            Set<UUID> memberUuids = new HashSet<>(team.getMembers());
            // Ensure owner is included
            if (ownerUuid != null) memberUuids.add(ownerUuid);
            // Ensure current player is included
            memberUuids.add(player.getUUID());

            for (UUID uuid : memberUuids) {
                String memberName = "Player";
                boolean isOnline = false;

                if (server != null) {
                    ServerPlayer onlinePlayer = server.getPlayerList().getPlayer(uuid);
                    if (onlinePlayer != null) {
                        memberName = onlinePlayer.getGameProfile().getName();
                        isOnline = true;
                    } else if (server.getProfileCache() != null) {
                        Optional<GameProfile> cached = server.getProfileCache().get(uuid);
                        if (cached.isPresent()) {
                            memberName = cached.get().getName();
                        } else {
                            memberName = uuid.toString().substring(0, 8);
                        }
                    }
                }

                MemberStat stat = stats.getOrDefault(uuid, new MemberStat(0, 0));
                boolean isOwner = uuid.equals(ownerUuid);

                members.add(new TeamMembersPayload.MemberEntry(
                        uuid,
                        memberName,
                        stat.earned(),
                        stat.spent(),
                        isOwner,
                        isOnline
                ));
            }

            // Sort members: Owner first, then by earned descending, then by name
            members.sort(Comparator.comparing((TeamMembersPayload.MemberEntry m) -> !m.isOwner())
                    .thenComparing(Comparator.comparingInt(TeamMembersPayload.MemberEntry::earned).reversed())
                    .thenComparing(TeamMembersPayload.MemberEntry::name));

            return new TeamMembersPayload(isParty, teamName, totalPot, members);
        }

        // Fallback for non-team player
        TeamMembersPayload.MemberEntry solo = new TeamMembersPayload.MemberEntry(
                player.getUUID(),
                player.getGameProfile().getName(),
                0,
                0,
                true,
                true
        );
        return new TeamMembersPayload(false, player.getGameProfile().getName(), 0, List.of(solo));
    }

    public static void PlayerJoinedPartyTeamEvent(PlayerJoinedPartyTeamEvent event) {
        Team personal = event.getPreviousTeam();
        Team party = event.getTeam();
        if (personal != null && party != null) {
            int coins = get(personal);
            if (coins > 0) {
                add(party, coins);
                recordMemberDelta(party, event.getPlayer().getUUID(), coins);
                set(personal, 0);
            }
        }
    }

    public static void PlayerLeftPartyTeamEvent(PlayerLeftPartyTeamEvent event) {
        // Player leaves party - history is preserved
    }
}
