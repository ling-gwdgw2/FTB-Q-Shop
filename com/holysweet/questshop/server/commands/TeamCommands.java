package com.holysweet.questshop.server.commands;

import com.holysweet.questshop.network.payload.TeamMembersPayload;
import com.holysweet.questshop.service.CoinsService;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public final class TeamCommands {

    private TeamCommands() {}

    public static int showTeam(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        TeamMembersPayload data = CoinsService.getTeamData(player);

        if (!data.hasTeam()) {
            ctx.getSource().sendSuccess(() -> Component.literal(
                    "§6✦ Personal Gem Account: §e" + player.getGameProfile().getName() + "\n" +
                    "§7----------------------------------------\n" +
                    "§f• Current Balance: §6💎 " + data.totalPot() + " G\n" +
                    "§8(Join an FTB Party Team to track member contributions with friends!)"
            ), false);
            return 1;
        }

        int totalEarned = data.members().stream().mapToInt(TeamMembersPayload.MemberEntry::earned).sum();

        StringBuilder sb = new StringBuilder();
        sb.append("§6✦ [Team: §e").append(data.teamName()).append("§6] Member Gem Breakdown ✦\n");
        sb.append("§7--------------------------------------------------\n");

        for (TeamMembersPayload.MemberEntry m : data.members()) {
            int pct = totalEarned > 0 ? (int) Math.round(((double) m.earned() / totalEarned) * 100) : 0;
            String roleTag = m.isOwner() ? " §6★" : "";
            String status = m.isOnline() ? "§a●" : "§7○";

            sb.append(status).append(" §f").append(m.name()).append(roleTag).append("§7: ");
            sb.append("§a+").append(m.earned()).append(" G §7(Earned) ");
            sb.append("§c-").append(m.spent()).append(" G §7(Spent) ");
            sb.append("§e[").append(pct).append("%]\n");
        }

        sb.append("§7--------------------------------------------------\n");
        sb.append("§6💎 Total Team Pot: §e").append(data.totalPot()).append(" G §7(Shared balance)");

        ctx.getSource().sendSuccess(() -> Component.literal(sb.toString()), false);
        return 1;
    }
}
