package net.lopymine.ip.editor.screen;

import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;
import net.lopymine.ip.InventoryParticles;
import net.lopymine.ip.client.InventoryParticlesClient;
import net.lopymine.ip.config.particle.ParticleConfig;
import net.lopymine.ip.editor.*;
import net.lopymine.ip.editor.widget.*;
import net.lopymine.ip.resourcepack.manager.ParticlesConfigsManager;
import net.lopymine.mossylib.utils.ScreenUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.*;
import net.minecraft.network.chat.*;
import org.jetbrains.annotations.Nullable;

public class PackParticlesScreen extends EditorScreen {

	private static final int BUTTON_WIDTH = 220;
	private static final int BUTTON_HEIGHT = 20;
	private static final int BUTTON_OFFSET = 4;

	@Nullable
	private final Screen parent;
	private final PackEditSession session;

	private Button saveButton;

	public PackParticlesScreen(@Nullable Screen parent, PackEditSession session) {
		super(InventoryParticles.text("editor.pack.title", session.getPackName()));
		this.parent = parent;
		this.session = session;
		this.session.createResources();
	}

	public static void open(@Nullable Screen parent, ParticlesPackInfo info) {
		if (!info.legacyLayout()) {
			openSession(parent, info, false);
			return;
		}

		ScreenUtils.set(new MossyConfirmScreen(
				(confirmed) -> {
					if (!confirmed) {
						ScreenUtils.set(parent);
						return;
					}
					openSession(parent, info, true);
				},
				InventoryParticles.text("editor.pack.migrate.title").withStyle(ChatFormatting.GOLD),
				InventoryParticles.text("editor.pack.migrate.message").withStyle(ChatFormatting.GOLD),
				InventoryParticles.text("editor.pack.migrate.confirm"),
				CommonComponents.GUI_CANCEL
		));
	}

	private static void openSession(@Nullable Screen parent, ParticlesPackInfo info, boolean migrate) {
		try {
			PackEditSession session = PackEditSession.open(info);
			if (migrate) {
				session.migrateLayout();
			}
			ScreenUtils.set(new PackParticlesScreen(parent, session));
		} catch (IOException e) {
			InventoryParticlesClient.LOGGER.error("Failed to open resource pack \"{}\":", info.fileName(), e);
			ScreenUtils.set(parent);
		}
	}

	@Override
	protected void init() {
		int x = (this.width - BUTTON_WIDTH) / 2;
		int y = this.height / 2 - (BUTTON_HEIGHT + BUTTON_OFFSET) * 3;

		y = this.addAction(x, y, "editor.pack.create_config", this::createConfig);
		y = this.addAction(x, y, "editor.pack.edit_config", this::editConfig);
		y = this.addAction(x, y, "editor.pack.create_item_particles", this::createItemParticles);
		y = this.addAction(x, y, "editor.pack.edit_item_particles", this::editItemParticles);

		y += BUTTON_OFFSET * 2;
		this.saveButton = Button.builder(InventoryParticles.text("editor.pack.save"), (button) -> this.save(false))
				.bounds(x, y, BUTTON_WIDTH, BUTTON_HEIGHT)
				.build();
		this.addRenderableWidget(this.saveButton);

		y += BUTTON_HEIGHT + BUTTON_OFFSET;
		y = this.addAction(x, y, "editor.pack.save_and_apply", () -> this.save(true));

		this.addRenderableWidget(Button.builder(CommonComponents.GUI_BACK, (button) -> this.onClose())
				.bounds(x, y + BUTTON_OFFSET, BUTTON_WIDTH, BUTTON_HEIGHT)
				.build());
	}

	private int addAction(int x, int y, String key, Runnable action) {
		this.addRenderableWidget(Button.builder(InventoryParticles.text(key), (button) -> action.run())
				.bounds(x, y, BUTTON_WIDTH, BUTTON_HEIGHT)
				.build());
		return y + BUTTON_HEIGHT + BUTTON_OFFSET;
	}

	private void createConfig() {
		this.askConfigName("new_particle", (path) -> {
			ParticleConfig config = EditorTemplates.createTemplateConfig();
			this.openEditor(EditedConfig.createNew(this.session, path, config), null);
		});
	}

	private void editConfig() {
		ScreenUtils.set(new ConfigSelectionScreen(
				this,
				InventoryParticles.text("editor.pack.edit_config"),
				this.session,
				null,
				(entry) -> this.openConfig(entry, null)
		));
	}

