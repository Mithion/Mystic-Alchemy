package com.mysticalchemy.init;

import com.mysticalchemy.crucible.CrucibleRenderer;

import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public class TileEntityClientInit {
	@SubscribeEvent
	public static void onClientSetupEvent(FMLClientSetupEvent event) {		
		ClientRegistry.bindTileEntityRenderer(
				TileEntityInit.CRUCIBLE_TILE_TYPE.get(), 
				CrucibleRenderer::new
		);
	}
}
