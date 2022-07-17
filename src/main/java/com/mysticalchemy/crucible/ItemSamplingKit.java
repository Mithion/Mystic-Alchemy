package com.mysticalchemy.crucible;

import java.util.HashMap;

import com.mysticalchemy.init.BlockInit;

import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.state.BlockState;

public class ItemSamplingKit extends Item {
	//threshold controls what level of ingredients within the potion this will reveal.
	//the idea is to have a couple levels of this kit, at different expense, that will give different levels of detail
	private float threshold;
	
	public ItemSamplingKit(float threshold) {
		super(new Item.Properties().tab(CreativeModeTab.TAB_BREWING));
		this.threshold = threshold;
	}
	
	@Override
	public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
		BlockState state = context.getLevel().getBlockState(context.getClickedPos());
		if (state.getBlock() == BlockInit.CRUCIBLE.get() && !context.getLevel().isClientSide) {
			//get the TE so we can get the potion
			CrucibleTile crucible = (CrucibleTile) context.getLevel().getBlockEntity(context.getClickedPos());
			if (crucible == null) //...not good, cheese it!
				return InteractionResult.FAIL;
			
			//remove the kit
			if (!context.getPlayer().isCreative())
				stack.shrink(1);
			
			//get the current potion
			HashMap<MobEffect, Float> allEffects = crucible.getAllEffects();
			
			//spam the player with messages!
			//first, handle no effects
			if (allEffects.size() == 0) {
				context.getPlayer().sendMessage(new TranslatableComponent("chat.mysticalchemy.no_effects"), Util.NIL_UUID);
				return InteractionResult.SUCCESS;
			}
			
			//ensure the test can read the effects
			if (allEffects.values().stream().noneMatch(f -> f >= this.threshold)) {
				context.getPlayer().sendMessage(new TranslatableComponent("chat.mysticalchemy.no_notable_effects"), Util.NIL_UUID);
				return InteractionResult.SUCCESS;
			}
			
			//list out all effects greater than or equal to our threshold
			allEffects.forEach((e,f) -> {
				if (f >= this.threshold) {			
					Component ttc = new TranslatableComponent("chat.mysticalchemy.format_effect", String.format("%.2f", f), e.getDisplayName().getString()).withStyle(f >= 1 ? ChatFormatting.GREEN : ChatFormatting.DARK_RED);					
					context.getPlayer().sendMessage(ttc, Util.NIL_UUID);
				}
			});
			
			//and done!
			return InteractionResult.SUCCESS;
		}
		
		//if here, pass as this isn't something this item handles
		return InteractionResult.PASS;
	}
}
