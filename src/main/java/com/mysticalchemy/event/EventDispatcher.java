package com.mysticalchemy.event;

import com.mysticalchemy.api.events.CrucibleEvent;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.Event;

import java.util.HashMap;

public class EventDispatcher {
    public static Event.Result DispatchCrucibleAddIngredientEvent(HashMap<MobEffect, Float> effects, ItemStack stack) {
        CrucibleEvent.AddIngredient event = new CrucibleEvent.AddIngredient(effects, stack);
        MinecraftForge.EVENT_BUS.post(event);
        return event.getResult();
    }
}
