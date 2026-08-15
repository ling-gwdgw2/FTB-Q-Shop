package com.holysweet.questshop;

import net.neoforged.neoforge.common.ModConfigSpec;

public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    // ==========================================
    // 1. Gameplay & Drops Settings
    // ==========================================
    public static final ModConfigSpec.BooleanValue LOOT_COIN_FROM_ENEMY;
    public static final ModConfigSpec.DoubleValue LOOT_COIN_DROP_CHANCE;

    // ==========================================
    // 2. Feature Flags (Modular Toggles)
    // ==========================================
    public static final ModConfigSpec.BooleanValue ENABLE_INVENTORY_BUTTON;
    public static final ModConfigSpec.BooleanValue ENABLE_SOUND_EFFECTS;
    public static final ModConfigSpec.BooleanValue ENABLE_TOAST_NOTIFICATIONS;

    static final ModConfigSpec SPEC;

    static {
        BUILDER.push("drops");
        BUILDER.comment("Enemies can drop coins when killed. (values: true, false)");
        LOOT_COIN_FROM_ENEMY = BUILDER.define("lootCoinFromEnemy", true);

        BUILDER.comment("Coin drop chance from enemies (0.0 to 1.0).");
        LOOT_COIN_DROP_CHANCE = BUILDER.defineInRange("lootCoinDropChance", 0.05d, 0.0d, 1.0d);
        BUILDER.pop();

        BUILDER.push("features");
        BUILDER.comment("Enable the Primogem Coin Shop button in player inventory screen (E).");
        ENABLE_INVENTORY_BUTTON = BUILDER.define("enableInventoryButton", true);

        BUILDER.comment("Play sound effects when completing purchases or actions in the shop.");
        ENABLE_SOUND_EFFECTS = BUILDER.define("enableSoundEffects", true);

        BUILDER.comment("Show popup toast notifications when receiving coins or buying items.");
        ENABLE_TOAST_NOTIFICATIONS = BUILDER.define("enableToastNotifications", true);
        BUILDER.pop();

        SPEC = BUILDER.build();
    }
}
