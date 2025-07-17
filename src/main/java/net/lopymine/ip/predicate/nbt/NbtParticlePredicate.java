package net.lopymine.ip.predicate.nbt;

import java.util.*;
import java.util.stream.Stream;
import net.lopymine.ip.client.InventoryParticlesClient;
import net.lopymine.ip.predicate.IParticlePredicate;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;

public record NbtParticlePredicate(HashSet<NbtNode> nodes, NbtNodeMatch match) implements IParticlePredicate {

	@Override
	public boolean test(ItemStack stack) {
		if (this.nodes.isEmpty()) {
			return true;
		}
		try {
			MinecraftClient client = MinecraftClient.getInstance();
			if (client.player == null) {
				return false;
			}

			NbtElement nbt = stack.toNbt(client.player.getRegistryManager());
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
			InventoryParticlesClient.LOGGER.error("Failed to read nbt from item \"{}\" for NbtParticlePredicate! Reason:", stack.getItemName().getString(), e);
		}

		return true;
	}

	private ReadResult readElementByType(NbtElement element, NbtNode node) {
		List<String> findValues = node.getCheckValue().orElse(new ArrayList<>());
		List<NbtNode> nodes = node.getNext().orElse(new ArrayList<>());

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
				case STRING, INT -> {
					if (element instanceof NbtString || element instanceof NbtInt) {
						String string = element.asString();
						for (String findValue : findValues) {
							if (findValue.equals(string)) {
								return ReadResult.SUCCESS;
							}
						}
						return findValues.isEmpty() ? ReadResult.SUCCESS : ReadResult.FAILED;
					}
					return ReadResult.FAILED;
				}
			}
			return ReadResult.FAILED;
		});

		boolean bl = switch (node.getNextMatchType()) {
			case NONE -> stream.noneMatch((result) -> result != ReadResult.SUCCESS);
			case ALL -> stream.allMatch((result) -> result == ReadResult.SUCCESS);
			case ANY -> stream.anyMatch((result) -> result == ReadResult.SUCCESS);
		};

		return bl ? ReadResult.SUCCESS : ReadResult.FAILED;
	}

	private enum ReadResult {
		FAILED,
		SUCCESS
	}
}
