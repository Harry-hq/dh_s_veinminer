package com.harry_hq.dh_s_veinminer.network;

import com.harry_hq.dh_s_veinminer.HarryhqsVeinMiner;
import com.harry_hq.dh_s_veinminer.handler.VeinMinerTracker;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record VeinMinerPayload(boolean pressed) implements CustomPacketPayload{
	public static final Identifier ID=Identifier.fromNamespaceAndPath(HarryhqsVeinMiner.MODID,"vein_miner_key");
	public static final Type<VeinMinerPayload> TYPE=new Type<>(ID);
	public static final StreamCodec<FriendlyByteBuf,VeinMinerPayload> CODEC=StreamCodec.of(
		(buf,p)->buf.writeBoolean(p.pressed),
		buf->new VeinMinerPayload(buf.readBoolean()));

	@Override
	public Type<? extends CustomPacketPayload> type(){return TYPE;}

	public void handle(IPayloadContext ctx){
		ctx.enqueueWork(()->{
			var player=ctx.player();
			if(player==null)return;
			if(pressed)
				VeinMinerTracker.activate(player.getUUID());
			else
				VeinMinerTracker.deactivate(player.getUUID());
		});
	}
}
