package com.mysticalchemy.api.events;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.Event;

import java.util.HashMap;

/**
 * This event is fired when the crucible is used.
 */
public class CrucibleEvent extends Event {
    private HashMap<MobEffect, Float> effects = new HashMap<>();

    public CrucibleEvent(HashMap<MobEffect, Float> effects) {
        this.effects = effects;
    }

    public HashMap<MobEffect, Float> getEffects() {
        return effects;
    }


    /**
     * CrucibleEvent.AddIngredient is fired when an ingredient is added to the crucible.<br>
     * <br>
     * {@link #effects} contains the effects already stored in the crucible.<br>
     * {@link #stack} contains the ItemStack that is being added to the crucible.<br>
     * <br>
     * This event has a result {@link HasResult result}:<br>
     * <ul>
     * <li>{@link Result#ALLOW} means this ingredient can be added.</li>
     * <li>{@link Result#DEFAULT} means the {@code potion_ingredient recipe} is used to determine the behaviour.</li>
     * <li>{@link Result#DENY} means this ingredient cannot be added.</li>
     * </ul>
     */
    @HasResult
    public static class AddIngredient extends CrucibleEvent {
        private final ItemStack stack;

        public AddIngredient(HashMap<MobEffect, Float> effects, ItemStack stack) {
            super(effects);
            this.stack = stack;
        }

        public ItemStack getStack() {
            return stack;
        }
    }
}
