package com.mysticalchemy.config;

import java.util.ArrayList;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;

public class BrewingConfig {
	public static ForgeConfigSpec.ConfigValue<String> disallowed_effects;
	
	private static ArrayList<String> disallowed_effects_parsed;
	
	public static void init(ForgeConfigSpec.Builder serverBuilder) {
		serverBuilder.comment("Mystic Alchemy // Disallowed Effects");	
		disallowed_effects = serverBuilder.comment("Disallow the following effects (comma separated potion identifiers, ex minecraft:nausea)").define("ma_disallowed_effects", "");		
	}
	
	public static boolean isEffectDisabled(ResourceLocation effectID) {
		if (disallowed_effects_parsed == null) {
			disallowed_effects_parsed = new ArrayList<String>();
			for (String s : disallowed_effects.get().split(","))
				disallowed_effects_parsed.add(s);
		}
		
		return disallowed_effects_parsed.contains(effectID.toString());
	}
}
