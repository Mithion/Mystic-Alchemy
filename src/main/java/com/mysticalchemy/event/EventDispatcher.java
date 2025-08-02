package com.mysticalchemy.event;

import com.mysticalchemy.api.events.CrucibleEvent;
import com.mysticalchemy.crucible.CrucibleTile;
import com.mysticalchemy.recipe.PotionIngredientRecipe;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.Event;

import java.util.HashMap;

public class EventDispatcher {
    public static CrucibleEvent.AddIngredient DispatchCrucibleAddIngredientEvent(HashMap<MobEffect, Float> effects, PotionIngredientRecipe recipe, ItemStack stack) {
        CrucibleEvent.AddIngredient event = new CrucibleEvent.AddIngredient(effects, recipe, stack);
        MinecraftForge.EVENT_BUS.post(event);
        return event;
    }

    public static void DispatchCrucibleExtractPotionEvent(ItemStack potionstack, Player player) {
        CrucibleEvent.ExtractPotion event = new CrucibleEvent.ExtractPotion(potionstack, player);
        MinecraftForge.EVENT_BUS.post(event);
    }
}
