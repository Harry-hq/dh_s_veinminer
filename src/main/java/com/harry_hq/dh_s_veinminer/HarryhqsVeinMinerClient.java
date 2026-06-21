package com.harry_hq.dh_s_veinminer;

import com.harry_hq.dh_s_veinminer.network.VeinMinerPayload;
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
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
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

		// 使用服务端同步过来的 triggerAction 来决定发送逻辑
		String action=HarryhqsVeinMinerClientConfig.getEffectiveTriggerAction();

		boolean pressed;
		switch(action){
			case "ALWAYS" -> pressed=true;
			case "SNEAK" -> pressed=mc.player.isShiftKeyDown();
			default -> pressed=VEIN_MINER_KEY.isDown(); // KEYBIND
		}

		if(pressed!=lastPressed){
			lastPressed=pressed;
			ClientPacketDistributor.sendToServer(new VeinMinerPayload(pressed));
		}
	}

	@SubscribeEvent
	static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event){
		// 断开连接时清理服务端配置缓存，回退到本地配置
		HarryhqsVeinMinerClientConfig.reset();
	}
}
