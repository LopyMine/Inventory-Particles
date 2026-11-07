package net.lopymine.ip.editor;

import java.nio.file.Path;
import java.util.*;
import lombok.Getter;
import net.lopymine.ip.InventoryParticles;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

public record EditorConfigEntry(
		Identifier id,
		String namespace,
		String path,
		Source source,
		String sourcePackId,
		List<String> itemIds,
		@Nullable Path file
) {

	public String getShortPath() {
		int index = this.path.lastIndexOf('/');
		return index == -1 ? this.path : this.path.substring(index + 1);
	}

	public String getFullPath() {
		return this.namespace.equals(InventoryParticles.MOD_ID) ? this.path : this.namespace + ":" + this.path;
	}

	public boolean matchesPath(String query) {
		return this.getFullPath().toLowerCase(Locale.ROOT).contains(query.toLowerCase(Locale.ROOT));
	}

	public boolean matchesItem(String query) {
		String lowered = query.toLowerCase(Locale.ROOT);
		for (String itemId : this.itemIds) {
			if (itemId.toLowerCase(Locale.ROOT).contains(lowered)) {
				return true;
			}
		}
		return false;
	}

	@Getter
	public enum Source {

		PACK("pack"),
		MOD("mod"),
		OTHER_PACK("other_pack");

		private final String name;

		Source(String name) {
			this.name = name;
		}

	}
}
