package net.lopymine.ip.predicate.nbt;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.*;
import lombok.*;
import net.lopymine.ip.utils.CodecUtils;
import static net.lopymine.ip.utils.CodecUtils.option;

@Getter
@Setter
@AllArgsConstructor
@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class NbtNode {

	public static final Codec<NbtNode> CODEC = CodecUtils.recursive(
			"ip_nbt_node",
			(codec) -> {
				Codec<List<NbtNode>> nextCodec = Codec.either(codec, codec.listOf()).xmap((either) -> {
					Optional<List<NbtNode>> right = either.right();
					Optional<NbtNode> left = either.left();
					return right.orElseGet(() -> left.map(List::of).orElseGet(ArrayList::new));
				}, Either::right);

				Codec<List<String>> checkValueCodec = Codec.either(Codec.STRING, Codec.STRING.listOf()).xmap((either) -> {
					Optional<List<String>> right = either.right();
					Optional<String> left = either.left();
					return right.orElseGet(() -> left.map(List::of).orElseGet(ArrayList::new));
				}, Either::right);

				return RecordCodecBuilder.create((instance) -> instance.group(
						option("this_name", "", Codec.STRING, NbtNode::getName),
						option("this_type", NbtNodeType.OBJECT, NbtNodeType.CODEC, NbtNode::getType),
						option("check_value", checkValueCodec, NbtNode::getCheckValue),
						option("next_match", NbtNodeMatch.ANY, NbtNodeMatch.CODEC, NbtNode::getNextMatchType),
						option("next", nextCodec, NbtNode::getNext)
				).apply(instance, NbtNode::new));
			});

	private String name;
	private NbtNodeType type;
	private Optional<List<String>> checkValue;
	private NbtNodeMatch nextMatchType;
	private Optional<List<NbtNode>> next;

}
