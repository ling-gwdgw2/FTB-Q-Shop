package com.holysweet.questshop.api.coins;

import java.util.UUID;

public interface AccountRef {
    record Player(UUID uuid) implements AccountRef {}
    record Team(UUID teamId) implements AccountRef {}
}
