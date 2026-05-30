package com.harry_hq.dh_s_veinminer;

import org.slf4j.Logger;
import com.mojang.logging.LogUtils;
import com.harry_hq.dh_s_veinminer.network.VeinMinerPayload;
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
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

@Mod(HarryhqsVeinMiner.MODID)
public class HarryhqsVeinMiner{
	public static final String MODID="dh_s_veinminer";
	public static final Logger LOGGER=LogUtils.getLogger();

	public static final ResourceKey<Enchantment> VEIN_MINER_ENCHANTMENT_KEY=
		ResourceKey.create(Registries.ENCHANTMENT,Identifier.fromNamespaceAndPath(MODID,"vein_miner"));

	public HarryhqsVeinMiner(IEventBus modEventBus,ModContainer modContainer){
		ModBlocks.register(modEventBus);
		modEventBus.addListener(this::commonSetup);
		modEventBus.addListener(Config::onConfigReload);
		modEventBus.addListener(this::registerPayloads);
		modContainer.registerConfig(ModConfig.Type.COMMON,Config.SPEC);
	}

	private void commonSetup(FMLCommonSetupEvent event){
		Config.refresh();
		LOGGER.info("连锁挖矿模组已加载!");
	}

	private void registerPayloads(RegisterPayloadHandlersEvent event){
		var registrar=event.registrar(MODID);
		registrar.playToServer(VeinMinerPayload.TYPE,VeinMinerPayload.CODEC,VeinMinerPayload::handle);
	}
}
