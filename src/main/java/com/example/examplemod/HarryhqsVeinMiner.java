package com.example.examplemod;

import org.slf4j.Logger;
import com.mojang.logging.LogUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.enchantment.Enchantment;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(HarryhqsVeinMiner.MODID)
public class HarryhqsVeinMiner {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "dh_s_veinminer";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();

    // 连锁挖矿附魔的资源键
    public static final ResourceKey<Enchantment> VEIN_MINER_ENCHANTMENT_KEY = ResourceKey.create(Registries.ENCHANTMENT,
            Identifier.fromNamespaceAndPath(MODID, "vein_miner"));

    // The constructor for the mod class is the first code that is run when your mod
    // is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and
    // pass them in automatically.
    public HarryhqsVeinMiner(IEventBus modEventBus, ModContainer modContainer) {
        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);
        // 注册配置重载事件
        modEventBus.addListener(Config::onConfigReload);
        // Register our mod's ModConfigSpec so that FML can create and load the config
        // file for us
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        Config.refresh();
        LOGGER.info("连锁挖矿模组已加载!");
    }
}