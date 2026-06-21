package com.harry_hq.dh_s_veinminer.network;

import com.harry_hq.dh_s_veinminer.HarryhqsVeinMiner;
import com.harry_hq.dh_s_veinminer.Config;
import com.harry_hq.dh_s_veinminer.HarryhqsVeinMinerClientConfig;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * 服务端→客户端：同步服务端的关键配置，确保客户端按键逻辑匹配服务端配置。
 */
public record ConfigSyncPayload(String triggerAction, boolean enabled) implements CustomPacketPayload {
	public static final Identifier ID = Identifier.fromNamespaceAndPath(HarryhqsVeinMiner.MODID, "config_sync");
	public static final Type<ConfigSyncPayload> TYPE = new Type<>(ID);
	public static final StreamCodec<FriendlyByteBuf, ConfigSyncPayload> CODEC = StreamCodec.of(
		(buf, p) -> {
			buf.writeUtf(p.triggerAction);
			buf.writeBoolean(p.enabled);
		},
		buf -> new ConfigSyncPayload(buf.readUtf(), buf.readBoolean()));

	@Override
	public Type<? extends CustomPacketPayload> type() { return TYPE; }

	public void handle(IPayloadContext ctx) {
		ctx.enqueueWork(() -> {
			// 用服务端的配置覆盖客户端本地缓存，确保对齐
			HarryhqsVeinMinerClientConfig.serverTriggerAction = this.triggerAction;
			HarryhqsVeinMinerClientConfig.serverEnabled = this.enabled;
		});
	}
}
