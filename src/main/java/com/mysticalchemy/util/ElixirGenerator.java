package com.mysticalchemy.util;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;

public class ElixirGenerator {

    public static String generateCustomName(List<MobEffectInstance> effects) {
        String prefix = null;
        String suffix = null;

        for (MobEffectInstance effect : effects) {
            ResourceLocation id = ForgeRegistries.MOB_EFFECTS.getKey(effect.getEffect());
            if (id == null) continue;
            String key = "effect.elixir." + id.getPath();

            if (isPositiveEffect(id)) {
                prefix = Component.translatable(key).getString();
            } else if (isNegativeEffect(id)) {
                suffix = Component.translatable(key).getString();
            }
        }

        if (prefix != null && suffix != null)
            return "Elixir of " + prefix + " and " + suffix;
        else if (prefix != null)
            return "Elixir of " + prefix;
        else if (suffix != null)
            return "Elixir of " + suffix;
        else
            return null;
    }

    private static boolean isPositiveEffect(ResourceLocation id) {
        return switch (id.getPath()) {
            case "absorption", "conduit_power", "fire_resistance", "haste", "health_boost",
                 "hero_of_the_village", "invisibility", "jump_boost", "luck", "night_vision",
                 "regeneration", "resistance", "saturation", "slow_falling", "speed", "strength",
                 "water_breathing", "instant_mana", "mana_boost", "mana_regen", "arcane_vision",
                 "arcane_ward", "etheral", "warding", "magic_resistance", "mana_shield" -> true;
            default -> false;
        };
    }

    private static boolean isNegativeEffect(ResourceLocation id) {
        return switch (id.getPath()) {
            case "bad_omen", "blindness", "darkness", "hunger", "levitation", "mining_fatigue",
                 "nausea", "poison", "slowness", "unluck", "weakness", "wither",
                 "mana_burn", "mana_lock", "silence", "fragile" -> true;
            default -> false;
        };
    }
}
