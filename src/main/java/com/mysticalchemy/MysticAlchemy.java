package com.mysticalchemy;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mysticalchemy.config.Config;
import com.mysticalchemy.init.BlockInit;
import com.mysticalchemy.init.ItemInit;
import com.mysticalchemy.init.RecipeInit;
import com.mysticalchemy.init.TileEntityClientInit;
import com.mysticalchemy.init.TileEntityInit;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;

// The value here should match an entry in the META-INF/mods.toml file
@Mod("mysticalchemy")
public class MysticAlchemy {
	public static final String MODID = "mysticalchemy";
	public static final Logger LOGGER = LogManager.getLogger();
	final IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

	public MysticAlchemy() {
		final boolean HIDE_CONSOLE_NOISE = false; 
		if (HIDE_CONSOLE_NOISE) {
			ForgeLoggerTweaker.setMinimumLevel(Level.WARN);
			ForgeLoggerTweaker.applyLoggerFilter();
		}
		
		//load config
		Config.loadConfig(Config.CONFIG, FMLPaths.CONFIGDIR.get().resolve("mystic-alchemy-brewing-config.toml"));

		ItemInit.ITEMS.register(modEventBus);
		BlockInit.BLOCKS.register(modEventBus);
		TileEntityInit.TILE_ENTITY_TYPES.register(modEventBus);
		RecipeInit.SERIALIZERS.register(modEventBus);
		
		DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
			modEventBus.register(TileEntityClientInit.class);
		});
	}
}
