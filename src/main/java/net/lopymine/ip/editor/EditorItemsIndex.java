package net.lopymine.ip.editor;

import java.util.*;
import net.lopymine.ip.InventoryParticles;
import net.lopymine.ip.utils.ItemStackUtils;
import net.minecraft.core.registries.*;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.*;
import org.jetbrains.annotations.Nullable;

public class EditorItemsIndex {

	private static final String VANILLA_NAMESPACE = "minecraft";

	private final List<Item> items;
	private final Map<Item, Identifier> itemIds = new IdentityHashMap<>();
	private final Map<Item, String> searchKeys = new IdentityHashMap<>();
	private final Set<Item> itemsWithParticles = Collections.newSetFromMap(new IdentityHashMap<>());

	@Nullable
	private List<String> namespaces;

	private EditorItemsIndex(List<Item> items) {
		this.items = items;
	}

	public static EditorItemsIndex create(PackEditSession session) {
		List<Item> items = new ArrayList<>();
		BuiltInRegistries.ITEM.forEach((item) -> {
			if (item != Items.AIR) {
				items.add(item);
			}
		});

		EditorItemsIndex index = new EditorItemsIndex(items);
		BuiltInRegistries.ITEM.entrySet().forEach((entry) -> index.itemIds.put(entry.getValue(), entry.getKey().identifier()));
		items.sort(Comparator.comparing((item) -> index.getId(item).toString()));

		index.collectItemsWithParticles(session);
		return index;
	}

	public List<String> getNamespaces() {
		if (this.namespaces == null) {
			Set<String> found = new TreeSet<>();
			for (Item item : this.items) {
				found.add(this.getId(item).getNamespace());
			}

			this.namespaces = new ArrayList<>();
			if (found.remove(VANILLA_NAMESPACE)) {
				this.namespaces.add(VANILLA_NAMESPACE);
			}
			this.namespaces.addAll(found);
		}
		return this.namespaces;
	}

	public List<Item> getItems() {
		return Collections.unmodifiableList(this.items);
	}

	public Identifier getId(Item item) {
		return this.itemIds.getOrDefault(item, BuiltInRegistries.ITEM.getKey(item));
	}

	public boolean hasParticles(Item item) {
		return this.itemsWithParticles.contains(item);
	}

	public List<Item> search(String query, boolean onlyWithParticles) {
		List<Item> result = new ArrayList<>();
		String lowered = query.toLowerCase(Locale.ROOT).trim();

		for (Item item : this.items) {
			if (onlyWithParticles && !this.hasParticles(item)) {
				continue;
			}
			if (!lowered.isEmpty() && !this.matches(item, lowered)) {
				continue;
			}
			result.add(item);
		}

		return result;
	}

	private boolean matches(Item item, String query) {
		return this.getSearchKey(item).contains(query);
	}

	private String getSearchKey(Item item) {
		return this.searchKeys.computeIfAbsent(item, (key) -> "%s\n%s".formatted(
				this.getId(key),
				item.getName(ItemStackUtils.create(key)).getString()
		).toLowerCase(Locale.ROOT));
	}

	private void collectItemsWithParticles(PackEditSession session) {
		Set<String> itemIds = new HashSet<>();
		Set<TagKey<Item>> tags = new HashSet<>();

		for (EditorConfigEntry entry : session.getOtherEntries()) {
			this.collectBindings(entry, itemIds, tags);
		}
		for (EditorConfigEntry entry : session.getPackEntries()) {
			this.collectBindings(entry, itemIds, tags);
		}

		for (Item item : this.items) {
			if (itemIds.contains(this.getId(item).toString())) {
				this.itemsWithParticles.add(item);
			}
		}

		if (tags.isEmpty()) {
			return;
		}

		//? if >=1.21.4 {
		BuiltInRegistries.ITEM.getTags().forEach((tag) -> {
			if (!tags.contains(tag.key())) {
				return;
			}
			tag.forEach((holder) -> this.itemsWithParticles.add(holder.value()));
		});
		//?} else {
		/*BuiltInRegistries.ITEM.getTags().forEach((pair) -> {
			if (!tags.contains(pair.getFirst())) {
				return;
			}
			pair.getSecond().forEach((holder) -> this.itemsWithParticles.add(holder.value()));
		});
		*///?}
	}

	private void collectBindings(EditorConfigEntry entry, Set<String> itemIds, Set<TagKey<Item>> tags) {
		for (String binding : entry.itemIds()) {
			if (binding.startsWith("#")) {
				tags.add(TagKey.create(Registries.ITEM, InventoryParticles.parseId(binding.substring(1))));
				continue;
			}
			itemIds.add(binding);
		}
	}
}
