package net.lopymine.ip.editor.field;

import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import java.lang.reflect.Field;
import java.util.*;
import java.util.function.*;
import net.lopymine.ip.InventoryParticles;
import net.lopymine.ip.client.InventoryParticlesClient;
import net.lopymine.ip.config.i2o.Integer2DynamicParticleSize;
import net.lopymine.ip.config.particle.*;
import net.lopymine.ip.config.range.*;
import net.lopymine.ip.editor.EditedConfig;
import net.lopymine.ip.editor.field.FieldValues.Variant;
import net.lopymine.ip.element.size.*;
import net.lopymine.ip.element.texture.ITexture;
import net.lopymine.mossylib.utils.CodecUtils;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

public class ConfigFieldsBuilder {

	private static final Map<String, String> ROOT_KEYS = Map.of(
			"lifeTimeTicks", "life_time",
			"animationType", "animation_type",
			"animationSpeed", "animation_speed",
			"size", "size",
			"textures", "textures",
			"holders", "holders",
			"physics", "physics"
	);

	private final FieldsPanelWidget panel;
	private final Runnable onChange;
	private final Runnable rebuild;
	@Nullable
	private final EditedConfig edited;
	@Nullable
	private final String lockedItemId;

	private final List<FieldRowWidget> rows = new ArrayList<>();
	private final Map<FieldRowWidget, Runnable> defaultResets = new IdentityHashMap<>();
	private final Map<FieldRowWidget, Runnable> originalResets = new IdentityHashMap<>();
	private final List<GroupRowWidget> objectGroups = new ArrayList<>();

	public ConfigFieldsBuilder(FieldsPanelWidget panel, Runnable onChange, Runnable rebuild, @Nullable EditedConfig edited, @Nullable String lockedItemId) {
		this.panel = panel;
		this.onChange = onChange;
		this.rebuild = rebuild;
		this.edited = edited;
		this.lockedItemId = lockedItemId;
	}

	public List<FieldRowWidget> build(ParticleConfig config) {
		this.rows.clear();
		this.defaultResets.clear();
		this.originalResets.clear();
		this.objectGroups.clear();

		this.walkObject(config, null, 0, "", new Scope(null, false));
		this.addGroupResets();

		return List.copyOf(this.rows);
	}

	private void walkObject(Object object, @Nullable GroupRowWidget group, int indent, String path, Scope scope) {
		for (Field field : FieldAccess.getFields(object.getClass())) {
			String name = field.getName();
			if (name.equals("copyVisualFrom")) {
				continue;
			}

			FieldAccess access = field.getType() == Optional.class ? FieldAccess.ofOptional(object, field) : FieldAccess.of(object, field);
			String fieldPath = path.isEmpty() ? name : "%s.%s".formatted(path, name);
			Scope fieldScope = indent == 0 ? new Scope(ROOT_KEYS.get(name), scope.locked()) : scope;

			this.addField(access, group, indent, fieldPath, fieldScope, getLabel(name));
		}
	}

	private void addField(FieldAccess access, @Nullable GroupRowWidget group, int indent, String path, Scope scope, Component label) {
		Class<?> type = access.type();
		Object value = access.get();

		if (FieldValues.isBoolean(type)) {
			this.addValueRow(this.createBooleanRow(access, group, indent, label), access, scope, path);
			return;
		}

		if (type.isEnum()) {
			this.addValueRow(this.createEnumRow(access, group, indent, label), access, scope, path);
			return;
		}

		if (type == IntegerRange.class || type == DoubleRange.class) {
			if (value == null) {
				return;
			}
			this.addValueRow(this.createRangeRow(value, group, indent, label), access, scope, path);
			return;
		}

		if (FieldValues.isText(type)) {
			this.addValueRow(new TextRowWidget(indent, group, label, access, this.onChange), access, scope, path);
			return;
		}

		if (Collection.class.isAssignableFrom(type)) {
			this.addCollection(access, group, indent, path, scope, label);
			return;
		}

		if (value == null) {
			return;
		}

		if (value instanceof DynamicSizesWithInterpolation size) {
			this.addSize(access, size, group, indent, path, scope, label);
			return;
		}

		if (FieldValues.isVariantOnly(value.getClass())) {
			FieldRowWidget row = this.createVariantRow(access, group, indent, label);
			if (row != null) {
				this.addRow(row, scope);
			}
			return;
		}

		if (!FieldValues.getVariants(value.getClass()).isEmpty()) {
			this.addVariant(access, group, indent, path, scope, label);
			return;
		}

		if (!hasCodec(value.getClass())) {
			return;
		}

		GroupRowWidget nested = this.createGroup(group, indent, label, path, scope);
		nested.addAction(this.createFieldAction(scope));
		this.addRow(nested, scope);
		this.objectGroups.add(nested);
		this.walkObject(value, nested, indent + 1, path, scope);
	}

