package com.mysticalchemy.crucible;

import java.util.HashMap;

import com.mysticalchemy.init.BlockInit;

import net.minecraft.block.BlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.potion.Effect;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Util;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

public class ItemSamplingKit extends Item {
	//threshold controls what level of ingredients within the potion this will reveal.
	//the idea is to have a couple levels of this kit, at different expense, that will give different levels of detail
	private float threshold;
	
	public ItemSamplingKit(float threshold) {
		super(new Item.Properties().group(ItemGroup.BREWING));
		this.threshold = threshold;
	}
	
	@Override
	public ActionResultType onItemUseFirst(ItemStack stack, ItemUseContext context) {
		BlockState state = context.getWorld().getBlockState(context.getPos());
		if (state.getBlock() == BlockInit.CRUCIBLE.get() && !context.getWorld().isRemote) {
			//get the TE so we can get the potion
			TileEntityCrucible crucible = (TileEntityCrucible) context.getWorld().getTileEntity(context.getPos());
			if (crucible == null) //...not good, cheese it!
				return ActionResultType.FAIL;
			
			//remove the kit
			if (!context.getPlayer().isCreative())
				stack.shrink(1);
			
			//get the current potion
			HashMap<Effect, Float> allEffects = crucible.getAllEffects();
			
			//spam the player with messages!
			//first, handle no effects
			if (allEffects.size() == 0) {
				context.getPlayer().sendMessage(new TranslationTextComponent("chat.mysticalchemy.no_effects"), Util.field_240973_b_);
				return ActionResultType.SUCCESS;
			}
			
			//list out all effects greater than or equal to our threshold
			allEffects.forEach((e,f) -> {
				if (f >= this.threshold) {			
					IFormattableTextComponent ttc = new TranslationTextComponent("chat.mysticalchemy.format_effect", String.format("%.2f", f), e.getDisplayName().getString());
					if (f >= 1)
						ttc = ttc.func_240699_a_(TextFormatting.GREEN);
					else
						ttc = ttc.func_240699_a_(TextFormatting.DARK_RED);
					
					context.getPlayer().sendMessage(ttc, Util.field_240973_b_);
				}
			});
			
			//and done!
			return ActionResultType.SUCCESS;
		}
		
		//if here, pass as this isn't something this item handles
		return ActionResultType.PASS;
	}
}
