package com.mysticalchemy.crucible;

import com.mysticalchemy.init.BlockInit;

import net.minecraft.core.BlockPos;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.AbstractCauldronBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;

public class BlockEmptyCrucible extends AbstractCauldronBlock{
	private static final float RAIN_FILL_CHANCE = 0.05F;

	public BlockEmptyCrucible() {
		super(Properties.of(Material.METAL).noOcclusion().strength(3.0f), CauldronInteraction.EMPTY);
	}
	
	@Override
	public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
	      ItemStack itemstack = pPlayer.getItemInHand(pHand);
	      CauldronInteraction cauldroninteraction = CauldronInteraction.WATER.get(itemstack.getItem());
	      if (cauldroninteraction != null) {
	    	  InteractionResult res = cauldroninteraction.interact(pState, pLevel, pPos, pPlayer, pHand, itemstack);
	    	  if (pLevel.getBlockState(pPos).getBlock() == Blocks.WATER_CAULDRON) {
	    		  pLevel.setBlock(pPos, BlockInit.CRUCIBLE.get().defaultBlockState().setValue(LayeredCauldronBlock.LEVEL, 3), UPDATE_ALL);
	    	  }
	    	  return res;
	      }
	      return InteractionResult.FAIL;
	   }

	@Override
	public boolean isFull(BlockState pState) {
		return false;
	}

	protected static boolean shouldHandlePrecipitation(Level pLevel, Biome.Precipitation pPrecipitation) {
		if (pPrecipitation == Biome.Precipitation.RAIN) {
			return pLevel.getRandom().nextFloat() < RAIN_FILL_CHANCE;
		}else {
			return false;
		}
	}

	@Override
	public void handlePrecipitation(BlockState pState, Level pLevel, BlockPos pPos,
			Biome.Precipitation pPrecipitation) {
		if (shouldHandlePrecipitation(pLevel, pPrecipitation)) {
			if (pPrecipitation == Biome.Precipitation.RAIN) {
				pLevel.setBlockAndUpdate(pPos, BlockInit.CRUCIBLE.get().defaultBlockState().setValue(LayeredCauldronBlock.LEVEL, 1));
				pLevel.gameEvent((Entity) null, GameEvent.FLUID_PLACE, pPos);
			}
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	protected boolean canReceiveStalactiteDrip(Fluid pFluid) {
		return pFluid.is(FluidTags.WATER);
	}

	@Override
	protected void receiveStalactiteDrip(BlockState pState, Level pLevel, BlockPos pPos, Fluid pFluid) {
		if (pFluid == Fluids.WATER) {
			pLevel.setBlockAndUpdate(pPos, BlockInit.CRUCIBLE.get().defaultBlockState().setValue(LayeredCauldronBlock.LEVEL, 1));
			pLevel.levelEvent(1047, pPos, 0);
			pLevel.gameEvent((Entity) null, GameEvent.FLUID_PLACE, pPos);
		}
	}
}