	private void addCollection(FieldAccess access, @Nullable GroupRowWidget group, int indent, String path, Scope scope, Component label) {
		Object value = access.get();
		if (!(value instanceof Collection<?> collection)) {
			return;
		}

		Class<?> elementType = access.elementType();
		List<Object> elements = new ArrayList<>(collection);
		if (Set.class.isAssignableFrom(access.type())) {
			FieldPath.sort(elements);
		}

		Runnable sync = () -> {
			applyCollection(access, elements);
			this.onChange.run();
		};

		GroupRowWidget listGroup = this.createGroup(group, indent, label, path, scope);
		listGroup.addAction(this.createFieldAction(scope));
		if (elementType != null && !this.isDisabled(scope)) {
			listGroup.addPermanentAction(FieldAction.add(() -> {
				Object created = createInstance(elementType);
				if (created == null) {
					return;
				}
				elements.add(created);
				sync.run();
				this.panel.toggleGroup(path, true);
				this.rebuild.run();
			}));
		}
		this.addRow(listGroup, scope);

		for (int i = 0; i < elements.size(); i++) {
			Object element = elements.get(i);
			if (element == null) {
				continue;
			}

			int index = i;
			String elementPath = "%s[%s]".formatted(path, i);
			FieldAccess elementAccess = FieldAccess.ofElement(elements, i, element.getClass(), sync);
			Scope elementScope = scope.withLocked(scope.locked() || this.isLockedHolder(path, element));

			if (FieldValues.isText(element.getClass())) {
				this.addRow(new TextRowWidget(indent + 1, listGroup, Component.literal("[%s]".formatted(i)), elementAccess, this.onChange), elementScope);
				continue;
			}

			GroupRowWidget elementGroup = this.createGroup(listGroup, indent + 1, Component.literal("[%s]".formatted(i)), elementPath, elementScope);
			elementGroup.setIcon(createIcon(element));
			if (!this.isDisabled(elementScope)) {
				elementGroup.addAction(FieldAction.duplicate(() -> {
					Object copy = copyElement(element);
					if (copy == null) {
						return;
					}
					elements.add(index + 1, copy);
					sync.run();
					this.rebuild.run();
				}));

				elementGroup.addAction(FieldAction.delete(() -> {
					elements.remove(index);
					sync.run();
					this.rebuild.run();
				}));
			}
			this.addRow(elementGroup, elementScope);

			FieldRowWidget variantRow = this.createVariantRow(elementAccess, elementGroup, indent + 2, getLabel("variant"));
			if (variantRow != null) {
				this.addRow(variantRow, elementScope);
			}

			this.walkObject(element, elementGroup, indent + 2, elementPath, elementScope);
		}
	}

	@Nullable
	private static GroupRowWidget.RowIcon createIcon(Object element) {
		if (element instanceof ParticleHolder holder) {
			return GroupRowWidget.itemIcon(holder);
		}
		if (element instanceof ITexture texture) {
			return GroupRowWidget.textureIcon(texture);
		}
		return null;
	}

	private boolean isLockedHolder(String path, Object element) {
		String holdersPath = "holders";
		if (this.lockedItemId == null || !path.equals(holdersPath) || !(element instanceof ParticleHolder holder)) {
			return false;
		}
		return !holder.getItemOrTag().left()
				.map((cachedItem) -> cachedItem.getId().toString().equals(this.lockedItemId))
				.orElse(false);
	}

