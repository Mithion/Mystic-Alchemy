package com.mysticalchemy.compat;

import com.mysticalchemy.MysticAlchemy;
import com.mysticalchemy.recipe.PotionIngredientRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mod.EventBusSubscriber(modid = MysticAlchemy.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class DeferredEffectLoader {
    // Store all recipes with unresolved effects
    private static final List<PendingRecipe> pendingRecipes = new ArrayList<>();
    
    // Store all tile entities with unresolved effects  
    private static final List<PendingTileData> pendingTileData = new ArrayList<>();
    
    public static class PendingRecipe {
        public final PotionIngredientRecipe recipe;
        public final Map<ResourceLocation, Float> effectsToResolve;
        
        public PendingRecipe(PotionIngredientRecipe recipe, Map<ResourceLocation, Float> effectsToResolve) {
            this.recipe = recipe;
            this.effectsToResolve = effectsToResolve;
        }
    }
    
    public static class PendingTileData {
        public final Map<ResourceLocation, Float> effectsToResolve;
        public final Map<MobEffect, Float> targetMap;
        
        public PendingTileData(Map<ResourceLocation, Float> effectsToResolve, Map<MobEffect, Float> targetMap) {
            this.effectsToResolve = effectsToResolve;
            this.targetMap = targetMap;
        }
    }
    
    /**
     * Register a recipe that needs effect resolution after all mods load
     */
    public static void deferRecipe(PotionIngredientRecipe recipe, Map<ResourceLocation, Float> effects) {
        pendingRecipes.add(new PendingRecipe(recipe, effects));
    }
    
    /**
     * Register tile entity data that needs effect resolution after all mods load
     */
    public static void deferTileData(Map<ResourceLocation, Float> effects, Map<MobEffect, Float> targetMap) {
        pendingTileData.add(new PendingTileData(effects, targetMap));
    }
    
    @SubscribeEvent
    public static void onLoadComplete(FMLLoadCompleteEvent event) {
        event.enqueueWork(() -> {
            MysticAlchemy.LOGGER.info("Resolving deferred effects for {} recipes and {} tile entities", 
                pendingRecipes.size(), pendingTileData.size());
            
            // Resolve recipe effects
            for (PendingRecipe pending : pendingRecipes) {
                HashMap<MobEffect, Float> existingEffects = pending.recipe.getEffects();
                if (existingEffects == null) {
                    existingEffects = new HashMap<>();
                    pending.recipe.setEffects(existingEffects);
                }
                
                final HashMap<MobEffect, Float> effectsMap = existingEffects;
                pending.effectsToResolve.forEach((effectId, strength) -> {
                    MobEffect effect = ForgeRegistries.MOB_EFFECTS.getValue(effectId);
                    if (effect != null) {
                        effectsMap.put(effect, strength);
                    } else {
                        MysticAlchemy.LOGGER.warn("Failed to resolve potion effect {} in recipe {}", 
                            effectId, pending.recipe.getId());
                    }
                });
            }
            
            // Resolve tile entity effects
            for (PendingTileData pending : pendingTileData) {
                pending.effectsToResolve.forEach((effectId, strength) -> {
                    MobEffect effect = ForgeRegistries.MOB_EFFECTS.getValue(effectId);
                    if (effect != null) {
                        pending.targetMap.put(effect, strength);
                    } else {
                        MysticAlchemy.LOGGER.warn("Failed to resolve potion effect {} in tile entity data", effectId);
                    }
                });
            }
            
            // Clear pending lists
            pendingRecipes.clear();
            pendingTileData.clear();
        });
    }
}