package com.harry_hq.dh_s_veinminer.handler;

import com.harry_hq.dh_s_veinminer.Config;
import com.harry_hq.dh_s_veinminer.CuboidRegion;
import com.harry_hq.dh_s_veinminer.HarryhqsVeinMiner;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.block.BreakBlockEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import net.minecraft.resources.Identifier;
import java.util.*;

@EventBusSubscriber(modid=HarryhqsVeinMiner.MODID)
public class VeinMinerHandler{

	private static final BlockPos[] DIRECTIONS={
		new BlockPos(1,0,0),new BlockPos(-1,0,0),
		new BlockPos(0,1,0),new BlockPos(0,-1,0),
		new BlockPos(0,0,1),new BlockPos(0,0,-1),
		new BlockPos(1,1,0),new BlockPos(1,-1,0),
		new BlockPos(-1,1,0),new BlockPos(-1,-1,0),
		new BlockPos(1,0,1),new BlockPos(1,0,-1),
		new BlockPos(-1,0,1),new BlockPos(-1,0,-1),
		new BlockPos(0,1,1),new BlockPos(0,1,-1),
		new BlockPos(0,-1,1),new BlockPos(0,-1,-1),
		new BlockPos(1,1,1),new BlockPos(1,1,-1),
		new BlockPos(1,-1,1),new BlockPos(1,-1,-1),
		new BlockPos(-1,1,1),new BlockPos(-1,1,-1),
		new BlockPos(-1,-1,1),new BlockPos(-1,-1,-1)
	};

	@SubscribeEvent
	public static void onBlockBreak(BreakBlockEvent event){
		if(event.getLevel().isClientSide())return;
		if(!Config.enabled)return;
		Player player=event.getPlayer();
		if(!(player instanceof ServerPlayer serverPlayer))return;
		String action=Config.triggerAction;
		if("SNEAK".equals(action)&&!player.isShiftKeyDown())return;
		if("KEYBIND".equals(action)&&!VeinMinerTracker.isActive(player.getUUID()))return;
		ItemStack tool=player.getMainHandItem();
		if(tool.isEmpty())return;
		Level level=(Level)event.getLevel();
		var enchantmentRegistry=level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
		Optional<Holder.Reference<Enchantment>> veinMinerOpt=enchantmentRegistry
			.get(HarryhqsVeinMiner.VEIN_MINER_ENCHANTMENT_KEY);
		if(veinMinerOpt.isEmpty())return;
		Holder<Enchantment> veinMinerEnchantment=veinMinerOpt.get();
		int enchantLevel=tool.getEnchantmentLevel(veinMinerEnchantment);
		if(enchantLevel<=0)return;
		ItemStack toolCopy=tool.copy();
		BlockPos originPos=event.getPos();
		BlockState originState=level.getBlockState(originPos);
		List<BlockPos> targets=findConnectedBlocks(level,originPos,originState,Config.maxBlocks,Config.maxDistance);
		if(targets.isEmpty())return;
		event.setCanceled(true);
		veinMine(serverPlayer,level,originPos,originState,targets,toolCopy,enchantLevel);
	}

	private static List<BlockPos> findConnectedBlocks(Level level,BlockPos origin,BlockState originState,int maxBlocks,int maxDistance){
		if(!Config.regions.isEmpty()&&!isInAnyRegion(origin))
			return List.of();

		List<BlockPos> result=new ArrayList<>();
		Set<BlockPos> visited=new HashSet<>();
		Queue<BlockPos> queue=new LinkedList<>();
		queue.add(origin);
		visited.add(origin);
		while(!queue.isEmpty()&&result.size()<maxBlocks){
			BlockPos current=queue.poll();
			if(Math.abs(current.getX()-origin.getX())>maxDistance||
				Math.abs(current.getY()-origin.getY())>maxDistance||
				Math.abs(current.getZ()-origin.getZ())>maxDistance)
				continue;
			BlockState currentState=level.getBlockState(current);
			if(!isSameBlockType(currentState,originState))continue;
			if(!isBlockAllowed(level,currentState))continue;
			if(currentState.getDestroySpeed(level,current)<0)continue;
			result.add(current);
			for(BlockPos dir:DIRECTIONS){
				BlockPos neighbor=current.offset(dir);
				if(!visited.contains(neighbor)&&level.isLoaded(neighbor)){
					if(!Config.regions.isEmpty()&&!isInAnyRegion(neighbor))
						continue;
					visited.add(neighbor);
					queue.add(neighbor);
				}
			}
		}
		return result;
	}

	private static boolean isInAnyRegion(BlockPos pos){
		for(CuboidRegion region:Config.regions){
			if(region.contains(pos))return true;
		}
		return false;
	}

	private static boolean isSameBlockType(BlockState state1,BlockState state2){
		return state1.getBlock()==state2.getBlock();
	}

