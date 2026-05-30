package com.harry_hq.dh_s_veinminer;

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
}
