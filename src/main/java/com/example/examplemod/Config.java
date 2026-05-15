package com.example.examplemod;

import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    // 数量上线
    public static final ModConfigSpec.IntValue VEIN_MINER_MAX_BLOCKS = BUILDER
            .comment("最大连锁挖矿方块数量 (默认: 64)")
            .defineInRange("veinMinerMaxBlocks", 64, 1, 1024);

    // 最大搜索半径
    public static final ModConfigSpec.IntValue VEIN_MINER_MAX_DISTANCE = BUILDER
            .comment("最大连锁挖矿搜索半径 (默认: 8)")
            .defineInRange("veinMinerMaxDistance", 8, 1, 32);

    // 是否需要潜行触发
    public static final ModConfigSpec.BooleanValue VEIN_MINER_REQUIRE_SNEAKING = BUILDER
            .comment("是否需要在潜行时触发连锁挖矿 (默认: true)")
            .define("veinMinerRequireSneaking", true);

    // 是否启用
    public static final ModConfigSpec.BooleanValue VEIN_MINER_ENABLED = BUILDER
            .comment("是否启用连锁挖矿功能 (默认: true)")
            .define("veinMinerEnabled", true);

    // 是否消耗额外耐久
    public static final ModConfigSpec.BooleanValue VEIN_MINER_EXTRA_DURABILITY = BUILDER
            .comment("连锁挖矿时每个额外方块是否消耗额外耐久度 (默认: true)")
            .define("veinMinerExtraDurability", true);

    // 最大连锁数量
    public static final ModConfigSpec.IntValue MAX_VEIN_SIZE = VEIN_MINER_MAX_BLOCKS;

    // 是否根据附魔等级缩放连锁数量
    public static final ModConfigSpec.BooleanValue SCALE_WITH_LEVEL = BUILDER
            .comment("是否根据附魔等级缩放连锁挖矿数量 (默认: false)")
            .define("veinMinerScaleWithLevel", false);

    // 连锁挖矿是否需要潜行
    public static final ModConfigSpec.BooleanValue REQUIRE_SNEAKING = VEIN_MINER_REQUIRE_SNEAKING;

    static final ModConfigSpec SPEC = BUILDER.build();

    public static int maxBlocks;
    public static int maxDistance;
    public static boolean requireSneaking;
    public static boolean enabled;
    public static boolean extraDurability;
    public static boolean scaleWithLevel;

    static void onConfigReload(ModConfigEvent event) {
        refresh();
    }

    static void refresh() {
        maxBlocks = VEIN_MINER_MAX_BLOCKS.getAsInt();
        maxDistance = VEIN_MINER_MAX_DISTANCE.getAsInt();
        requireSneaking = VEIN_MINER_REQUIRE_SNEAKING.getAsBoolean();
        enabled = VEIN_MINER_ENABLED.getAsBoolean();
        extraDurability = VEIN_MINER_EXTRA_DURABILITY.getAsBoolean();
        scaleWithLevel = SCALE_WITH_LEVEL.getAsBoolean();
    }
}