package com.example.examplemod;

import com.example.examplemod.network.VeinMinerPayload;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.lwjgl.glfw.GLFW;

@Mod(value=HarryhqsVeinMiner.MODID,dist=Dist.CLIENT)
@EventBusSubscriber(modid=HarryhqsVeinMiner.MODID,value=Dist.CLIENT)
public class HarryhqsVeinMinerClient{
	private static final KeyMapping.Category KEY_CATEGORY=new KeyMapping.Category(
		Identifier.fromNamespaceAndPath(HarryhqsVeinMiner.MODID,"category"));
	private static final String KEY_NAME="key."+HarryhqsVeinMiner.MODID+".trigger";
	public static final KeyMapping VEIN_MINER_KEY=new KeyMapping(KEY_NAME,GLFW.GLFW_KEY_V,KEY_CATEGORY);

	public HarryhqsVeinMinerClient(ModContainer container){
		container.registerExtensionPoint(IConfigScreenFactory.class,ConfigurationScreen::new);
	}

	@SubscribeEvent
	static void onRegisterKeys(RegisterKeyMappingsEvent event){
		event.registerCategory(KEY_CATEGORY);
		event.register(VEIN_MINER_KEY);
	}

	private static boolean lastPressed=false;

	@SubscribeEvent
	static void onClientTick(ClientTickEvent.Pre event){
		var mc=Minecraft.getInstance();
		if(mc.player==null||mc.level==null){
			lastPressed=false;
			return;
		}
		boolean pressed=VEIN_MINER_KEY.isDown();
		if(pressed!=lastPressed){
			lastPressed=pressed;
			ClientPacketDistributor.sendToServer(new VeinMinerPayload(pressed));
		}
	}
}
