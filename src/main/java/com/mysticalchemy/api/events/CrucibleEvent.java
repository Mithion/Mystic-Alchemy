package com.mysticalchemy.api.events;

import com.mysticalchemy.recipe.PotionIngredientRecipe;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

import java.util.HashMap;

/**
 * This event is fired when the crucible is used.
 */
public class CrucibleEvent extends Event {

    public CrucibleEvent() {}


    /**
     * CrucibleEvent.AddIngredient is fired when an ingredient is added to the crucible.<br>
     * <br>
     * {@link #effects} contains the effects already stored in the crucible.<br>
     * {@link #recipe} contains the {@code potion_ingredient recipe} that is being added to the crucible.<br>
     * {@link #stack} contains the ItemStack that is being added to the crucible.<br>
     * <br>
     * The recipe can be modified during the event's execution to affect what happens when the ingredient is added.<br>
     * The stack size can also be increased or decreased during the event's execution.<br>
     * <br>
     * This event has a {@link HasResult result}:<br>
     * <ul>
     * <li>{@link Result#ALLOW} means this ingredient can be added.</li>
     * <li>{@link Result#DEFAULT} means the {@code potion_ingredient recipe} is used to determine the behaviour.</li>
     * <li>{@link Result#DENY} means this ingredient cannot be added.</li>
     * </ul>
     */
    @HasResult
    public static class AddIngredient extends CrucibleEvent {
        private HashMap<MobEffect, Float> effects = new HashMap<>();
        private final PotionIngredientRecipe recipe;
        private final ItemStack stack;


        public AddIngredient(HashMap<MobEffect, Float> effects, PotionIngredientRecipe recipe, ItemStack stack) {
            super();
            this.effects = effects;
            this.recipe = recipe;
            this.stack = stack;
        }

        public HashMap<MobEffect, Float> getEffects() {
            return effects;
        }
        public PotionIngredientRecipe getRecipe() {
            return recipe;
        }
        public ItemStack getStack() {
            return stack;
        }
    }


    /**
     * CrucibleEvent.ExtractPotion is fired when a player extracts a potion from the crucible.<br>
     * <br>
     * {@link #stack} contains the potion that is being extracted from the crucible.<br>
     * {@link #player} contains the Player that is extracting the potion.<br>
     * <br>
     * The stack can be modified during the event's execution to affect what potion is extracted.<br>
     * <br>
     * This event has no {@link HasResult result}.
     */
    public static class ExtractPotion extends CrucibleEvent {
        private final ItemStack stack;
        private final Player player;

        public ExtractPotion(ItemStack potionstack, Player player) {
            super();
            this.stack = potionstack;
            this.player = player;
        }

        public ItemStack getStack() {
            return stack;
        }

        public Player getPlayer() {
            return player;
        }
    }
}
