package com.mysticalchemy.crucible;

import com.mysticalchemy.init.BlockInit;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;


public class ItemCrucibleSpoon extends Item {
	public ItemCrucibleSpoon() {
		super(new Item.Properties().stacksTo(1).durability(200).tab(CreativeModeTab.TAB_BREWING));
	}
	
	@Override
	public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
		Level world = context.getLevel();
		BlockState state = world.getBlockState(context.getClickedPos());
		if (state.getBlock() == BlockInit.CRUCIBLE.get()) {
			CrucibleTile crucible = (CrucibleTile)context.getLevel().getBlockEntity(context.getClickedPos());
			if (crucible != null) {
				if (!world.isClientSide && !context.getPlayer().isCreative())
					stack.hurt(1, world.random, (ServerPlayer) context.getPlayer());
				crucible.stir();
				world.playSound(context.getPlayer(), context.getClickedPos(), SoundEvents.PLAYER_SPLASH, SoundSource.BLOCKS, 1.0f, (float) (0.8f + Math.random() * 0.4f));
				return InteractionResult.SUCCESS;
			}
		}
		
		return super.onItemUseFirst(stack, context);
	}
}
