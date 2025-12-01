package net.lopymine.ip.extension;

//? if <=1.20.1 {
/*import java.util.Optional;
import net.minecraft.nbt.*;
*///?}

public class OptionalExtension {

	//? if <=1.20.1 {
	/*public static <T extends NbtElement> Optional<T> to(Optional<NbtCompound> optional, String id, Class<T> clazz) {
		return to(to(optional, id), clazz);
	}

	public static Optional<NbtElement> to(Optional<NbtCompound> optional, String id) {
		return optional.map((c) -> c.get(id));
	}

	public static <T extends NbtElement> Optional<T> to(Optional<NbtElement> optional, Class<T> clazz) {
		return optional.filter(clazz::isInstance).map(clazz::cast);
	}

	public static <T extends AbstractNbtList<?>> Optional<T> toEmpty(Optional<T> optional, boolean empty) {
		return optional.filter((l) -> empty == l.isEmpty());
	}

	public static <T extends NbtElement> Optional<T> toFirst(Optional<NbtList> optional, Class<T> clazz) {
		return to(optional.map((l) -> l.get(0)), clazz);
	}

	public static Optional<NbtElement> toFirst(Optional<NbtList> optional) {
		return optional.map((l) -> l.get(0));
	}
	*///?}

}
