package com.mysticalchemy.init;

import com.mysticalchemy.MysticAlchemy;
import com.mysticalchemy.crucible.CrucibleTile;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class TileEntityInit {
public static final DeferredRegister<BlockEntityType<?>> TILE_ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITIES, MysticAlchemy.MODID);
	
	public static final RegistryObject<BlockEntityType<CrucibleTile>> CRUCIBLE_TILE_TYPE = TILE_ENTITY_TYPES.register(
			"chalk_rune_tile_entity", 
			() -> BlockEntityType.Builder.of(CrucibleTile::new, 
					BlockInit.CRUCIBLE.get()
			).build(null));
}
