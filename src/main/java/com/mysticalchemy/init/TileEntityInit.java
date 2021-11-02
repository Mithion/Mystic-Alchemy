package com.mysticalchemy.init;

import com.mysticalchemy.MysticAlchemy;
import com.mysticalchemy.crucible.TileEntityCrucible;

import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class TileEntityInit {
public static final DeferredRegister<TileEntityType<?>> TILE_ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.TILE_ENTITIES, MysticAlchemy.MODID);
	
	public static final RegistryObject<TileEntityType<TileEntityCrucible>> CRUCIBLE_TILE_TYPE = TILE_ENTITY_TYPES.register(
			"chalk_rune_tile_entity", 
			() -> TileEntityType.Builder.of(TileEntityCrucible::new, 
					BlockInit.CRUCIBLE.get()
			).build(null));
}
