package com.holysweet.questshop.client;

import com.holysweet.questshop.api.ShopEntry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ClientShopData {
    private static List<ShopEntry> ENTRIES = Collections.emptyList();

    private ClientShopData() {}

    public static void set(List<ShopEntry> entries) {
        ENTRIES = Collections.unmodifiableList(new ArrayList<>(entries));
    }

    public static List<ShopEntry> get() {
        return ENTRIES;
    }
}
