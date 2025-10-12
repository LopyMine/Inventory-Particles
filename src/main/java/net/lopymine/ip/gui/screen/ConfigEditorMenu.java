package net.lopymine.ip.gui.screen;

import java.util.*;
import java.util.Map.Entry;
import java.util.function.*;
import net.lopymine.ip.config.particle.ParticleHolder;
import net.lopymine.ip.element.InventoryCursor;
import net.lopymine.ip.gui.config.ConfigOptionEditorWidget;
import net.lopymine.ip.gui.config.ConfigOptionEditorWidget.OptionEditor;
import net.lopymine.ip.gui.list.*;
import net.lopymine.ip.gui.widget.ClippedPanelWidget;
import net.lopymine.ip.renderer.InventoryParticlesRenderer;
import net.lopymine.ip.resourcepack.ResourcePackParticleConfigsManager;
import net.lopymine.mossylib.gui.*;
import net.lopymine.mossylib.yacl.custom.MossyScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.*;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ConfigEditorMenu extends Screen implements MossyScreen {

	private Area listArea;
	private Area showcaseArea;
	private ResourcePackEntriesListWidget listWidget;
	private ItemStack currentItem = ItemStack.EMPTY;

	public ConfigEditorMenu() {
		super(Text.of(""));
	}

	@Override
	protected void init() {
		int offset = 10;

		int listWidth = (this.width / 5);
		int listHeight = this.height - offset * 2;
		int scrollBarWidth = 2 + 6 + 2;
		this.listArea = new Area(this.width - listWidth - offset - scrollBarWidth, 10, listWidth, listHeight);

		TextFieldWidget searchWidget = new TextFieldWidget(MinecraftClient.getInstance().textRenderer, this.listArea.getX(), this.listArea.getY(), this.listArea.getWidth(), 20, Text.of(""));
		searchWidget.setPlaceholder(Text.of("Search"));
		this.addDrawableChild(searchWidget);

		int a = searchWidget.getHeight() + offset;
		this.listArea.offsetY(a);
		this.listArea.expandHeight(-a);
		this.listWidget = new ResourcePackEntriesListWidget(this.listArea.getX(), this.listArea.getY(), this.listArea.getWidth(), this.listArea.getHeight(), 20);
		searchWidget.setChangedListener(this.listWidget::search);
		this.addDrawableChild(this.listWidget);

		Area panelArea = new Area(offset, offset, (int) (this.listArea.getWidth() * 3F), listHeight);
		int showcaseHeight = panelArea.getHeight() / -3;
		panelArea.expandHeight(showcaseHeight - offset);
		this.showcaseArea = new Area(panelArea.getX(), panelArea.getBottom() + offset, panelArea.getWidth(), showcaseHeight * -1);

		ClippedPanelWidget panelWidget = new ClippedPanelWidget(panelArea.getX(), panelArea.getY(), panelArea.getWidth(), panelArea.getHeight());

		OptionEditor<ParticleHolder, String> min = new OptionEditor<>() {
			@Override
			public void set(ParticleHolder config, String value) {
				config.getSpawnFrequency().setMin(Integer.parseInt(value));
			}

			@Override
			public String get(ParticleHolder config) {
				return String.valueOf(config.getSpawnFrequency().getMin());
			}

			@Override
			public Text getName() {
				return Text.of("Min");
			}
		};

		OptionEditor<ParticleHolder, String> max = new OptionEditor<>() {
			@Override
			public void set(ParticleHolder config, String value) {
				config.getSpawnFrequency().setMax(Integer.parseInt(value));
			}

			@Override
			public String get(ParticleHolder config) {
				return String.valueOf(config.getSpawnFrequency().getMax());
			}

			@Override
			public Text getName() {
				return Text.of("Max");
			}
		};

		ConfigOptionEditorWidget<ParticleHolder> editorWidget = new ConfigOptionEditorWidget<>(2, 2, 45, Text.of("Spawn Frequency"), min, max);
		panelWidget.addWidget(editorWidget);

		this.addDrawableChild(panelWidget);

		ResourcePackParticleConfigsManager.getRegisteredConfigs().forEach((holder, config) -> {
			ResourcePackEntryWidget widget = new ResourcePackEntryWidget(Text.of(config.id()), (button) -> {
				this.currentItem = holder.getItem().getItem().getDefaultStack();
				editorWidget.setConfig(holder);
			});
			this.listWidget.addEntry(widget);
		});
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
		BackgroundRenderer.drawTransparencyBackground(context, this.listArea.getX(), this.listArea.getY(), this.listArea.getWidth(), this.listArea.getHeight(), true);
		BackgroundRenderer.drawTransparencyBackground(context, this.listArea.getRight() + 1, this.listArea.getY(), 2 + 6 + 2, this.listArea.getHeight(), true);
		BackgroundRenderer.drawTransparencyBackground(context, this.showcaseArea.getX(), this.showcaseArea.getY(), this.showcaseArea.getWidth(), this.showcaseArea.getHeight(), true);
		super.render(context, mouseX, mouseY, deltaTicks);

		context.enableScissor(this.showcaseArea.getX() + 2, this.showcaseArea.getY() + 2, this.showcaseArea.getRight() - 2, this.showcaseArea.getBottom() - 2);

		boolean bl = this.showcaseArea.over(mouseX, mouseY);
		InventoryParticlesRenderer.getInstance().updateCursor(bl ? mouseY : this.showcaseArea.getCenterY(), bl ? mouseX : this.showcaseArea.getCenterX(), this.currentItem, null);
		InventoryCursor cursor = InventoryParticlesRenderer.getInstance().getCursor();
		context.drawItem(this.currentItem, cursor.getMouseX() - 8, cursor.getMouseY() - 8);

		context.disableScissor();
	}

	@Override
	public void tick() {
		InventoryParticlesRenderer.getInstance().tick(null, null, null);
	}
}
