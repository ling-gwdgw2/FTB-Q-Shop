package com.holysweet.questshop.client;

public final class ClientCoins {
    private static int BALANCE = 0;

    private ClientCoins() {}

    public static void set(int balance) {
        BALANCE = balance;
    }

    public static int get() {
        return BALANCE;
    }
}
