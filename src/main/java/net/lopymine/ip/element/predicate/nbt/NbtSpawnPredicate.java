package net.lopymine.ip.element.predicate.nbt;

import java.util.*;
import java.util.stream.Stream;
import lombok.*;
import lombok.experimental.ExtensionMethod;
import net.lopymine.ip.client.InventoryParticlesClient;
import net.lopymine.ip.config.InventoryParticlesConfig;
import net.lopymine.ip.extension.ItemExtension;
import net.lopymine.ip.element.predicate.ISpawnPredicate;
import net.lopymine.ip.element.predicate.nbt.debug.DebugNbtPath;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.*;
import org.jetbrains.annotations.*;

@Getter
@Setter
@AllArgsConstructor
@ExtensionMethod(ItemExtension.class)
public class NbtSpawnPredicate implements ISpawnPredicate {

	private String particleName;
	private HashSet<NbtNode> nodes;
	private NbtNodeMatch match;

	@Override
	public boolean test(ItemStack stack) {
		if (this.nodes.isEmpty()) {
			return true;
		}
		
		try {
			Minecraft client = Minecraft.getInstance();
			LocalPlayer player = client.player;
			if (player == null) {
				return false;
			}

			//? if >=1.21 {
			Tag nbt = ItemStack.CODEC.encodeStart(player.registryAccess().createSerializationContext(NbtOps.INSTANCE), stack).getOrThrow();
			if (!(nbt instanceof CompoundTag root)) {
				this.debugLog(null, DebugLogReason.ENCODED_WRONG_ROOT, stack.getItem().getStringName());
				return false;
			}
			//?} else {
			/*CompoundTag root = stack.save(new CompoundTag());
			*///?}

			int success = 0;
			List<DebugNbtPath> successPaths = new ArrayList<>();

			for (NbtNode node : this.nodes) {
				Tag element = root.get(node.getName());
				if (element == null) {
					this.debugLog(null, DebugLogReason.NO_SUCH_ELEMENT_IN_ROOT, node.getName());
					continue;
				}

				DebugNbtPath debugNbtPath = DebugNbtPath.create(node);
				ReadResult readResult = this.readElementByType(element, node, debugNbtPath);
				if (readResult == ReadResult.SUCCESS) {
					if (this.match == NbtNodeMatch.ANY) {
						this.debugLog(debugNbtPath, DebugLogReason.MATCH);
						return true;
					} else {
						success++;
						successPaths.add(debugNbtPath);
					}
				}
			}

			boolean bl = switch (this.match) {
				case ANY -> false;
				case ALL -> success == this.nodes.size();
				case NONE -> success == 0;
			};
			if (bl) {
				for (DebugNbtPath debugNbtPath : successPaths) {
					this.debugLog(debugNbtPath, DebugLogReason.MATCH);
				}
			}
			return bl;
		} catch (Exception e) {
			InventoryParticlesClient.LOGGER.error("Failed to read nbt from item \"{}\" for NbtSpawnPredicate! Reason:", stack.getItem().getStringName(), e);
		}

		return true;
	}