	private void addSize(FieldAccess access, DynamicSizesWithInterpolation size, @Nullable GroupRowWidget group, int indent, String path, Scope scope, Component label) {
		GroupRowWidget sizeGroup = this.createGroup(group, indent, label, path, scope);
		sizeGroup.addAction(this.createFieldAction(scope));
		this.addRow(sizeGroup, scope);
		this.objectGroups.add(sizeGroup);

		StaticSize staticSize = size.toStatic();
		List<CycleRowWidget.Option> options = List.of(
				new CycleRowWidget.Option(getLabel("static_size"), null, () -> this.setSizeMode(access, size, true)),
				new CycleRowWidget.Option(getLabel("dynamic_size"), null, () -> this.setSizeMode(access, size, false))
		);
		this.addRow(new CycleRowWidget(indent + 1, sizeGroup, getLabel("variant"), options, staticSize == null ? 1 : 0, this.onChange), scope);

		if (staticSize == null) {
			this.walkObject(size, sizeGroup, indent + 1, path, scope);
			return;
		}

		DynamicSize value = size.getSizes().get(0).getObject();
		for (Field field : FieldAccess.getFields(DynamicSize.class)) {
			if (field.getName().equals("interpolation")) {
				continue;
			}
			this.addField(FieldAccess.of(value, field), sizeGroup, indent + 1, "%s.sizes[0].object.%s".formatted(path, field.getName()), scope, getLabel(field.getName()));
		}
	}

	private void setSizeMode(FieldAccess access, DynamicSizesWithInterpolation size, boolean toStatic) {
		StaticSize staticSize = size.toStatic();
		if (toStatic == (staticSize != null)) {
			return;
		}

		if (toStatic) {
			DynamicSize first = size.getSizes().isEmpty() ? new DynamicSize() : size.getSizes().get(0).getObject();
			access.set(DynamicSizesWithInterpolation.fromStatic(new StaticSize(first.getWidth(), first.getHeight())));
		} else {
			DynamicSize first = new DynamicSize(staticSize.getWidth(), staticSize.getHeight());
			List<Integer2DynamicParticleSize> sizes = new ArrayList<>(List.of(new Integer2DynamicParticleSize(0, first)));
			access.set(new DynamicSizesWithInterpolation(sizes, DynamicSizeInterpolation.LINEAR_INTERPOLATION));
		}

		this.rebuild.run();
	}

	private void addVariant(FieldAccess access, @Nullable GroupRowWidget group, int indent, String path, Scope scope, Component label) {
		Object value = access.get();
		if (value == null) {
			return;
		}

		GroupRowWidget variantGroup = this.createGroup(group, indent, label, path, scope);
		variantGroup.addAction(this.createFieldAction(scope));
		this.addRow(variantGroup, scope);
		this.objectGroups.add(variantGroup);

		FieldRowWidget variantRow = this.createVariantRow(access, variantGroup, indent + 1, getLabel("variant"));
		if (variantRow != null) {
			this.addRow(variantRow, scope);
		}

		this.walkObject(value, variantGroup, indent + 1, path, scope);
	}

	@Nullable
	private FieldRowWidget createVariantRow(FieldAccess access, @Nullable GroupRowWidget group, int indent, Component label) {
		Object value = access.get();
		if (value == null) {
			return null;
		}

		List<Variant> variants = FieldValues.getVariants(value.getClass());
		if (variants.isEmpty()) {
			return null;
		}

		int index = 0;
		List<CycleRowWidget.Option> options = new ArrayList<>();
		for (int i = 0; i < variants.size(); i++) {
			Variant variant = variants.get(i);
			if (variant.type().isInstance(value)) {
				index = i;
			}
			options.add(new CycleRowWidget.Option(getLabel(variant.name()), null, () -> {
				if (variant.type().isInstance(access.get())) {
					return;
				}
				access.set(variant.factory().create());
				this.rebuild.run();
			}));
		}

		return new CycleRowWidget(indent, group, label, options, index, this.onChange);
	}

	private FieldRowWidget createBooleanRow(FieldAccess access, @Nullable GroupRowWidget group, int indent, Component label) {
		boolean current = Boolean.TRUE.equals(access.get());
		List<CycleRowWidget.Option> options = List.of(
				new CycleRowWidget.Option(InventoryParticles.text("editor.field.boolean.false"), null, () -> access.set(false)),
				new CycleRowWidget.Option(InventoryParticles.text("editor.field.boolean.true"), null, () -> access.set(true))
		);
		return new CycleRowWidget(indent, group, label, options, current ? 1 : 0, this.onChange);
	}

