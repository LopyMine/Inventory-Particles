package net.lopymine.ip.renderer;

//? if <=1.21.1 {
/*import com.mojang.blaze3d.vertex.*;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import java.util.*;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
*///?}

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

public class OptimizedDrawer {

	public static void drawParticleSprite(
			GuiGraphics context,
			TextureAtlasSprite sprite,
			int x,
			int y,
			float width,
			float height,
			int color
	) {
		//? if >=1.21.2 && <=1.21.4 {
		/*//com.mojang.blaze3d.systems.RenderSystem.enableDepthTest();
		com.mojang.blaze3d.systems.RenderSystem.enableBlend();
		*///?}

		//? if >=1.21.2 {
		context.blitSprite(
				/*? if >=1.21.6 {*/ net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, /*?} elif >=1.21.2 {*/ /*net.minecraft.client.renderer.RenderType::guiTextured,*//*?}*/
				sprite,
				x,
				y,
				(int) width,
				(int) height,
				color
		);
		//?} else {
		/*PER_ATLAS_CONTEXTS.computeIfAbsent(sprite.atlasLocation(), (key) -> new ArrayList<>())
				.add(new RenderContext(context.pose().last().pose(), sprite, x, y, width, height, color));
		*///?}

		//? if >=1.21.2 && <=1.21.4 {
		/*//com.mojang.blaze3d.systems.RenderSystem.disableDepthTest();
		com.mojang.blaze3d.systems.RenderSystem.disableBlend();
		*///?}
	}

	//? if <=1.21.1 {
	/*@Nullable
	private static BufferBuilder PARTICLES_BUFFER = null;

	private static final Map<Identifier, List<RenderContext>> PER_ATLAS_CONTEXTS = new HashMap<>();

	public static void endDrawing() {
		if (PER_ATLAS_CONTEXTS.isEmpty()) {
			return;
		}

		PER_ATLAS_CONTEXTS.forEach((atlas, contexts) -> {
			if (contexts.isEmpty()) {
				return;
			}

			prepareParticlesBuffer(atlas);
			for (RenderContext context : contexts) {
				context.render();
			}
			endParticlesBuffer();
			contexts.clear();
		});
	}

	private static void prepareParticlesBuffer(Identifier atlasId) {
		if (PARTICLES_BUFFER != null) {
			endParticlesBuffer();
		}
		com.mojang.blaze3d.systems.RenderSystem.disableDepthTest();
		com.mojang.blaze3d.systems.RenderSystem.enableBlend();
		com.mojang.blaze3d.systems.RenderSystem.setShaderTexture(0, atlasId);
		com.mojang.blaze3d.systems.RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
		//? if >=1.21 {
		PARTICLES_BUFFER = Tesselator.getInstance().begin(Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
		//?} else {
		/^BufferBuilder buffer = Tesselator.getInstance().getBuilder();
		buffer.begin(Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
		PARTICLES_BUFFER = buffer;
		^///?}
	}

	private static void endParticlesBuffer() {
		if (PARTICLES_BUFFER == null) {
			return;
		}
		//? if >=1.21 {
		BufferUploader.drawWithShader(PARTICLES_BUFFER.buildOrThrow());
		//?} else {
		/^BufferUploader.drawWithShader(PARTICLES_BUFFER.end());
		^///?}
		com.mojang.blaze3d.systems.RenderSystem.disableBlend();
		com.mojang.blaze3d.systems.RenderSystem.enableDepthTest();
		PARTICLES_BUFFER = null;
	}

	private record RenderContext(
			Matrix4f matrix4f,
			TextureAtlasSprite sprite,
			int x,
			int y,
			float width,
			float height,
			int color
	) {

		public void render() {
			if (PARTICLES_BUFFER == null) {
				return;
			}
			float x2 = this.x + this.width;
			float y2 = this.y + this.height;
			float z = 0F;
			float u1 = this.sprite.getU0();
			float u2 = this.sprite.getU1();
			float v1 = this.sprite.getV0();
			float v2 = this.sprite.getV1();

			//? if >=1.21 {
			PARTICLES_BUFFER.addVertex(this.matrix4f, (float) this.x, (float) this.y, z).setUv(u1, v1).setColor(this.color);
			PARTICLES_BUFFER.addVertex(this.matrix4f, (float) this.x, y2, z).setUv(u1, v2).setColor(this.color);
			PARTICLES_BUFFER.addVertex(this.matrix4f, x2, y2, z).setUv(u2, v2).setColor(this.color);
			PARTICLES_BUFFER.addVertex(this.matrix4f, x2, (float) this.y, z).setUv(u2, v1).setColor(this.color);
			//?} else {
			/^PARTICLES_BUFFER.vertex(this.matrix4f, (float) this.x, (float) this.y, z).uv(u1, v1).color(this.color).endVertex();
			PARTICLES_BUFFER.vertex(this.matrix4f, (float) this.x, y2, z).uv(u1, v2).color(this.color).endVertex();
			PARTICLES_BUFFER.vertex(this.matrix4f, x2, y2, z).uv(u2, v2).color(this.color).endVertex();
			PARTICLES_BUFFER.vertex(this.matrix4f, x2, (float) this.y, z).uv(u2, v1).color(this.color).endVertex();
			^///?}
		}

	}

	*///?} else {
	public static void endDrawing() {}
	//?}
}
