package com.example.examplemod;

import java.util.Arrays;
import java.util.List;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

public class Config {
	private static final ModConfigSpec.Builder BUILDER=new ModConfigSpec.Builder();

	// 数量上线
	public static final ModConfigSpec.IntValue VEIN_MINER_MAX_BLOCKS=BUILDER
		.comment("最大连锁挖矿方块数量 (默认: 16384) / Maximum number of blocks to mine in a chain (default: 16384)")
		.defineInRange("veinMinerMaxBlocks",16384,1,16384);

	// 最大搜索半径
	public static final ModConfigSpec.IntValue VEIN_MINER_MAX_DISTANCE=BUILDER
		.comment("最大连锁挖矿搜索半径 (默认: 64) / Maximum search radius for vein mining (default: 64)")
		.defineInRange("veinMinerMaxDistance",64,1,64);

	// 触发方式: KEYBIND(自定义键位) / SNEAK(潜行触发) / ALWAYS(直接触发)
	public static final ModConfigSpec.ConfigValue<String> VEIN_MINER_TRIGGER_ACTION=BUILDER
		.comment("连锁挖矿触发方式: KEYBIND(自定义键位,默认V键), SNEAK(潜行时触发), ALWAYS(直接触发) (默认: KEYBIND) / Vein mining trigger: KEYBIND(custom key,default V), SNEAK(hold shift), ALWAYS(always active) (default: KEYBIND)")
		.define("veinMinerTriggerAction","KEYBIND");

	// 是否启用
	public static final ModConfigSpec.BooleanValue VEIN_MINER_ENABLED=BUILDER
		.comment("是否启用连锁挖矿功能 (默认: true) / Whether vein mining is enabled (default: true)")
		.define("veinMinerEnabled",true);

	// 是否消耗额外耐久
	public static final ModConfigSpec.BooleanValue VEIN_MINER_EXTRA_DURABILITY=BUILDER
		.comment("连锁挖矿时每个额外方块是否消耗额外耐久度 (默认: true) / Whether each extra block consumes additional durability (default: true)")
		.define("veinMinerExtraDurability",true);

	// 最大连锁数量
	public static final ModConfigSpec.IntValue MAX_VEIN_SIZE=VEIN_MINER_MAX_BLOCKS;

	// [已弃用] 根据附魔等级缩放连锁数量
	// public static final ModConfigSpec.BooleanValue SCALE_WITH_LEVEL=BUILDER
	// 	.comment("是否根据附魔等级缩放连锁挖矿数量 (默认: false) / Whether vein mining capacity scales with enchantment level (default: false)")
	// 	.define("veinMinerScaleWithLevel",false);

	// 连锁挖矿是否需要潜行
	public static final ModConfigSpec.ConfigValue<String> REQUIRE_SNEAKING=VEIN_MINER_TRIGGER_ACTION;

	// 模式: BLACKLIST(黑名单), WHITELIST(白名单), DISABLED(禁用)
	public static final ModConfigSpec.ConfigValue<String> VEIN_MINER_MODE=BUILDER
		.comment("连锁挖矿模式: BLACKLIST(黑名单), WHITELIST(白名单), DISABLED(禁用) (默认: DISABLED) / Vein mining mode: BLACKLIST, WHITELIST, DISABLED (default: DISABLED)")
		.define("veinMinerMode","DISABLED");

	// 白名单方块列表 (逗号分隔)
	public static final ModConfigSpec.ConfigValue<String> VEIN_MINER_WHITELIST=BUILDER
		.comment("白名单方块列表，多个用逗号分隔 (例如: minecraft:stone,minecraft:iron_ore)，仅在模式为WHITELIST时生效 / Whitelist of blocks, comma-separated (e.g., minecraft:stone,minecraft:iron_ore). Only effective when mode is WHITELIST.")
		.define("veinMinerWhitelist","");

	// 黑名单方块列表 (逗号分隔)
	public static final ModConfigSpec.ConfigValue<String> VEIN_MINER_BLACKLIST=BUILDER
		.comment("黑名单方块列表，多个用逗号分隔 (例如: minecraft:bedrock,minecraft:obsidian)，仅在模式为BLACKLIST时生效 / Blacklist of blocks, comma-separated (e.g., minecraft:bedrock,minecraft:obsidian). Only effective when mode is BLACKLIST.")
		.define("veinMinerBlacklist","");

	static final ModConfigSpec SPEC=BUILDER.build();

	public static int maxBlocks;
	public static int maxDistance;
	public static String triggerAction;
	public static boolean enabled;
	public static boolean extraDurability;
	public static boolean scaleWithLevel; // [已停用] 保留字段避免编译错误
	public static String mode;
	public static List<String> whitelist;
	public static List<String> blacklist;

	static void onConfigReload(ModConfigEvent event){refresh();}

	static void refresh(){
		maxBlocks=VEIN_MINER_MAX_BLOCKS.getAsInt();
		maxDistance=VEIN_MINER_MAX_DISTANCE.getAsInt();
		triggerAction=VEIN_MINER_TRIGGER_ACTION.get();
		enabled=VEIN_MINER_ENABLED.getAsBoolean();
		extraDurability=VEIN_MINER_EXTRA_DURABILITY.getAsBoolean();
		scaleWithLevel=false; // [已停用] 原: SCALE_WITH_LEVEL.getAsBoolean()
		mode=VEIN_MINER_MODE.get();
		whitelist=parseList(VEIN_MINER_WHITELIST.get());
		blacklist=parseList(VEIN_MINER_BLACKLIST.get());
	}

	// 将逗号分隔的字符串解析为列表，同时去除空白
	private static List<String> parseList(String input){
		if(input==null||input.isBlank())return List.of();
		return Arrays.stream(input.split(","))
			.map(String::trim)
			.filter(s->!s.isEmpty())
			.toList();
	}
}