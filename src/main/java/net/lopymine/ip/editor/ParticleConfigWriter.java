package net.lopymine.ip.editor;

import com.google.gson.*;
import com.mojang.serialization.JsonOps;
import java.util.*;
import net.lopymine.ip.client.InventoryParticlesClient;
import net.lopymine.ip.config.particle.ParticleConfig;
import net.lopymine.ip.element.size.DynamicSizeInterpolation;
import org.jetbrains.annotations.Nullable;

public class ParticleConfigWriter {

	public static String write(ParticleConfig config, @Nullable Collection<String> keys) {
		JsonElement json = sanitize(ParticleConfig.CODEC.encodeStart(JsonOps.INSTANCE, config)/*? if >=1.20.5 {*/.getOrThrow()/*?} else {*//*.getOrThrow(false, InventoryParticlesClient.LOGGER::error)*//*?}*/);

		if (json instanceof JsonObject object) {
			sortHolders(object);
			if (keys != null) {
				json = ConfigJsonWriter.keepKeys(object, keys);
			}
		}

		return ConfigJsonWriter.write(json);
	}

	private static JsonElement sanitize(JsonElement element) {
		if (element instanceof JsonArray array) {
			JsonArray sanitized = new JsonArray();
			array.forEach((child) -> sanitized.add(sanitize(child)));
			return sanitized;
		}

		if (!(element instanceof JsonObject object)) {
			return element;
		}

		JsonObject sanitized = new JsonObject();
		for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
			if (isSkipped(entry.getKey(), entry.getValue())) {
				continue;
			}
			sanitized.add(entry.getKey(), sanitize(entry.getValue()));
		}

		return collapseColors(sanitized);
	}

	private static boolean isSkipped(String key, JsonElement value) {
		return isUnbounded(value) || isNoInterpolation(key, value) || isGeneratedName(key, value);
	}

	private static boolean isUnbounded(JsonElement element) {
		if (element instanceof JsonArray array) {
			if (array.size() == 0) {
				return false;
			}
			for (JsonElement child : array) {
				if (!isUnbounded(child)) {
					return false;
				}
			}
			return true;
		}
		return element instanceof JsonPrimitive primitive && primitive.isNumber() && Math.abs(primitive.getAsDouble()) == Double.MAX_VALUE;
	}

	private static boolean isNoInterpolation(String key, JsonElement element) {
		return key.equals("interpolation") && element instanceof JsonPrimitive primitive && primitive.isString() && primitive.getAsString().equals(DynamicSizeInterpolation.NO_INTERPOLATION.getId());
	}

	private static boolean isGeneratedName(String key, JsonElement element) {
		String generatedNamePrefix = "UnknownParticle@";
		return key.equals("name") && element instanceof JsonPrimitive primitive && primitive.isString() && primitive.getAsString().startsWith(generatedNamePrefix);
	}

	private static JsonElement collapseColors(JsonObject object) {
		if (object.size() != 3 || !(object.get("values") instanceof JsonArray values)) {
			return object;
		}
		if (!(object.get("mode") instanceof JsonPrimitive mode) || !"random_static".equals(mode.getAsString())) {
			return object;
		}
		if (!(object.get("speed") instanceof JsonPrimitive speed) || !speed.isNumber() || speed.getAsDouble() != 0.0D) {
			return object;
		}
		return values;
	}

	private static void sortHolders(JsonObject config) {
		if (!(config.get("holders") instanceof JsonArray holders)) {
			return;
		}

		List<JsonElement> sorted = new ArrayList<>(holders.asList());
		sorted.sort(Comparator.comparing(ParticleConfigWriter::getHolderOrder));

		JsonArray array = new JsonArray();
		sorted.forEach(array::add);
		config.add("holders", array);
	}

	private static String getHolderOrder(JsonElement holder) {
		if (!(holder instanceof JsonObject object)) {
			return holder.toString();
		}
		JsonElement item = object.get("item");
		JsonElement tag = object.get("tag");
		JsonElement name = object.get("name");
		return "%s/%s/%s".formatted(
				item == null ? "" : item.toString(),
				tag == null ? "" : tag.toString(),
				name == null ? "" : name.toString()
		);
	}
}
