package net.lopymine.ip.editor.screen;

import java.io.IOException;
import java.util.Set;
import net.lopymine.ip.InventoryParticles;
import net.lopymine.ip.client.InventoryParticlesClient;
import net.lopymine.ip.editor.*;
import net.lopymine.ip.editor.widget.*;
import net.lopymine.mossylib.utils.ScreenUtils;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import org.jetbrains.annotations.Nullable;

public class ParticlesResourcePackEditorScreen extends EditorScreen {

	private static final int BUTTON_WIDTH = 220;
	private static final int BUTTON_HEIGHT = 20;
	private static final int BUTTON_OFFSET = 4;

	@Nullable
	private final Screen parent;

	public ParticlesResourcePackEditorScreen(@Nullable Screen parent) {
		super(InventoryParticles.text("editor.title"));
		this.parent = parent;
	}

	@Override
	protected void init() {
		int x = (this.width - BUTTON_WIDTH) / 2;
		int y = this.height / 2 - BUTTON_HEIGHT - BUTTON_OFFSET;

		this.addRenderableWidget(Button.builder(InventoryParticles.text("editor.create_new"), (button) -> {
			ScreenUtils.set(this.createPackNameScreen());
		}).bounds(x, y, BUTTON_WIDTH, BUTTON_HEIGHT).build());

		y += BUTTON_HEIGHT + BUTTON_OFFSET;
		this.addRenderableWidget(Button.builder(InventoryParticles.text("editor.edit_another"), (button) -> {
			ScreenUtils.set(new ParticlesPackSelectionScreen(this));
		}).bounds(x, y, BUTTON_WIDTH, BUTTON_HEIGHT).build());

		y += (BUTTON_HEIGHT + BUTTON_OFFSET) * 2;
		this.addRenderableWidget(Button.builder(CommonComponents.GUI_BACK, (button) -> {
			this.onClose();
		}).bounds(x, y, BUTTON_WIDTH, BUTTON_HEIGHT).build());
	}

	private NameInputScreen createPackNameScreen() {
		Set<String> usedPackNames = ParticlesPacksManager.getUsedPackNames();
		return new NameInputScreen(
				this,
				InventoryParticles.text("editor.creation.title"),
				InventoryParticles.text("editor.creation.name"),
				InventoryParticles.text("editor.creation.create"),
				"",
				(name) -> PackNamespaceUtils.isValidFileName(name) && !usedPackNames.contains(name),
				this::createPack,
				(name) -> name.isEmpty()
						?
						InventoryParticles.text("editor.creation.namespace_hint")
						:
						InventoryParticles.text("editor.creation.namespace", PackNamespaceUtils.toNamespace(name))
		);
	}

	private void createPack(String name) {
		try {
			PackEditSession session = PackEditSession.createNew(name);
			ScreenUtils.set(new PackParticlesScreen(this, session));
		} catch (IOException e) {
			InventoryParticlesClient.LOGGER.error("Failed to create resource pack \"{}\":", name, e);
		}
	}

	@Override
	protected void renderEditor(GuiGraphicsExtractor context, int mouseX, int mouseY, float deltaTicks) {
		this.renderScreen(context, mouseX, mouseY, deltaTicks);
		context.centeredText(this.font, this.title, this.width / 2, this.height / 4, -1);
	}

	@Override
	public void onClose() {
		ScreenUtils.set(this.parent);
	}
}
