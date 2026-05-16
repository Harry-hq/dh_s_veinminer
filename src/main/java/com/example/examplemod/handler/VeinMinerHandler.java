package com.example.examplemod.handler;

import com.example.examplemod.Config;
import com.example.examplemod.HarryhqsVeinMiner;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
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
import net.minecraft.resources.Identifier;
import java.util.*;

@EventBusSubscriber(modid=HarryhqsVeinMiner.MODID)
public class VeinMinerHandler {

	// 方块检测
	private static final BlockPos[] DIRECTIONS={
		// 六面
		new BlockPos(1,0,0),new BlockPos(-1,0,0),
		new BlockPos(0,1,0),new BlockPos(0,-1,0),
		new BlockPos(0,0,1),new BlockPos(0,0,-1),
		// 十二边
		new BlockPos(1,1,0),new BlockPos(1,-1,0),
		new BlockPos(-1,1,0),new BlockPos(-1,-1,0),
		new BlockPos(1,0,1),new BlockPos(1,0,-1),
		new BlockPos(-1,0,1),new BlockPos(-1,0,-1),
		new BlockPos(0,1,1),new BlockPos(0,1,-1),
		new BlockPos(0,-1,1),new BlockPos(0,-1,-1),
		// 八角
		new BlockPos(1,1,1),new BlockPos(1,1,-1),
		new BlockPos(1,-1,1),new BlockPos(1,-1,-1),
		new BlockPos(-1,1,1),new BlockPos(-1,1,-1),
		new BlockPos(-1,-1,1),new BlockPos(-1,-1,-1)
	};

	@SubscribeEvent
	public static void onBlockBreak(BreakBlockEvent event){
		// 仅服务端执行
		if(event.getLevel().isClientSide())return;
		// 检测模组是否启用
		if(!Config.enabled)return;
		Player player=event.getPlayer();
		if(!(player instanceof ServerPlayer serverPlayer))return;
		// 检查是否需要潜行
		if(Config.requireSneaking&&!player.isShiftKeyDown())return;
		ItemStack tool=player.getMainHandItem();
		if(tool.isEmpty())return;
		// 检查附魔
		Level level=(Level)event.getLevel();
		var enchantmentRegistry=level.registryAccess()
			.lookupOrThrow(Registries.ENCHANTMENT);
		Optional<Holder.Reference<Enchantment>> veinMinerOpt=enchantmentRegistry
			.get(HarryhqsVeinMiner.VEIN_MINER_ENCHANTMENT_KEY);
		if(veinMinerOpt.isEmpty())return;
		Holder<Enchantment> veinMinerEnchantment=veinMinerOpt.get();
		int enchantLevel=tool.getEnchantmentLevel(veinMinerEnchantment);
		if(enchantLevel<=0)return;
		// 复制工具用于后续掉落物处理
		ItemStack toolCopy=tool.copy();
		BlockPos originPos=event.getPos();
		BlockState originState=level.getBlockState(originPos);
		Block originBlock=originState.getBlock();
		// 获取方块列表
		List<BlockPos> targets=findConnectedBlocks(level,originPos,originState,Config.maxBlocks,
			Config.maxDistance);
		if(targets.isEmpty())return;
		// 阻断原事件
		event.setCanceled(true);
		// 连锁挖矿逻辑
		veinMine(serverPlayer,level,originPos,originState,targets,toolCopy,enchantLevel);
	}

	// 方块查找
	private static List<BlockPos> findConnectedBlocks(Level level,BlockPos origin,BlockState originState,
			int maxBlocks,int maxDistance){
		List<BlockPos> result=new ArrayList<>();
		Set<BlockPos> visited=new HashSet<>();
		Queue<BlockPos> queue=new LinkedList<>();
		queue.add(origin);
		visited.add(origin);
		while(!queue.isEmpty()&&result.size()<maxBlocks){
			BlockPos current=queue.poll();
			// 检查距离
			if(Math.abs(current.getX()-origin.getX())>maxDistance||
				Math.abs(current.getY()-origin.getY())>maxDistance||
				Math.abs(current.getZ()-origin.getZ())>maxDistance)
				continue;
			BlockState currentState=level.getBlockState(current);
			// 检查方块类型是否相同
			if(!isSameBlockType(currentState,originState))continue;
			// 检查黑/白名单
			if(!isBlockAllowed(level,currentState))continue;
			// 防止挖掘基岩
			if(currentState.getDestroySpeed(level,current)<0)continue;
			result.add(current);
			// 方块邻居搜索
			for(BlockPos dir:DIRECTIONS){
				BlockPos neighbor=current.offset(dir);
				if(!visited.contains(neighbor)&&level.isLoaded(neighbor)){
					visited.add(neighbor);
					queue.add(neighbor);
				}
			}
		}
		return result;
	}

