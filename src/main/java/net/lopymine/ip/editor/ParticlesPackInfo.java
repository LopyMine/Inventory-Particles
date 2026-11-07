package net.lopymine.ip.editor;

import java.nio.file.Path;
import java.util.*;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

public record ParticlesPackInfo(
		String packId,
		String fileName,
		Component title,
		Path path,
		boolean archive,
		boolean enabled,
		@Nullable PackContent content
) {

	public ParticlesPackInfo withContent(PackContent content) {
		return new ParticlesPackInfo(this.packId, this.fileName, this.title, this.path, this.archive, this.enabled, content);
	}

	public boolean isScanned() {
		return this.content != null;
	}

	public int particleConfigs() {
		return this.content == null ? 0 : this.content.particleConfigs();
	}

	public int familyConfigs() {
		return this.content == null ? 0 : this.content.familyConfigs();
	}

	public boolean legacyLayout() {
		return this.content != null && this.content.legacyLayout();
	}

	public boolean hasConfigs() {
		return this.particleConfigs() + this.familyConfigs() > 0;
	}

	public String getMainNamespace() {
		Set<String> namespaces = this.content == null ? Set.of() : this.content.namespaces();
		return namespaces.stream().sorted().findFirst().orElseGet(() -> PackNamespaceUtils.toNamespace(this.fileName));
	}

	public record PackContent(Set<String> namespaces, int particleConfigs, int familyConfigs, boolean legacyLayout) { }
}
