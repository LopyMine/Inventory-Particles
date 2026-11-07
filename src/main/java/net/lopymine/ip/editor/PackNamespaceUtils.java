package net.lopymine.ip.editor;

import java.util.*;

public class PackNamespaceUtils {

	public static String toNamespace(String name) {
		StringBuilder builder = new StringBuilder();

		for (char c : name.toLowerCase(Locale.ROOT).toCharArray()) {
			if (c >= 'a' && c <= 'z' || c >= '0' && c <= '9' || c == '_' || c == '-' || c == '.') {
				builder.append(c);
				continue;
			}
			builder.append('_');
		}

		String namespace = builder.toString().replaceAll("_{2,}", "_").replaceAll("^[_.-]+|[_.-]+$", "");
		return namespace.isEmpty() ? "pack" : namespace;
	}

//	public static String toUniqueNamespace(String name, Collection<String> used) {
//		String namespace = toNamespace(name);
//		if (!used.contains(namespace)) {
//			return namespace;
//		}
//
//		int index = 2;
//		while (used.contains("%s_%s".formatted(namespace, index))) {
//			index++;
//		}
//		return "%s_%s".formatted(namespace, index);
//	}

	public static boolean isValidFileName(String name) {
		if (name.isBlank() || name.length() > 64) {
			return false;
		}
		for (char c : name.toCharArray()) {
			if (c == '/' || c == '\\' || c == ':' || c == '*' || c == '?' || c == '"' || c == '<' || c == '>' || c == '|' || c < ' ') {
				return false;
			}
		}
		return !name.equals(".") && !name.equals("..");
	}
}