	// 判断方块类型
	private static boolean isSameBlockType(BlockState state1,BlockState state2){
		// 判断方块id
		return state1.getBlock()==state2.getBlock();
	}

	// 检查方块是否被黑/白名单允许
	private static boolean isBlockAllowed(Level level,BlockState state){
		String mode=Config.mode;
		if("DISABLED".equals(mode))return true; // 模式禁用时不做限制
		Block block=state.getBlock();
		// 获取方块的注册ID
		Identifier blockId=level.registryAccess()
			.lookupOrThrow(Registries.BLOCK)
			.getKey(block);
		if(blockId==null)return true;
		String blockStr=blockId.toString();
		if("WHITELIST".equals(mode)){
			return Config.whitelist.contains(blockStr);
		}else if("BLACKLIST".equals(mode)){
			return !Config.blacklist.contains(blockStr);
		}
		return true;
	}

	// 执行连锁挖矿
	private static void veinMine(ServerPlayer player,Level level,BlockPos originPos,BlockState originState,
			List<BlockPos> targets,ItemStack tool,int enchantLevel){
		if(!(level instanceof ServerLevel serverLevel))return;
		// 首先检查工具耐久度是否足够
		int extraCost=Config.extraDurability?targets.size()-1:0;
		int currentDamage=tool.getDamageValue();
		int maxDamage=tool.getMaxDamage();
		if(maxDamage>0&&currentDamage+extraCost>=maxDamage){
			// 耐久度不足，只挖掘原方块
			breakSingleBlock(player,serverLevel,originPos,originState,tool);
			return;
		}
		// 获取玩家经验等级
		int minedCount=0;
		for(int i=0;i<targets.size();i++){
			BlockPos targetPos=targets.get(i);
			if(!level.isLoaded(targetPos))continue;
			BlockState targetState=level.getBlockState(targetPos);
			// 验证方块仍然存在且类型匹配
			if(!isSameBlockType(targetState,originState))continue;
			// 获取方块实体
			BlockEntity blockEntity=level.getBlockEntity(targetPos);
			// 破坏方块
			boolean removed=targetState.onDestroyedByPlayer(level,targetPos,player,tool,true,
					level.getFluidState(targetPos));
			if(!removed)continue;
			minedCount++;
			// 掉落物处理
			Collection<ItemStack> drops=Block.getDrops(targetState,serverLevel,targetPos,blockEntity,player,
					tool);
			for(ItemStack drop:drops)Block.popResource(level,targetPos,drop);
			// 设置方块为空气
			level.setBlock(targetPos,targetState.getFluidState().createLegacyBlock(),3);
			// 播放破坏声音和粒子效果
			level.levelEvent(player,2001,targetPos,Block.getId(targetState));
			// 消耗工具耐久度
			if(Config.extraDurability)tool.hurtAndBreak(1,player,EquipmentSlot.MAINHAND);
			// 更新主手物品状态
			player.setItemSlot(EquipmentSlot.MAINHAND,tool);
			// 检查工具是否损坏
			if(tool.isEmpty()||(maxDamage>0&&tool.getDamageValue()>=maxDamage))break;
		}
		// 在行动栏显示挖掘完成提示（与睡觉人数提示同一位置）
		String translationKey="dh_s_veinminer.message.vein_mine_complete";
		Component message=Component.translatable(translationKey,minedCount);
		player.connection.send(new ClientboundSetActionBarTextPacket(message));
	}

	// 单独破坏一个方块
	private static void breakSingleBlock(ServerPlayer player,ServerLevel level,BlockPos pos,BlockState state,
			ItemStack tool){
		BlockEntity blockEntity=level.getBlockEntity(pos);
		Collection<ItemStack> drops=Block.getDrops(state,level,pos,blockEntity,player,tool);
		boolean removed=state.onDestroyedByPlayer(level,pos,player,tool,true,level.getFluidState(pos));
		if(!removed)return;
		for(ItemStack drop:drops)Block.popResource(level,pos,drop);
		level.setBlock(pos,state.getFluidState().createLegacyBlock(),3);
		level.levelEvent(player,2001,pos,Block.getId(state));
	}
}