package com.mysticalchemy.init;

import com.mysticalchemy.MysticAlchemy;
import com.mysticalchemy.crucible.BlockCrucible;
import com.mysticalchemy.crucible.BlockEmptyCrucible;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class BlockInit {
	public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MysticAlchemy.MODID);
	public static final RegistryObject<BlockCrucible> CRUCIBLE = BLOCKS.register("crucible", BlockCrucible::new);
	public static final RegistryObject<BlockEmptyCrucible> EMPTY_CRUCIBLE = BLOCKS.register("crucible_empty", BlockEmptyCrucible::new);
}
