package com.holysweet.questshop.client;

import com.holysweet.questshop.network.payload.TeamMembersPayload;

import java.util.Collections;

public final class ClientTeamData {
    private static TeamMembersPayload CURRENT = new TeamMembersPayload(false, "", 0, Collections.emptyList());

    private ClientTeamData() {}

    public static void set(TeamMembersPayload payload) {
        if (payload != null) {
            CURRENT = payload;
        }
    }

    public static TeamMembersPayload get() {
        return CURRENT;
    }

    public static void reset() {
        CURRENT = new TeamMembersPayload(false, "", 0, Collections.emptyList());
    }
}
