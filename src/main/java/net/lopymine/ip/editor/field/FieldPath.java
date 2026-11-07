package net.lopymine.ip.editor.field;

import java.lang.reflect.Field;
import java.util.*;
import net.lopymine.ip.config.misc.CachedItem;
import net.lopymine.ip.config.particle.ParticleHolder;
import net.lopymine.ip.element.predicate.nbt.NbtNode;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

public class FieldPath {

	@Nullable
	public static Object resolve(Object root, String path) {
		Object current = root;

		for (String part : path.split("\\.")) {
			if (current == null) {
				return null;
			}

			int bracket = part.indexOf('[');
			current = readField(current, bracket == -1 ? part : part.substring(0, bracket));

			if (bracket != -1 && part.endsWith("]")) {
				current = readElement(current, part.substring(bracket + 1, part.length() - 1));
			}
		}

		return current;
	}

	public static void sort(List<Object> elements) {
		if (elements.size() < 2) {
			return;
		}
		if (elements.get(0) instanceof ParticleHolder) {
			elements.sort(Comparator.comparing((element) -> getHolderOrder((ParticleHolder) element)));
			return;
		}
		if (elements.get(0) instanceof NbtNode) {
			elements.sort(Comparator.comparing((element) -> getNodeOrder((NbtNode) element)));
		}
	}

	private static String getHolderOrder(ParticleHolder holder) {
		String item = holder.getItemOrTag().map(CachedItem::getId, Identifier::toString).toString();
		return "%s/%s".formatted(item, holder.getName());
	}

	private static String getNodeOrder(NbtNode node) {
		return "%s/%s".formatted(node.getName(), node.getType());
	}

	@Nullable
	private static Object readField(Object owner, String name) {
		for (Field field : FieldAccess.getFields(owner.getClass())) {
			if (!field.getName().equals(name)) {
				continue;
			}
			field.setAccessible(true);
			try {
				return field.get(owner);
			} catch (IllegalAccessException e) {
				return null;
			}
		}
		return null;
	}

	@Nullable
	private static Object readElement(@Nullable Object value, String index) {
		if (value instanceof Optional<?> optional) {
			value = optional.orElse(null);
		}
		if (!(value instanceof Collection<?> collection)) {
			return null;
		}

		List<Object> elements = new ArrayList<>(collection);
		sort(elements);

		try {
			int i = Integer.parseInt(index);
			return i >= 0 && i < elements.size() ? elements.get(i) : null;
		} catch (NumberFormatException e) {
			return null;
		}
	}
}
