package net.lopymine.ip.editor.field;

import com.mojang.serialization.Codec;
import java.lang.reflect.Field;
import java.util.*;
import net.lopymine.mossylib.utils.CodecUtils;
import org.jetbrains.annotations.Nullable;

public class FieldDefaults {

	private static final Map<Class<?>, Optional<Object>> INSTANCES = new HashMap<>();

	@Nullable
	public static Object valueOf(Object owner, Field field) {
		Object instance = getInstance(owner.getClass());
		if (instance == null) {
			return null;
		}

		try {
			field.setAccessible(true);
			return FieldValues.copy(field.get(instance));
		} catch (Exception ignored) {
			return null;
		}
	}

	@Nullable
	private static Object getInstance(Class<?> type) {
		return INSTANCES.computeIfAbsent(type, (key) -> Optional.ofNullable(create(key))).orElse(null);
	}

	@Nullable
	private static Object create(Class<?> type) {
		Codec<?> codec = getCodec(type);
		if (codec == null) {
			return null;
		}
		try {
			return CodecUtils.parseNewInstanceHacky(codec);
		} catch (Exception ignored) {
			return null;
		}
	}

	@Nullable
	private static Codec<?> getCodec(Class<?> type) {
		try {
			Field field = type.getDeclaredField("CODEC");
			field.setAccessible(true);
			return field.get(null) instanceof Codec<?> codec ? codec : null;
		} catch (Exception ignored) {
			return null;
		}
	}
}
