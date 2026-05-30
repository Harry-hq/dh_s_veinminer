package com.harry_hq.dh_s_veinminer;

<<<<<<< HEAD
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS =
        DeferredRegister.create(Registries.BLOCK, HarryhqsVeinMiner.MODID);

    public static final Supplier<Block> TC_VM_STONE = BLOCKS.register("tc_vm_stone",
        () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE)));
=======
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlocks {
    private static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks(HarryhqsVeinMiner.MODID);

    private static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(HarryhqsVeinMiner.MODID);

    // 完全复用石头代码的方块，仅可通过指令获得
    public static final DeferredBlock<Block> TC_VM_STONE = BLOCKS.registerSimpleBlock("tc_vm_stone",
            () -> Block.Properties.ofFullCopy(Blocks.STONE));

    // registerSimpleBlockItem(Holder<Block>) 内部用 Holder 延迟绑定，不会出现未绑定错误
    public static final DeferredItem<net.minecraft.world.item.BlockItem> TC_VM_STONE_ITEM =
            ITEMS.registerSimpleBlockItem(TC_VM_STONE);

    public static void register(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
    }
>>>>>>> f3fde9c (注册演示方块)
}
