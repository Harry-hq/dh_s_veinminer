package com.harry_hq.dh_s_veinminer;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlocks {
    private static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks(HarryhqsVeinMiner.MODID);

    private static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(HarryhqsVeinMiner.MODID);

    // 用 registerBlock 确保 setId 被调用，显式指定石头属性
    public static final DeferredBlock<Block> TC_VM_STONE = BLOCKS.registerBlock("tc_vm_stone",
            Block::new,
            () -> BlockBehaviour.Properties.of()
                    .destroyTime(1.5F)
                    .explosionResistance(6.0F)
                    .sound(SoundType.STONE)
                    .requiresCorrectToolForDrops());

    // BlockItem 注册
    public static final DeferredItem<net.minecraft.world.item.BlockItem> TC_VM_STONE_ITEM =
            ITEMS.registerSimpleBlockItem(TC_VM_STONE);

    public static void register(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
    }
}
