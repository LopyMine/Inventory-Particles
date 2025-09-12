package net.lopymine.ip.predicate.nbt.debug;

import lombok.*;
import net.lopymine.ip.predicate.nbt.*;
import org.jetbrains.annotations.Nullable;

@Setter
@Getter
@AllArgsConstructor
public class DebugNbtPath {

	private String name;
	private NbtNodeType type;
	@Nullable
	private DebugNbtPath parent;

	private DebugNbtPath(String name, NbtNodeType type) {
		this.name = name;
		this.type = type;
	}

	public static DebugNbtPath create(NbtNode node) {
		return new DebugNbtPath(node.getName(), node.getType());
	}

	public void next(NbtNode next) {
		String name = this.getName();
		NbtNodeType type = this.getType();
		DebugNbtPath parent = this.getParent();

		DebugNbtPath path = new DebugNbtPath(name, type, parent);

		this.setName(next.getName());
		this.setType(next.getType());
		this.setParent(path);
	}

	public void back() {
		DebugNbtPath parent = this.getParent();
		if (parent == null) {
			return;
		}
		this.setName(parent.getName());
		this.setType(parent.getType());
		this.setParent(parent.getParent());
	}

	@Override
	public String toString() {
		String string = this.asString();
		if (this.parent != null) {
			String parentString = this.parent.toString();
			string = parentString + " —> " + string;
		}
		return string;
	}

	public String asString() {
		return "%s[%s]".formatted(this.name, this.type.asString());
	}
}