	private void createItemParticles() {
		EditorItemsIndex index = this.session.getItemsIndex();
		ScreenUtils.set(new ItemSelectionScreen(
				this,
				InventoryParticles.text("editor.pack.create_item_particles"),
				index,
				false,
				true,
				(item) -> this.askConfigName(index.getId(item).getPath(), (path) -> {
					ParticleConfig config = EditorTemplates.createTemplateConfig(item);
					this.openEditor(EditedConfig.createNew(this.session, path, config), null);
				})
		));
	}

	private void editItemParticles() {
		EditorItemsIndex index = this.session.getItemsIndex();
		ScreenUtils.set(new ItemSelectionScreen(
				this,
				InventoryParticles.text("editor.pack.edit_item_particles"),
				index,
				true,
				false,
				(item) -> {
					String itemId = index.getId(item).toString();
					ScreenUtils.set(new ConfigSelectionScreen(
							this,
							InventoryParticles.text("editor.pack.edit_item_particles"),
							this.session,
							itemId,
							(entry) -> this.openConfig(entry, itemId)
					));
				}
		));
	}

	private void askConfigName(String initialPath, Consumer<String> onConfirm) {
		Set<String> paths = new HashSet<>();
		for (EditorConfigEntry entry : this.session.getPackEntries()) {
			paths.add(entry.path());
		}

		ScreenUtils.set(new NameInputScreen(
				this,
				InventoryParticles.text("editor.config.create.title"),
				InventoryParticles.text("editor.config.name"),
				InventoryParticles.text("editor.config.create.confirm"),
				getUniquePath(initialPath, paths),
				(path) -> !path.isBlank() && !paths.contains(path),
				onConfirm,
				(path) -> InventoryParticles.text("editor.config.name.subfolder_hint")
		));
	}

	private static String getUniquePath(String path, Set<String> paths) {
		if (!paths.contains(path)) {
			return path;
		}
		int number = 1;
		while (paths.contains("%s_%s".formatted(path, number))) {
			number++;
		}
		return "%s_%s".formatted(path, number);
	}

	private void openConfig(EditorConfigEntry entry, @Nullable String focusItemId) {
		EditorConfigsIndex.LoadedConfig loaded = EditorConfigsIndex.load(entry);
		if (loaded == null) {
			return;
		}

		if (entry.source() == EditorConfigEntry.Source.PACK) {
			EditorConfigEntry parent = null;
			ParticleConfig parentConfig = null;

			if (loaded.config().getCopyVisualFrom() != null) {
				parent = EditorConfigsIndex.findById(this.session, ParticlesConfigsManager.getParticleConfigId(loaded.config().getCopyVisualFrom()));
				EditorConfigsIndex.LoadedConfig loadedParent = parent == null ? null : EditorConfigsIndex.load(parent);
				parentConfig = loadedParent == null ? null : loadedParent.config();
			}

			this.openEditor(EditedConfig.openOwn(this.session, entry, loaded.config(), loaded.keys(), parent, parentConfig), focusItemId);
			return;
		}

		this.openEditor(EditedConfig.override(this.session, entry, loaded.config()), focusItemId);
	}

	private void openEditor(EditedConfig edited, @Nullable String focusItemId) {
		ScreenUtils.set(new ConfigEditorScreen(this, edited, focusItemId));
	}

	private void save(boolean apply) {
		try {
			if (apply) {
				this.session.saveAndApply();
			} else {
				this.session.save();
			}
			this.saveButton.setMessage(InventoryParticles.text("editor.pack.saved"));
		} catch (IOException e) {
			InventoryParticlesClient.LOGGER.error("Failed to save resource pack \"{}\":", this.session.getPackName(), e);
		}
	}

	@Override
	protected void renderEditor(GuiGraphicsExtractor context, int mouseX, int mouseY, float deltaTicks) {
		this.renderScreen(context, mouseX, mouseY, deltaTicks);

		context.centeredText(this.font, this.title, this.width / 2, 14, -1);
		context.centeredText(this.font, InventoryParticles.text("editor.pack.namespace", this.session.getNamespace()), this.width / 2, 30, -6250336);
	}

	@Override
	public void onClose() {
		if (!this.session.isDirty()) {
			this.close();
			return;
		}

		ScreenUtils.set(new MossyConfirmScreen(
				(confirmed) -> {
					if (confirmed) {
						this.close();
						return;
					}
					ScreenUtils.set(this);
				},
				InventoryParticles.text("editor.pack.discard.title"),
				InventoryParticles.text("editor.pack.discard.message"),
				InventoryParticles.text("editor.pack.discard.confirm"),
				CommonComponents.GUI_CANCEL
		));
	}

	private void close() {
		this.session.discard();
		ScreenUtils.set(this.parent);
	}
}
