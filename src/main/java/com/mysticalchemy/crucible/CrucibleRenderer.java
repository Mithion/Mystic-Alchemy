package com.mysticalchemy.crucible;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Matrix4f;

public class CrucibleRenderer extends TileEntityRenderer<TileEntityCrucible>{

	public CrucibleRenderer(TileEntityRendererDispatcher rendererDispatcherIn) {
		super(rendererDispatcherIn);
	}

	@Override
	public void render(TileEntityCrucible tileEntityIn, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn) {
		if (tileEntityIn.getBlockState().getValue(BlockCrucible.LEVEL) > 0) {
			IVertexBuilder builder = bufferIn.getBuffer(RenderType.beaconBeam(new ResourceLocation("textures/block/water_still.png"), true));
			long color = tileEntityIn.getPotionColor();
			float[] rgb = colorFromLong(color);
			
			Matrix4f mat = matrixStackIn.last().pose();
			int frames = 16;
			float frameSize = 1f / frames;
			long frame = (tileEntityIn.getLevel().getGameTime() / 3) % frames;
			float min_u = 0;
			float max_u = 1;
			float min_v = (frameSize * frame);
			float max_v = (frameSize * (frame+1));
			
			
			float yPos = 0.2f + 0.25f * tileEntityIn.getBlockState().getValue(BlockCrucible.LEVEL);
			
			addVertex(builder, mat, 1, yPos, 0, max_u, min_v, rgb, combinedLightIn);
			addVertex(builder, mat, 0, yPos, 0, min_u, min_v, rgb, combinedLightIn);
			addVertex(builder, mat, 0, yPos, 1, min_u, max_v, rgb, combinedLightIn);
			addVertex(builder, mat, 1, yPos, 1, max_u, max_v, rgb, combinedLightIn);
		}
	}
	
	private float[] colorFromLong(long color) {
		return new float[] {
			((color >> 16) & 0xFF) / 255.0f,
			((color >> 8) & 0xFF) / 255.0f,
			(color & 0xFF) / 255.0f
		};
	}
	
	private static void addVertex(IVertexBuilder builder, Matrix4f pos, float x, float y, float z, float u, float v, float[] rgb, int combinedLightIn) {
		builder		
			.vertex(pos, x, y, z)
			.color(rgb[0], rgb[1], rgb[2], 1f)
			.uv(u, v)
			.overlayCoords(OverlayTexture.NO_OVERLAY)
			.uv2(combinedLightIn)
			.normal(0, 1, 0)			
		.endVertex();
	}

}