	private ReadResult readElementByType(Tag element, NbtNode node, @NotNull DebugNbtPath debugNbtPath) {
		boolean debugLog = InventoryParticlesConfig.getInstance().getMainConfig().isDebugModeEnabled();

		List<String> checkValues = node.getCheckValue().orElse(new ArrayList<>());
		List<NbtNode> nodes = node.getNext().orElse(new ArrayList<>());

		if (checkValues.isEmpty() && nodes.isEmpty()) {
			boolean rightType = switch (node.getType()) {
				case OBJECT -> element instanceof CompoundTag;
				case LIST -> element instanceof CollectionTag /*? if <=1.21.4 {*//*<?>*//*?}*/;
				case STRING -> element instanceof StringTag;
				case INT -> element instanceof NumericTag;
			};
			if (rightType) {
				return ReadResult.SUCCESS;
			} else if (debugLog) {
				this.debugLog(debugNbtPath, DebugLogReason.TYPE_MISMATCH, node.getName(), element.getId(), node.getType().getId());
			}
			return ReadResult.FAILED;
		}

		boolean valueCheckedIfPresent = true; // true by default

		if (!checkValues.isEmpty()) {
			valueCheckedIfPresent = switch (node.getType()) {
				case STRING, INT -> {
					String value = null;
					if (element instanceof StringTag) {
						//? if <=1.21.4 {
						/*value = element.getAsString();
						 *///?} else {
						value = element.asString().orElse(null);
						//?}
					}
					if (element instanceof NumericTag number) {
						//? if <=1.21.4 {
						/*value = String.valueOf(number.getAsInt());
						 *///?} else {
						value = number.asInt().map(Object::toString).orElse(null);
						//?}
					}
					if (value == null) {
						this.debugLog(debugNbtPath, DebugLogReason.NO_VALUE, node.getName());
						yield false;
					}
					for (String findValue : checkValues) {
						if (findValue.equals(value)) {
							yield true;
						}
					}
					this.debugLog(debugNbtPath, DebugLogReason.WRONG_VALUE, node.getName(), value, new ArrayList<>(checkValues));
					yield false;
				}
				case OBJECT, LIST -> {
					if (node.getType() == NbtNodeType.LIST) {
						if (checkValues.size() == 1) {
							if (element instanceof CollectionTag /*? if <=1.21.4 {*//*<?>*//*?}*/ list) {
								List<String> values = List.of("EMPTY_LIST", "NOT_EMPTY_LIST");

								boolean empty = checkValues.get(0).equals(values.get(0));
								boolean notEmpty = checkValues.get(0).equals(values.get(1));
								if (empty && list.isEmpty()) {
									yield true;
								} else if (empty) {
									this.debugLog(debugNbtPath, DebugLogReason.CHECK_LIST_EMPTY, node.getName());
									yield false;
								}
								if (notEmpty && !list.isEmpty()) {
									yield true;
								} else if (notEmpty) {
									this.debugLog(debugNbtPath, DebugLogReason.CHECK_LIST_NOT_EMPTY, node.getName());
									yield false;
								}
								this.debugLog(debugNbtPath, DebugLogReason.CHECK_LIST_UNKNOWN, node.getName(), checkValues.get(0), values);
								yield false;
							}
							this.debugLog(debugNbtPath, DebugLogReason.CHECK_VALUE_IN_NOT_LIST_OBJECT, node.getName(), node.getType(), NbtNodeType.LIST);
							yield false;
						}
					}
					this.debugLog(debugNbtPath, DebugLogReason.NOT_STRING_LIKE, node.getName(), node.getType(), NbtNodeType.STRING_LIKE);
					yield true; // true by default
				}
			};
			if (nodes.isEmpty()) {
				return valueCheckedIfPresent ? ReadResult.SUCCESS : ReadResult.FAILED;
			}
		}

		Stream<ReadResult> stream = nodes.stream().map((nextNode) -> {
			debugNbtPath.next(nextNode);
			switch (node.getType()) {
				case OBJECT -> {
					if (element instanceof CompoundTag nbt) {
						Tag nextElement = nbt.get(nextNode.getName());
						if (nextElement == null) {
							//? if >=1.21.5 {
							Set<String> set = nbt.keySet();
							//?} else {
							/*Set<String> set = nbt.getAllKeys();
							*///?}
							this.debugLog(debugNbtPath, DebugLogReason.NODE_NOT_FOUND, nextNode.getName(), set);
							return ReadResult.FAILED;
						}
						return this.readElementByType(nextElement, nextNode, debugNbtPath);
					}
					this.debugLog(debugNbtPath, DebugLogReason.OBJECT_LIKE_TYPE_MISMATCH, node.getName(), node.getType(), NbtNodeType.OBJECT);
					return ReadResult.FAILED;
				}
				case LIST -> {
					if (element instanceof CollectionTag/*? if <=1.21.4 {*//*<?>*//*?}*/ list) {
						for (Tag nbtElement : list) {
							if (this.readElementByType(nbtElement, nextNode, debugNbtPath) == ReadResult.SUCCESS) {
								return ReadResult.SUCCESS;
							}
						}
						this.debugLog(debugNbtPath, DebugLogReason.NODE_NOT_FOUND_IN_LIST, nextNode.getName());
						return ReadResult.FAILED;
					}
					this.debugLog(debugNbtPath, DebugLogReason.OBJECT_LIKE_TYPE_MISMATCH, node.getName(), node.getType(), NbtNodeType.LIST);
					return ReadResult.FAILED;
				}
			}
			this.debugLog(debugNbtPath, DebugLogReason.NOT_OBJECT_LIKE, node.getName(), node.getType(), NbtNodeType.OBJECT_LIKE);
			return ReadResult.FAILED;
		});

		boolean bl = switch (node.getNextMatchType()) {
			case NONE -> stream.noneMatch((result) -> result == ReadResult.SUCCESS);
			case ALL -> stream.allMatch((result) -> result == ReadResult.SUCCESS);
			case ANY -> stream.anyMatch((result) -> result == ReadResult.SUCCESS);
		};

		return bl && valueCheckedIfPresent ? ReadResult.SUCCESS : ReadResult.FAILED;
	}

