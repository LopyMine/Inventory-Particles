package net.lopymine.ip.editor.field;

public record FieldAction(String icon, String tooltipKey, Runnable action) {

	public static FieldAction override(Runnable action) {
		return new FieldAction("✎", "editor.field.action.override", action);
	}

	public static FieldAction reset(Runnable action) {
		return new FieldAction("⤓", "editor.field.action.reset", action);
	}

	public static FieldAction resetDefault(Runnable action) {
		return new FieldAction("↺", "editor.field.action.reset_default", action);
	}

	public static FieldAction resetOriginal(Runnable action) {
		return new FieldAction("⎌", "editor.field.action.reset_original", action);
	}

	public static FieldAction add(Runnable action) {
		return new FieldAction("+", "editor.field.action.add", action);
	}

	public static FieldAction duplicate(Runnable action) {
		return new FieldAction("⧉", "editor.field.action.duplicate", action);
	}

	public static FieldAction delete(Runnable action) {
		return new FieldAction("✕", "editor.field.action.delete", action);
	}
}
