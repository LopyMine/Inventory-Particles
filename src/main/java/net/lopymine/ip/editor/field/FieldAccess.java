package net.lopymine.ip.editor.field;

import java.lang.reflect.*;
import java.util.*;
import net.lopymine.ip.client.InventoryParticlesClient;
import org.jetbrains.annotations.Nullable;

public interface FieldAccess {

	String name();

	Class<?> type();

	@Nullable
	Object get();

	void set(@Nullable Object value);

	@Nullable
	default Class<?> elementType() {
		return null;
	}

	@Nullable
	default Object defaultValue() {
		return null;
	}

	static FieldAccess of(Object owner, Field field) {
		field.setAccessible(true);
		return new ReflectionAccess(owner, field);
	}

	static FieldAccess ofElement(List<Object> elements, int index, Class<?> type, Runnable onChange) {
		return new ElementAccess(elements, index, type, onChange);
	}

	static FieldAccess ofOptional(Object owner, Field field) {
		field.setAccessible(true);

		Type inner = field.getGenericType() instanceof ParameterizedType parameterized ? parameterized.getActualTypeArguments()[0] : null;
		Class<?> type = getRawType(inner);
		Class<?> elementType = inner instanceof ParameterizedType parameterized ? getRawType(parameterized.getActualTypeArguments()[0]) : null;

		return new OptionalAccess(new ReflectionAccess(owner, field), type == null ? Object.class : type, elementType);
	}

	@Nullable
	private static Class<?> getRawType(@Nullable Type type) {
		if (type instanceof Class<?> clazz) {
			return clazz;
		}
		return type instanceof ParameterizedType parameterized && parameterized.getRawType() instanceof Class<?> raw ? raw : null;
	}

	static List<Field> getFields(Class<?> type) {
		List<Field> fields = new ArrayList<>();
		for (Class<?> current = type; current != null && current != Object.class; current = current.getSuperclass()) {
			for (Field field : current.getDeclaredFields()) {
				if (Modifier.isStatic(field.getModifiers()) || Modifier.isTransient(field.getModifiers()) || field.isSynthetic()) {
					continue;
				}
				fields.add(field);
			}
		}
		return fields;
	}

	record ReflectionAccess(Object owner, Field field) implements FieldAccess {

		@Override
		public String name() {
			return this.field.getName();
		}

		@Override
		public Class<?> type() {
			return this.field.getType();
		}

		@Override
		@Nullable
		public Object get() {
			try {
				return this.field.get(this.owner);
			} catch (IllegalAccessException e) {
				InventoryParticlesClient.LOGGER.error("Failed to read config field \"{}\":", this.field.getName(), e);
				return null;
			}
		}

		@Override
		public void set(@Nullable Object value) {
			try {
				this.field.set(this.owner, value);
			} catch (IllegalAccessException e) {
				InventoryParticlesClient.LOGGER.error("Failed to write config field \"{}\":", this.field.getName(), e);
			}
		}

		@Override
		@Nullable
		public Object defaultValue() {
			return FieldDefaults.valueOf(this.owner, this.field);
		}

		@Override
		@Nullable
		public Class<?> elementType() {
			if (!(this.field.getGenericType() instanceof ParameterizedType parameterized)) {
				return null;
			}
			Type[] arguments = parameterized.getActualTypeArguments();
			if (arguments.length == 0) {
				return null;
			}
			return arguments[0] instanceof Class<?> type ? type : null;
		}
	}

	record OptionalAccess(FieldAccess delegate, Class<?> type, @Nullable Class<?> elementType) implements FieldAccess {

		@Override
		public String name() {
			return this.delegate.name();
		}

		@Override
		@Nullable
		public Object get() {
			Object value = this.delegate.get() instanceof Optional<?> optional ? optional.orElse(null) : null;
			if (value != null) {
				return value;
			}
			if (Set.class.isAssignableFrom(this.type)) {
				return new LinkedHashSet<>();
			}
			return List.class.isAssignableFrom(this.type) ? new ArrayList<>() : null;
		}

		@Override
		public void set(@Nullable Object value) {
			if (value instanceof Collection<?> collection && collection.isEmpty()) {
				this.delegate.set(Optional.empty());
				return;
			}
			this.delegate.set(Optional.ofNullable(value));
		}
	}

	record ElementAccess(List<Object> elements, int index, Class<?> type, Runnable onChange) implements FieldAccess {

		@Override
		public String name() {
			return "element";
		}

		@Override
		@Nullable
		public Object get() {
			return this.index < this.elements.size() ? this.elements.get(this.index) : null;
		}

		@Override
		public void set(@Nullable Object value) {
			if (this.index >= this.elements.size()) {
				return;
			}
			this.elements.set(this.index, value);
			this.onChange.run();
		}
	}
}