	private FieldRowWidget createEnumRow(FieldAccess access, @Nullable GroupRowWidget group, int indent, Component label) {
		Object[] constants = access.type().getEnumConstants();
		Object current = access.get();

		int index = 0;
		List<CycleRowWidget.Option> options = new ArrayList<>();
		for (int i = 0; i < constants.length; i++) {
			Object constant = constants[i];
			if (constant.equals(current)) {
				index = i;
			}
			options.add(new CycleRowWidget.Option(Component.literal(constant.toString().toLowerCase(Locale.ROOT)), null, () -> access.set(constant)));
		}

		return new CycleRowWidget(indent, group, label, options, index, this.onChange);
	}

	private FieldRowWidget createRangeRow(Object range, @Nullable GroupRowWidget group, int indent, Component label) {
		List<Field> fields = FieldAccess.getFields(range.getClass());
		FieldAccess min = FieldAccess.of(range, fields.get(0));
		FieldAccess max = FieldAccess.of(range, fields.get(1));
		return new RangeRowWidget(indent, group, label, min, max, this.onChange);
	}

	private GroupRowWidget createGroup(@Nullable GroupRowWidget group, int indent, Component label, String path, Scope scope) {
		boolean expanded = this.isDisabled(scope) ? this.panel.isExpanded(path) : !this.panel.isCollapsed(path);
		GroupRowWidget row = new GroupRowWidget(indent, group, label, path, expanded);
		row.setExpandAction(() -> {
			this.panel.toggleGroup(path, row.isExpanded());
			this.panel.layoutRows();
		});
		return row;
	}

	private void addRow(FieldRowWidget row, Scope scope) {
		row.setInherited(this.isInherited(scope.inheritedKey()));
		row.setLocked(scope.locked());
		this.rows.add(row);
	}

	private void addValueRow(FieldRowWidget row, FieldAccess access, Scope scope, String path) {
		row.addAction(this.createFieldAction(scope));
		this.addRow(row, scope);

		if (this.isDisabled(scope)) {
			return;
		}

		this.addReset(row, this.originalResets, this.createReset(access, () -> this.getOriginalValue(access, path)), FieldAction::resetOriginal);
		this.addReset(row, this.defaultResets, this.createReset(access, access::defaultValue), FieldAction::resetDefault);
	}

	@Nullable
	private Runnable createReset(FieldAccess access, Supplier<Object> value) {
		Object current = value.get();
		if (current == null) {
			return null;
		}

		return () -> {
			Object reset = value.get();
			if (reset == null) {
				return;
			}
			access.set(reset);
			this.onChange.run();
		};
	}

	private void addReset(FieldRowWidget row, Map<FieldRowWidget, Runnable> resets, @Nullable Runnable reset, Function<Runnable, FieldAction> action) {
		if (reset == null) {
			return;
		}

		resets.put(row, reset);
		row.addAction(action.apply(() -> {
			reset.run();
			this.rebuild.run();
		}));
	}

	@Nullable
	private Object getOriginalValue(FieldAccess access, String path) {
		if (this.edited == null) {
			return null;
		}

		Object original = this.edited.getOriginalValue(path);
		Object current = access.get();
		if (original == null || current != null && original.getClass() != current.getClass()) {
			return null;
		}

		return original;
	}

	private void addGroupResets() {
		for (GroupRowWidget group : this.objectGroups) {
			this.addGroupReset(group, this.originalResets, FieldAction::resetOriginal);
			this.addGroupReset(group, this.defaultResets, FieldAction::resetDefault);
		}
	}

	private void addGroupReset(GroupRowWidget group, Map<FieldRowWidget, Runnable> resets, Function<Runnable, FieldAction> action) {
		List<Runnable> groupResets = new ArrayList<>();
		for (FieldRowWidget row : this.rows) {
			Runnable reset = resets.get(row);
			if (reset != null && isDescendant(row, group)) {
				groupResets.add(reset);
			}
		}

		if (groupResets.isEmpty()) {
			return;
		}

		group.addAction(action.apply(() -> {
			groupResets.forEach(Runnable::run);
			this.rebuild.run();
		}));
	}

