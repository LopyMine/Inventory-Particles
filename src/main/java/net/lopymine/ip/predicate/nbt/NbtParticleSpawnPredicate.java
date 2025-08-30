package net.lopymine.ip.predicate.nbt;

import java.util.*;
import java.util.stream.Stream;
import net.lopymine.ip.client.InventoryParticlesClient;
import net.lopymine.ip.config.InventoryParticlesConfig;
import net.lopymine.ip.predicate.IParticleSpawnPredicate;
import net.lopymine.ip.predicate.nbt.debug.DebugNbtPath;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;
import org.jetbrains.annotations.*;

public class NbtParticleSpawnPredicate implements IParticleSpawnPredicate {

	private final HashSet<NbtNode> nodes;
	private final NbtNodeMatch match;

	public NbtParticleSpawnPredicate(HashSet<NbtNode> nodes, NbtNodeMatch match) {
		this.nodes = nodes;
		this.match = match;
	}

	@Override
	public boolean test(ItemStack stack) {
		if (this.nodes.isEmpty()) {
			return true;
		}
		
		try {
			MinecraftClient client = MinecraftClient.getInstance();
			ClientPlayerEntity player = client.player;
			if (player == null) {
				return false;
			}

			NbtElement nbt = ItemStack.CODEC.encodeStart(player.getRegistryManager().getOps(NbtOps.INSTANCE), stack).getOrThrow();
			if (!(nbt instanceof NbtCompound root)){
				this.debugLog(null, DebugLogReason.ENCODED_WRONG_ROOT, stack.getItem().getName());
				return false;
			}

			int success = 0;

			for (NbtNode node : this.nodes) {
				NbtElement element = root.get(node.getName());
				if (element == null) {
					this.debugLog(null, DebugLogReason.NO_SUCH_ELEMENT_IN_ROOT, node.getName());
					continue;
				}

				DebugNbtPath debugNbtPath = DebugNbtPath.create(node);
				ReadResult readResult = this.readElementByType(element, node, debugNbtPath);
				if (readResult == ReadResult.SUCCESS) {
					if (this.match == NbtNodeMatch.ANY) {
						return true;
					} else {
						success++;
					}
				}
			}

			return switch (this.match) {
				case ANY -> false;
				case ALL -> success == this.nodes.size();
				case NONE -> success == 0;
			};
		} catch (Exception e) {
			InventoryParticlesClient.LOGGER.error("Failed to read nbt from item \"{}\" for NbtParticleSpawnPredicate! Reason:", stack.getItemName().getString(), e);
		}

		return true;
	}

	private ReadResult readElementByType(NbtElement element, NbtNode node, @NotNull DebugNbtPath debugNbtPath) {
		boolean debugLog = InventoryParticlesConfig.getInstance().isDebugModeEnabled();

		List<String> checkValues = node.getCheckValue().orElse(new ArrayList<>());
		List<NbtNode> nodes = node.getNext().orElse(new ArrayList<>());

		if (checkValues.isEmpty() && nodes.isEmpty()) {
			boolean rightType = switch (node.getType()) {
				case OBJECT -> element instanceof NbtCompound;
				case LIST -> element instanceof NbtList;
				case STRING -> element instanceof NbtString;
				case INT -> element instanceof NbtInt;
			};
			if (rightType) {
				return ReadResult.SUCCESS;
			} else if (debugLog) {
				this.debugLog(debugNbtPath, DebugLogReason.TYPE_MISMATCH, node.getName(), element.getType(), node.getType().getId());
			}
			return ReadResult.FAILED;
		}

		boolean valueCheckedIfPresent = true; // true by default

		if (!checkValues.isEmpty()) {
			valueCheckedIfPresent = switch (node.getType()) {
				case STRING, INT -> {
					String value = null;
					if (element instanceof NbtString) {
						//? if <=1.21.7 {
						/*value = element.asString();
						 *///?} else {
						value = element.asString().orElse(null);
						//?}
					}
					if (element instanceof NbtInt) {
						//? if <=1.21.7 {
						/*String string = element.asString();
						 *///?} else {
						value = element.asInt().map(Object::toString).orElse(null);
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
					if (element instanceof NbtCompound nbt) {
						NbtElement nextElement = nbt.get(nextNode.getName());
						if (nextElement == null) {
							this.debugLog(debugNbtPath, DebugLogReason.NODE_NOT_FOUND, nextNode.getName(), nbt.getKeys());
							return ReadResult.FAILED;
						}
						return this.readElementByType(nextElement, nextNode, debugNbtPath);
					}
					this.debugLog(debugNbtPath, DebugLogReason.OBJECT_LIKE_TYPE_MISMATCH, node.getName(), node.getType(), NbtNodeType.OBJECT);
					return ReadResult.FAILED;
				}
				case LIST -> {
					if (element instanceof NbtList nbt) {
						for (NbtElement nbtElement : nbt) {
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
		if (!InventoryParticlesConfig.getInstance().isNbtDebugModeEnabled()) {
			return;
		}
		reason.debug(debugNbtPath, objects);
	}

	private enum DebugLogReason {

		ENCODED_WRONG_ROOT("Encoded invalid root NBT for item \"{}\"."),
		NO_SUCH_ELEMENT_IN_ROOT("Missing NBT element \"{}\" in root NBT."),
		TYPE_MISMATCH("NBT node \"{}\" has wrong type. Found type: \"{}\", Expected type: \"{}\"."),
		NO_VALUE("NBT node \"{}\" has no string-like value."),
		WRONG_VALUE("NBT node \"{}\" has unexpected value. Found: \"{}\", Expected one of: \"{}\"."),
		OBJECT_LIKE_TYPE_MISMATCH("Cannot get any next NBT node from object-like NBT node because of type mismatch. Node: \"{}\", Found type: \"{}\", Expected type: \"{}\"."),
		NOT_STRING_LIKE("NBT node \"{}\" is not string-like. Found type: \"{}\", Expected one of: \"{}\"."),
		NOT_OBJECT_LIKE("NBT node \"{}\" is not object-like. Found type: \"{}\", Expected one of: \"{}\"."),
		NODE_NOT_FOUND("Next NBT node \"{}\" not found. Available nodes: \"{}\"."),
		NODE_NOT_FOUND_IN_LIST("Next NBT node \"{}\" not found in the NBT list node.");

		private final String message;

		DebugLogReason(String message) {
			this.message = message;
		}

		public void debug(@Nullable DebugNbtPath path, Object... objects) {
			String string = this.message + (path == null ? ("") : (" Path: %s".formatted(path.toString())));
			if (path != null) {
				path.back();
			}
			InventoryParticlesClient.LOGGER.error(string, objects);
		}
	}

	private enum ReadResult {
		FAILED,
		SUCCESS
	}
}
