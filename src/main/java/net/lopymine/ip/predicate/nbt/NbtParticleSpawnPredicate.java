package net.lopymine.ip.predicate.nbt;

import java.util.*;
import java.util.stream.Stream;
import net.lopymine.ip.client.InventoryParticlesClient;
import net.lopymine.ip.predicate.IParticleSpawnPredicate;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;

public record NbtParticleSpawnPredicate(HashSet<NbtNode> nodes, NbtNodeMatch match) implements IParticleSpawnPredicate {

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
				return false;
			}

			int success = 0;

			for (NbtNode node : this.nodes) {
				NbtElement element = root.get(node.getName());
				if (element == null) {
					continue;
				}
				ReadResult readResult = this.readElementByType(element, node);
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

	private ReadResult readElementByType(NbtElement element, NbtNode node) {
		List<String> checkValues = node.getCheckValue().orElse(new ArrayList<>());
		List<NbtNode> nodes = node.getNext().orElse(new ArrayList<>());

		if (checkValues.isEmpty() && nodes.isEmpty()) {
			return switch (node.getType()) {
				case OBJECT -> element instanceof NbtCompound;
				case LIST -> element instanceof NbtList;
				case STRING -> element instanceof NbtString;
				case INT -> element instanceof NbtInt;
			} ? ReadResult.SUCCESS : ReadResult.FAILED;
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
						yield false;
					}
					for (String findValue : checkValues) {
						if (findValue.equals(value)) {
							yield true;
						}
					}
					yield false; // this means that element found, but with wrong value
				}
				case OBJECT, LIST -> true; // skip it
			};
			if (nodes.isEmpty()) {
				return valueCheckedIfPresent ? ReadResult.SUCCESS : ReadResult.FAILED;
			}
		}

		Stream<ReadResult> stream = nodes.stream().map((nextNode) -> {
			switch (node.getType()) {
				case OBJECT -> {
					if (element instanceof NbtCompound nbt) {
						NbtElement nextElement = nbt.get(nextNode.getName());
						if (nextElement == null) {
							return ReadResult.FAILED;
						}
						return this.readElementByType(nextElement, nextNode);
					}
					return ReadResult.FAILED;
				}
				case LIST -> {
					if (element instanceof NbtList nbt) {
						for (NbtElement nbtElement : nbt) {
							if (this.readElementByType(nbtElement, nextNode) == ReadResult.SUCCESS) {
								return ReadResult.SUCCESS;
							}
						}
					}
					return ReadResult.FAILED;
				}
			}
			return ReadResult.FAILED;
		});

		boolean bl = switch (node.getNextMatchType()) {
			case NONE -> stream.noneMatch((result) -> result == ReadResult.SUCCESS);
			case ALL -> stream.allMatch((result) -> result == ReadResult.SUCCESS);
			case ANY -> stream.anyMatch((result) -> result == ReadResult.SUCCESS);
		};

		return bl && valueCheckedIfPresent ? ReadResult.SUCCESS : ReadResult.FAILED;
	}

	private enum ReadResult {
		FAILED,
		SUCCESS
	}
}