	private static boolean isDescendant(FieldRowWidget row, GroupRowWidget group) {
		for (GroupRowWidget current = row.getGroup(); current != null; current = current.getGroup()) {
			if (current == group) {
				return true;
			}
		}
		return false;
	}

	private boolean isDisabled(Scope scope) {
		return scope.locked() || this.isInherited(scope.inheritedKey());
	}

	private boolean isInherited(@Nullable String key) {
		return key != null && this.edited != null && this.edited.isInherited(key);
	}

	@Nullable
	private FieldAction createFieldAction(Scope scope) {
		String key = scope.inheritedKey();
		if (key == null || this.edited == null || scope.locked()) {
			return null;
		}

		if (this.isInherited(key)) {
			return FieldAction.override(() -> {
				this.edited.overrideKey(key);
				this.rebuild.run();
			});
		}

		if (this.edited.isOverridden(key)) {
			return FieldAction.reset(() -> {
				this.edited.resetKey(key);
				this.rebuild.run();
			});
		}

		return null;
	}

	private static void applyCollection(FieldAccess access, List<Object> elements) {
		Class<?> type = access.type();
		if (Set.class.isAssignableFrom(type)) {
			access.set(new LinkedHashSet<>(elements));
			return;
		}
		if (List.class.isAssignableFrom(type)) {
			access.set(new ArrayList<>(elements));
		}
	}

	@Nullable
	private static Object copyElement(Object element) {
		if (element instanceof ParticleHolder holder) {
			return holder.copy();
		}

		Object copy = FieldValues.copy(element);
		if (copy != null) {
			return copy;
		}

		copy = copyWithCodec(element);
		return copy == null ? createInstance(element.getClass()) : copy;
	}

	@Nullable
	@SuppressWarnings("unchecked")
	private static Object copyWithCodec(Object element) {
		try {
			Codec<Object> codec = (Codec<Object>) getCodec(element.getClass());
			if (codec == null) {
				return null;
			}
			return codec.decode(JsonOps.INSTANCE, codec.encodeStart(JsonOps.INSTANCE, element)/*? if >=1.20.5 {*/.getOrThrow()/*?} else {*//*.getOrThrow(false, InventoryParticlesClient.LOGGER::error)*//*?}*/)/*? if >=1.20.5 {*/.getOrThrow()/*?} else {*//*.getOrThrow(false, InventoryParticlesClient.LOGGER::error)*//*?}*/.getFirst();
		} catch (Exception ignored) {
			return null;
		}
	}

	@Nullable
	private static Codec<?> getCodec(Class<?> type) {
		for (Class<?> current = type; current != null && current != Object.class; current = current.getSuperclass()) {
			try {
				Field field = current.getDeclaredField("CODEC");
				field.setAccessible(true);
				return field.get(null) instanceof Codec<?> codec ? codec : null;
			} catch (ReflectiveOperationException ignored) {
				continue;
			}
		}
		return null;
	}

	@Nullable
	private static Object createInstance(Class<?> type) {
		if (type == String.class) {
			return "";
		}

		List<Variant> variants = FieldValues.getVariants(type);
		if (!variants.isEmpty()) {
			return variants.get(0).factory().create();
		}

		try {
			return type.getDeclaredConstructor().newInstance();
		} catch (Exception ignored) {
			return createFromCodec(type);
		}
	}

	@Nullable
	private static Object createFromCodec(Class<?> type) {
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

	private static boolean hasCodec(Class<?> type) {
		return getCodec(type) != null;
	}

	private static Component getLabel(String name) {
		return Component.translatableWithFallback("%s.editor.field.%s".formatted(InventoryParticles.MOD_ID, toSnakeCase(name)), prettify(name));
	}

	private static String toSnakeCase(String name) {
		StringBuilder builder = new StringBuilder();
		for (char c : name.toCharArray()) {
			if (Character.isUpperCase(c)) {
				builder.append('_').append(Character.toLowerCase(c));
				continue;
			}
			builder.append(c);
		}
		return builder.toString();
	}

	private static String prettify(String name) {
		String snake = toSnakeCase(name).replace('_', ' ');
		return Character.toUpperCase(snake.charAt(0)) + snake.substring(1);
	}

	private record Scope(@Nullable String inheritedKey, boolean locked) {

		private Scope withLocked(boolean locked) {
			return new Scope(this.inheritedKey, locked);
		}
	}
}
