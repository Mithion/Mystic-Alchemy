package com.mysticalchemy.crucible;

import com.mysticalchemy.init.BlockInit;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.world.World;

public class ItemCrucibleSpoon extends Item {
	public ItemCrucibleSpoon() {
		super(new Item.Properties().stacksTo(1).durability(200).tab(ItemGroup.TAB_BREWING));
	}
	
	@Override
	public ActionResultType onItemUseFirst(ItemStack stack, ItemUseContext context) {
		World world = context.getLevel();
		BlockState state = world.getBlockState(context.getClickedPos());
		if (state.getBlock() == BlockInit.CRUCIBLE.get()) {
			TileEntityCrucible crucible = (TileEntityCrucible)context.getLevel().getBlockEntity(context.getClickedPos());
			if (crucible != null) {
				if (!world.isClientSide && !context.getPlayer().isCreative())
					stack.hurt(1, world.random, (ServerPlayerEntity) context.getPlayer());
				crucible.stir();
				world.playSound(context.getPlayer(), context.getClickedPos(), SoundEvents.PLAYER_SPLASH, SoundCategory.BLOCKS, 1.0f, (float) (0.8f + Math.random() * 0.4f));
				return ActionResultType.SUCCESS;
			}
		}
		
		return super.onItemUseFirst(stack, context);
	}
}