	private void debugLog(@Nullable DebugNbtPath debugNbtPath, DebugLogReason reason, Object... objects) {
		if (!InventoryParticlesConfig.getInstance().getMainConfig().isNbtDebugModeEnabled()) {
			return;
		}
		reason.debug(this.particleName, debugNbtPath, objects);
	}

	private enum DebugLogReason {

		MATCH("Successful NBT Predicate match."),

		ENCODED_WRONG_ROOT("Encoded invalid root NBT for item \"{}\"."),
		NO_SUCH_ELEMENT_IN_ROOT("Missing NBT element \"{}\" in root NBT."),
		TYPE_MISMATCH("NBT node \"{}\" has wrong type. Found type: \"{}\", Expected type: \"{}\"."),
		NO_VALUE("NBT node \"{}\" has no string-like value."),
		WRONG_VALUE("NBT node \"{}\" has unexpected value. Found: \"{}\", Expected one of: \"{}\"."),
		OBJECT_LIKE_TYPE_MISMATCH("Cannot get any next NBT node from object-like NBT node because of type mismatch. Node: \"{}\", Found type: \"{}\", Expected type: \"{}\"."),
		NOT_STRING_LIKE("NBT node \"{}\" is not string-like. Found type: \"{}\", Expected one of: \"{}\"."),
		NOT_OBJECT_LIKE("NBT node \"{}\" is not object-like. Found type: \"{}\", Expected one of: \"{}\"."),
		NODE_NOT_FOUND("Next NBT node \"{}\" not found. Available nodes: \"{}\"."),
		NODE_NOT_FOUND_IN_LIST("Next NBT node \"{}\" not found in the NBT list node."),
		CHECK_LIST_UNKNOWN("Unknown check mode for NBT list \"{}\". Found mode: \"{}\", Expected one of: \"{}\"."),
		CHECK_LIST_EMPTY("Required empty NBT list, but \"{}\" wasn't."),
		CHECK_LIST_NOT_EMPTY("Required NBT list with any element, but \"{}\" was empty."),
		CHECK_VALUE_IN_NOT_LIST_OBJECT("NBT node \"{}\" is not list-like. Found type: \"{}\", Expected one of: \"{}\".");

		private final String message;

		DebugLogReason(String message) {
			this.message = message;
		}

		public void debug(String particleName, @Nullable DebugNbtPath path, Object... objects) {
			String string = this.message + (path == null ? ("") : (" Path: %s".formatted(path.toString())));
			if (path != null) {
				path.back();
			}
			if (this == MATCH) {
				InventoryParticlesClient.LOGGER.info("[%s] %s".formatted(particleName, string), objects);
			} else {
				InventoryParticlesClient.LOGGER.error("[%s] %s".formatted(particleName, string), objects);
			}
		}
	}

	private enum ReadResult {
		FAILED,
		SUCCESS
	}
}
