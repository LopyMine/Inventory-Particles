package net.lopymine.ip.client.renderer;

import java.lang.reflect.*;
import java.util.*;
import java.util.function.*;
import java.util.stream.Stream;
import java.util.stream.Stream.Builder;
import net.lopymine.ip.client.InventoryParticlesClient;
import net.lopymine.ip.debug.*;
import net.lopymine.ip.utils.ArgbUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.Formatting;

public abstract class AbstractDebugInfoRenderer {

	private final Map<String, ISpecialFieldDebugRenderer> specialFieldRenderers = new HashMap<>();
	private String lastError;
	protected int yOffset = 5;
	protected int xOffset = 5;

	protected void registerSpecialFieldDebugRenderer(String name, ISpecialFieldDebugRenderer renderable) {
		this.specialFieldRenderers.put(name, renderable);
	}

	public void render(DrawContext context, Class<?> clazz, Object clazzInstance) {
		this.yOffset = 5;
		this.xOffset = 5;
		this.renderDecoration(context, "[" + this.getRendererName() + "]");
		this.renderClassFields(
				context,
				clazz,
				clazzInstance,
				(name, value) -> this.renderFieldData(context, name, value),
				(decoration) -> this.renderDecoration(context, decoration)
		);
	}

	private void renderClassFields(DrawContext context, Class<?> clazz, Object clazzInstance, BiConsumer<String, Object> renderFieldData, Consumer<String> renderDecoration) {
		Builder<Field> builder = Stream.builder();

		Class<?> c = clazz;
		do {
			for (Field field : c.getDeclaredFields()) {
				builder.add(field);
			}
			c = c.getSuperclass();
		} while (c != null);

		builder.build().filter((field) -> {
			int modifiers = field.getModifiers();
			if (Modifier.isStatic(modifiers) && Modifier.isFinal(modifiers)) {
				return false;
			}
			return !field.isAnnotationPresent(HideInDebugRender.class);
		}).forEach((field) -> {
			try {
				String name = field.getName();
				field.setAccessible(true);
				Object value = field.get(clazzInstance);

				if (value instanceof IDebugRenderable) {
					renderDecoration.accept("%s[%s]".formatted(value.getClass().getSimpleName(), name));
					this.xOffset += this.getTextXOffset();
					this.renderClassFields(context, value.getClass(), value, renderFieldData, renderDecoration);
					this.xOffset -= this.getTextXOffset();
					return;
				}

				ISpecialFieldDebugRenderer specialFieldRenderer = this.specialFieldRenderers.get(name);
				if (specialFieldRenderer != null) {
					specialFieldRenderer.consumeDebugRender(renderFieldData, renderDecoration, value);
				}
//				else if (value instanceof Collection<?> collection) {
//					renderDecoration.accept("]");
//					Iterator<?> iterator = collection.iterator();
//					while (iterator.hasNext()) {
//						Object next = iterator.next();
//						this.xOffset += this.getTextXOffset();
//						this.renderFieldData(context, name, next);
//						this.xOffset -= this.getTextXOffset();
//						if (iterator.hasNext()) {
//							renderDecoration.accept(",");
//						}
//					}
//					renderDecoration.accept("[");
//				}
				else {
					this.renderFieldData(context, name, value);
				}

			} catch (Exception e) {
				String error = e.toString();
				if (!error.equals(this.lastError)) {
					this.lastError = error;
					InventoryParticlesClient.LOGGER.error("Failed to render debug info for field \"{}\" in \"{}\"! Reason:", this.getClass().getSimpleName(), field.getName(), e);
				}
			}
		});
	}

	protected void renderFieldData(DrawContext context, String name, Object text) {
		String string = name + ": " + (text == null ? "NULL" : text.toString());
		this.renderLabel(context, string, Formatting.WHITE);
	}

	protected void renderDecoration(DrawContext context, String label) {
		this.renderLabel(context, label, Formatting.GRAY);
	}

	protected void renderLabel(DrawContext context, String label, Formatting color) {
		TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
		Screen screen = MinecraftClient.getInstance().currentScreen;
		if (screen == null) {
			return;
		}
		context.drawText(
				textRenderer,
				label,
				this.getTextX(screen, textRenderer, label),
				this.getTextY(),
				color.getColorValue() == null ? -1 : ArgbUtils.fullAlpha(color.getColorValue()),
				true
		);

		this.yOffset += textRenderer.fontHeight + 1;
	}

	protected int getTextXOffset() {
		return 10;
	}

	protected int getTextY() {
		return this.yOffset;
	}

	protected int getTextX(Screen screen, TextRenderer textRenderer, String string) {
		return this.xOffset;
	}

	protected abstract String getRendererName();

	public abstract void render(DrawContext context);
}
