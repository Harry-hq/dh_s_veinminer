package com.harry_hq.dh_s_veinminer;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
        DeferredRegister.create(Registries.ITEM, HarryhqsVeinMiner.MODID);

    public static final Supplier<Item> TC_VM_STONE = ITEMS.register("tc_vm_stone",
        () -> new BlockItem(ModBlocks.TC_VM_STONE.get(), new Item.Properties()));
}