	private static boolean isBlockAllowed(Level level,BlockState state){
		String mode=Config.mode;
		if("DISABLED".equals(mode))return true;
		Block block=state.getBlock();
		Identifier blockId=level.registryAccess().lookupOrThrow(Registries.BLOCK).getKey(block);
		if(blockId==null)return true;
		String blockStr=blockId.toString();
		if("WHITELIST".equals(mode))return Config.whitelist.contains(blockStr);
		if("BLACKLIST".equals(mode))return !Config.blacklist.contains(blockStr);
		return true;
	}

	/**
	 * 从方块战利品表中获取经验值并在玩家位置生成经验球实体。
	 * 经验球触发经验修补附魔和其他模组的经验吸收机制。
	 */
	private static void dropExperienceAtPlayer(ServerLevel level, BlockState state,
											   BlockEntity blockEntity, Player player, ItemStack tool) {
		int exp = state.getBlock().getExpDrop(state, level, BlockPos.containing(player.position()),
			blockEntity, player, tool);
		if (exp > 0) {
			ExperienceOrb.award(level, player.position(), exp);
		}
	}

	private static void veinMine(ServerPlayer player,Level level,BlockPos originPos,BlockState originState,List<BlockPos> targets,ItemStack tool,int enchantLevel){
		if(!(level instanceof ServerLevel serverLevel))return;
		int extraCost=Config.extraDurability?targets.size()-1:0;
		int currentDamage=tool.getDamageValue();
		int maxDamage=tool.getMaxDamage();
		if(maxDamage>0&&currentDamage+extraCost>=maxDamage){
			breakSingleBlock(player,serverLevel,originPos,originState,tool);
			return;
		}
		int minedCount=0;
		for(int i=0;i<targets.size();i++){
			BlockPos targetPos=targets.get(i);
			if(!level.isLoaded(targetPos))continue;
			BlockState targetState=level.getBlockState(targetPos);
			if(!isSameBlockType(targetState,originState))continue;
			BlockEntity blockEntity=level.getBlockEntity(targetPos);
			boolean removed=targetState.onDestroyedByPlayer(level,targetPos,player,tool,true,level.getFluidState(targetPos));
			if(!removed)continue;
			minedCount++;
			// 手动处理物品掉落（直接给玩家）
			Collection<ItemStack> drops;
			if(!targetState.requiresCorrectToolForDrops()||tool.isCorrectToolForDrops(targetState))
				drops=Block.getDrops(targetState,serverLevel,targetPos,blockEntity,player,tool);
			else
				drops=List.of();
			for(ItemStack drop:drops)giveToPlayerOrDrop(player,level,targetPos,drop);
			// legacy 经验（兼容旧版方块）
			targetState.spawnAfterBreak(serverLevel,targetPos,tool,true);
			// 在玩家位置生成经验球（触发经验修补等附魔效果）
			dropExperienceAtPlayer(serverLevel, targetState, blockEntity, player, tool);
			level.setBlock(targetPos,targetState.getFluidState().createLegacyBlock(),3);
			level.levelEvent(player,2001,targetPos,Block.getId(targetState));
			if(Config.extraDurability)tool.hurtAndBreak(1,player,EquipmentSlot.MAINHAND);
			player.setItemSlot(EquipmentSlot.MAINHAND,tool);
			if(tool.isEmpty()||(maxDamage>0&&tool.getDamageValue()>=maxDamage))break;
		}
		String key="dh_s_veinminer.message.vein_mine_complete";
		player.connection.send(new ClientboundSetActionBarTextPacket(Component.translatable(key,minedCount)));
	}

	private static void breakSingleBlock(ServerPlayer player,ServerLevel level,BlockPos pos,BlockState state,ItemStack tool){
		BlockEntity blockEntity=level.getBlockEntity(pos);
		boolean removed=state.onDestroyedByPlayer(level,pos,player,tool,true,level.getFluidState(pos));
		if(!removed)return;
		for(ItemStack drop:Block.getDrops(state,level,pos,blockEntity,player,tool))
			giveToPlayerOrDrop(player,level,pos,drop);
		state.spawnAfterBreak(level,pos,tool,true);
		dropExperienceAtPlayer(level, state, blockEntity, player, tool);
		level.setBlock(pos,state.getFluidState().createLegacyBlock(),3);
		level.levelEvent(player,2001,pos,Block.getId(state));
	}

	private static void giveToPlayerOrDrop(ServerPlayer player,Level level,BlockPos pos,ItemStack stack){
		if(!player.getInventory().add(stack))
			Block.popResource(level,pos,stack);
	}

	@SubscribeEvent
	public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event){
		if(event.getEntity() instanceof ServerPlayer sp)
			VeinMinerTracker.onPlayerDisconnect(sp.getUUID());
	}
}
