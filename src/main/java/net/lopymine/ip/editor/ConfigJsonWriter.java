package net.lopymine.ip.editor;

import com.google.gson.*;
import java.util.*;

public class ConfigJsonWriter {

	public static String write(JsonElement json) {
		StringBuilder builder = new StringBuilder();
		writeElement(builder, json, 0);
		return builder.append("\n").toString();
	}

	public static JsonObject keepKeys(JsonObject object, Collection<String> keys) {
		JsonObject filtered = new JsonObject();

		for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
			if (keys.contains(entry.getKey())) {
				filtered.add(entry.getKey(), entry.getValue());
			}
		}

		return filtered;
	}

	private static void writeElement(StringBuilder builder, JsonElement element, int depth) {
		if (element instanceof JsonObject object) {
			writeObject(builder, object, depth);
			return;
		}

		if (element instanceof JsonArray array) {
			writeArray(builder, array, depth);
			return;
		}

		builder.append(element);
	}

	private static void writeObject(StringBuilder builder, JsonObject object, int depth) {
		if (object.size() == 0) {
			builder.append("{}");
			return;
		}

		builder.append("{\n");

		Iterator<Map.Entry<String, JsonElement>> iterator = object.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<String, JsonElement> entry = iterator.next();
			indent(builder, depth + 1).append("\"").append(entry.getKey()).append("\": ");
			writeElement(builder, entry.getValue(), depth + 1);
			builder.append(iterator.hasNext() ? ",\n" : "\n");
		}

		indent(builder, depth).append("}");
	}

	private static void writeArray(StringBuilder builder, JsonArray array, int depth) {
		if (array.size() == 0) {
			builder.append("[]");
			return;
		}

		if (isInline(array)) {
			builder.append("[");
			for (int i = 0; i < array.size(); i++) {
				builder.append(i == 0 ? "" : ", ").append(array.get(i));
			}
			builder.append("]");
			return;
		}

		builder.append("[\n");
		for (int i = 0; i < array.size(); i++) {
			indent(builder, depth + 1);
			writeElement(builder, array.get(i), depth + 1);
			builder.append(i == array.size() - 1 ? "\n" : ",\n");
		}
		indent(builder, depth).append("]");
	}

	private static boolean isInline(JsonArray array) {
		for (JsonElement element : array) {
			if (!(element instanceof JsonPrimitive primitive) || !primitive.isNumber() && !primitive.isBoolean()) {
				return false;
			}
		}

		return true;
	}

	private static StringBuilder indent(StringBuilder builder, int depth) {
		String indent = "\t";
		return builder.append(indent.repeat(depth));
	}
}
