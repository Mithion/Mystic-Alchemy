package com.mysticalchemy.crucible;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.CauldronBlock;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.PotionUtils;
import net.minecraft.potion.Potions;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class BlockCrucible extends CauldronBlock {
	public BlockCrucible() {
		super(Properties.of(Material.METAL).noOcclusion().strength(3.0f));
	}

	@Override
	public boolean hasTileEntity(BlockState state) {
		return true;
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return new TileEntityCrucible();
	}
	
	@Override
	public void entityInside(BlockState state, World worldIn, BlockPos pos, Entity entityIn) {
		int fillLevel = state.getValue(LEVEL);
		float insideYPos = pos.getY() + (6.0F + 3 * fillLevel) / 16.0F;
		if (!worldIn.isClientSide && fillLevel > 0 && entityIn.getY() <= insideYPos) {
			if (entityIn instanceof ItemEntity) {
				TileEntityCrucible crucible = (TileEntityCrucible) worldIn.getBlockEntity(pos);
				if (crucible != null) {
					ItemStack stack = ((ItemEntity) entityIn).getItem();
					if (crucible.tryAddIngredient(stack)) {
						worldIn.playSound(null, pos, SoundEvents.GENERIC_SPLASH, SoundCategory.BLOCKS, 1.0f,
								(float) (0.8f + Math.random() * 0.4f));
						entityIn.remove(false);
					} else {
						entityIn.push(-0.2 + Math.random() * 0.4, 1, -0.2 + Math.random() * 0.4);
					}
				}
			} else if (entityIn instanceof LivingEntity) {
				TileEntityCrucible crucible = (TileEntityCrucible) worldIn.getBlockEntity(pos);
				if (crucible != null && crucible.getHeat() > crucible.getMaxHeat() / 2) {
					((LivingEntity) entityIn).hurt(DamageSource.IN_FIRE, 1);
				}
			}
		}
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void animateTick(BlockState stateIn, World worldIn, BlockPos pos, Random rand) {
		if (worldIn.isClientSide && stateIn.getValue(LEVEL) > 0) {
			TileEntityCrucible crucible = (TileEntityCrucible) worldIn.getBlockEntity(pos);
			if (crucible != null && crucible.getHeat() > 0) {
				Minecraft mc = Minecraft.getInstance();
				worldIn.playSound(mc.player, pos, SoundEvents.LAVA_POP, SoundCategory.BLOCKS,
						1.0f, (float) (0.8f + Math.random() * 0.4f));
			}
		}
	}

	@Override
	public ActionResultType use(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
		TileEntityCrucible crucible = (TileEntityCrucible) worldIn.getBlockEntity(pos);
		if (crucible != null && state.getValue(LEVEL) > 0) {
			HashMap<Effect, Float> prominents = crucible.getProminentEffects();
			if (prominents.size() > 0) {
				// if there are prominents and the player is using a glass bottle, assume extracting current potion.
				if (player.getItemInHand(handIn).getItem() == Items.GLASS_BOTTLE) {
					extractPotion(worldIn, prominents, crucible, player, handIn, state, pos);
					return ActionResultType.SUCCESS;
				}
				// disable other interactions when there's a potion in the crucible
				return ActionResultType.SUCCESS;
			}
		}
		return super.use(state, worldIn, pos, player, handIn, hit);
	}
	
	private void extractPotion(World worldIn, HashMap<Effect, Float> prominents, TileEntityCrucible crucible, PlayerEntity player, Hand handIn, BlockState state, BlockPos pos) {
		if (!worldIn.isClientSide) {
			List<EffectInstance> prominentEffects = new ArrayList<EffectInstance>();
			
			ItemStack potionstack = createBasePotionStack(crucible);
			
			prominents.forEach((e, f) -> {
				prominentEffects.add(new EffectInstance(e, e.isInstantenous() ? 1 : crucible.getDuration(), (int) Math.floor(f - 1)));
			});
			PotionUtils.setPotion(potionstack, Potions.WATER);
			PotionUtils.setCustomEffects(potionstack, prominentEffects);

			if (prominentEffects.size() == 1)
				potionstack.setHoverName(new TranslationTextComponent(prominentEffects.get(0).getDescriptionId()));
			else
				potionstack.setHoverName(new TranslationTextComponent("item.mysticalchemy.concoction"));

			player.getItemInHand(handIn).shrink(1);
			if (!player.addItem(potionstack)) {
				player.drop(potionstack, false);
			}

			worldIn.setBlock(pos, state.setValue(LEVEL, state.getValue(LEVEL) - 1), 3);
		}
	}
	
	private ItemStack createBasePotionStack(TileEntityCrucible crucible) {
		Item outputPotionItem;
		
		if (crucible.isLingering()) {
			outputPotionItem = Items.LINGERING_POTION;
		}else if (crucible.isSplash()) {
			outputPotionItem = Items.SPLASH_POTION;
		}else {
			outputPotionItem = Items.POTION;
		}
		
		return new ItemStack(outputPotionItem);
	}
	
	@Override
	public void handleRain(World worldIn, BlockPos pos) {
		TileEntityCrucible crucible = (TileEntityCrucible) worldIn.getBlockEntity(pos);
		if (crucible != null && crucible.isPotion())
			return;

		super.handleRain(worldIn, pos);
	}
	
	@Override
	public BlockRenderType getRenderShape(BlockState state) {
		return BlockRenderType.MODEL;
	}

}
