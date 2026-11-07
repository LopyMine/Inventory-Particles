package net.lopymine.ip.editor;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import lombok.Getter;
import net.lopymine.ip.InventoryParticles;
import net.lopymine.ip.config.particle.*;
import net.lopymine.ip.editor.field.*;
import net.lopymine.ip.resourcepack.manager.AbstractConfigsManager;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

@Getter
public class EditedConfig {

	private final PackEditSession session;
	private final ParticleConfig config;

	private String path;
	private BindingMode mode = BindingMode.LINK;

	@Nullable
	private EditorConfigEntry parent;
	@Nullable
	private ParticleConfig parentConfig;
	@Nullable
	private ParticleConfig originalConfig;

	private final Set<String> overriddenKeys = new HashSet<>();
	private final Set<ParticleHolder> parentHolders = Collections.newSetFromMap(new IdentityHashMap<>());

	private boolean dirty;

	private EditedConfig(PackEditSession session, ParticleConfig config, String path, @Nullable EditorConfigEntry parent, @Nullable ParticleConfig parentConfig) {
		this.session = session;
		this.config = config;
		this.path = path;
		this.parent = parent;
		this.parentConfig = parentConfig;
	}

	public static EditedConfig createNew(PackEditSession session, String path, ParticleConfig config) {
		EditedConfig edited = new EditedConfig(session, config, path, null, null);
		edited.mode = BindingMode.COPY;
		edited.dirty = true;
		return edited;
	}

	public static EditedConfig openOwn(PackEditSession session, EditorConfigEntry entry, ParticleConfig config, Set<String> keys, @Nullable EditorConfigEntry parent, @Nullable ParticleConfig parentConfig) {
		EditedConfig edited = new EditedConfig(session, config, entry.path(), parent, parentConfig);
		edited.mode = config.getCopyVisualFrom() != null && parent != null ? BindingMode.LINK : BindingMode.COPY;
		edited.overriddenKeys.addAll(keys);

		if (edited.mode == BindingMode.LINK && parentConfig != null) {
			config.copyVisualFrom(EditorConfigsIndex.deepCopy(parentConfig), keys);
		}

		edited.originalConfig = EditorConfigsIndex.deepCopy(config);
		return edited;
	}

	public static EditedConfig override(PackEditSession session, EditorConfigEntry parent, ParticleConfig parentConfig) {
		EditedConfig edited = new EditedConfig(session, EditorConfigsIndex.deepCopy(parentConfig), parent.path(), parent, parentConfig);
		edited.mode = BindingMode.OVERRIDE;
		edited.parentHolders.addAll(edited.config.getHolders());
		edited.originalConfig = EditorConfigsIndex.deepCopy(parentConfig);
		edited.dirty = true;
		return edited;
	}

//	public static EditedConfig fromParent(PackEditSession session, EditorConfigEntry parent, ParticleConfig parentConfig, String path) {
//		ParticleConfig config = EditorConfigsIndex.deepCopy(parentConfig);
//		config.setHolders(new HashSet<>());
//		config.setCopyVisualFrom(null);
//
//		EditedConfig edited = new EditedConfig(session, config, path, parent, parentConfig);
//		edited.mode = BindingMode.LINK;
//		edited.dirty = true;
//		return edited;
//	}

	public void markDirty() {
		this.dirty = true;
		this.session.markDirty();
	}

	public void copyFrom(EditorConfigEntry parent, ParticleConfig parentConfig, boolean withHolders) {
		this.parent = parent;
		this.parentConfig = parentConfig;

		this.config.copyVisualFrom(EditorConfigsIndex.deepCopy(parentConfig), Set.of());
		this.overriddenKeys.clear();
		this.parentHolders.clear();

		if (withHolders) {
			for (ParticleHolder holder : parentConfig.getHolders()) {
				this.config.getHolders().add(holder.copy());
			}
		}

		this.mode = BindingMode.LINK;
		this.markDirty();
	}

	public void setPath(String path) {
		this.path = path;
		this.markDirty();
	}

	public boolean hasParent() {
		return this.parent != null;
	}

	public void setMode(BindingMode mode) {
		if (this.parent == null || this.parentConfig == null || this.mode == mode) {
			return;
		}

		this.mode = mode;

		if (mode == BindingMode.OVERRIDE) {
			for (ParticleHolder holder : this.parentConfig.getHolders()) {
				ParticleHolder copy = holder.copy();
				this.parentHolders.add(copy);
				this.config.getHolders().add(copy);
			}
		} else {
			this.config.getHolders().removeAll(this.parentHolders);
			this.parentHolders.clear();
		}

		this.markDirty();
	}

	public boolean hasOriginal() {
		return this.originalConfig != null;
	}

	@Nullable
	public Object getOriginalValue(String path) {
		if (this.originalConfig == null) {
			return null;
		}
		return FieldValues.copy(FieldPath.resolve(this.originalConfig, path));
	}

	public boolean isInherited(String key) {
		if (this.mode != BindingMode.LINK || this.parent == null) {
			return false;
		}
		return ParticleConfig.VISUAL_KEYS.contains(key) && !this.overriddenKeys.contains(key);
	}

	public boolean isOverridden(String key) {
		return this.mode == BindingMode.LINK && this.parent != null && ParticleConfig.VISUAL_KEYS.contains(key) && this.overriddenKeys.contains(key);
	}

	public void overrideKey(String key) {
		this.overriddenKeys.add(key);
		this.markDirty();
	}

	public void resetKey(String key) {
		this.overriddenKeys.remove(key);
		if (this.parentConfig != null) {
			this.config.copyVisualFrom(EditorConfigsIndex.deepCopy(this.parentConfig), this.overriddenKeys);
		}
		this.markDirty();
	}

	public String getNamespace() {
		if (this.mode == BindingMode.OVERRIDE && this.parent != null) {
			return this.parent.namespace();
		}
		return this.session.getNamespace();
	}

	public String getTargetPath() {
		if (this.mode == BindingMode.OVERRIDE && this.parent != null) {
			return this.parent.path();
		}
		return this.path;
	}

	public String getFullPath() {
		String namespace = this.getNamespace();
		String path = this.getTargetPath();
		return namespace.equals(InventoryParticles.MOD_ID) ? path : "%s:%s".formatted(namespace, path);
	}

	public String getShortPath() {
		String path = this.getTargetPath();
		int index = path.lastIndexOf('/');
		return index == -1 ? path : path.substring(index + 1);
	}

	public Path getTargetFile() {
		return this.session.getWorkFolder()
				.resolve(ParticlesPackLayout.particlesFolder(this.getNamespace()))
				.resolve(this.getTargetPath() + AbstractConfigsManager.CONFIG_EXTENSION);
	}

	public void apply() throws IOException {
		if (this.mode == BindingMode.LINK && this.parent != null) {
			this.config.setCopyVisualFrom(getReference(this.parent));
		} else {
			this.config.setCopyVisualFrom(null);
		}

		Set<String> keys = null;
		if (this.mode == BindingMode.LINK) {
			keys = new HashSet<>(this.overriddenKeys);
			keys.add("copy_visual_from");
			keys.add("holders");
		}

		Path file = this.getTargetFile();
		Files.createDirectories(file.getParent());
		Files.writeString(file, ParticleConfigWriter.write(this.config, keys), StandardCharsets.UTF_8);

		this.dirty = false;
		this.session.markDirty();
	}

	private static Identifier getReference(EditorConfigEntry parent) {
		return InventoryParticles.id(parent.namespace(), parent.path());
	}
}
