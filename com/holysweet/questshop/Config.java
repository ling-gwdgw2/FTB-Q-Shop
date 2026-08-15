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

    // ==========================================
    // 3. Daily Flash Deals Settings
    // ==========================================
    public static final ModConfigSpec.BooleanValue ENABLE_DAILY_DEALS;
    public static final ModConfigSpec.IntValue DEALS_COUNT;
    public static final ModConfigSpec.IntValue FIXED_DISCOUNT_PERCENT;
    public static final ModConfigSpec.IntValue MIN_DISCOUNT_PERCENT;
    public static final ModConfigSpec.IntValue MAX_DISCOUNT_PERCENT;

    // ==========================================
    // 4. Stock & Purchase Limits Settings
    // ==========================================
    public static final ModConfigSpec.BooleanValue ENABLE_PURCHASE_LIMITS;

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

        BUILDER.push("daily_deals");
        BUILDER.comment("Enable Daily Flash Deals (Random discounted items refreshed every 24 in-game hours).");
        ENABLE_DAILY_DEALS = BUILDER.define("enableDailyDeals", true);

        BUILDER.comment("Number of random items in Daily Flash Deals (1 to 10).");
        DEALS_COUNT = BUILDER.defineInRange("dealsCount", 3, 1, 10);

        BUILDER.comment("Fixed discount percentage (0 to use random min/max range, or set e.g. 20, 30, 50 for fixed discount).");
        FIXED_DISCOUNT_PERCENT = BUILDER.defineInRange("fixedDiscountPercent", 0, 0, 90);

        BUILDER.comment("Minimum discount percentage when random discount is used (5 to 90%).");
        MIN_DISCOUNT_PERCENT = BUILDER.defineInRange("minDiscountPercent", 20, 5, 90);

        BUILDER.comment("Maximum discount percentage when random discount is used (5 to 90%).");
        MAX_DISCOUNT_PERCENT = BUILDER.defineInRange("maxDiscountPercent", 50, 5, 90);
        BUILDER.pop();

        BUILDER.push("limits");
        BUILDER.comment("Enable Stock and Daily Purchase Limits enforcement.");
        ENABLE_PURCHASE_LIMITS = BUILDER.define("enablePurchaseLimits", true);
        BUILDER.pop();

        SPEC = BUILDER.build();
    }
}
