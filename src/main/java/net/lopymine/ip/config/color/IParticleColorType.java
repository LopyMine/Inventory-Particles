package net.lopymine.ip.config.color;

import com.mojang.serialization.Codec;
import java.util.*;
import net.lopymine.ip.debug.*;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.NotNull;

public interface IParticleColorType extends IDebugRenderable {

	@HideInDebugRender
	Map<String, ParticleColorTypeFactory> FACTORIES = Map.of(
			"nbt", (s) -> new NbtParticleColorType(),
			"nbt_list", (s) -> new NbtListParticleColorType()
	);

	@HideInDebugRender
	Codec<IParticleColorType> CODEC = Codec.STRING.xmap(IParticleColorType::parse, IParticleColorType::asString);

	default void compile(ItemStack stack, Random random) { }

	int tick(Random random);

	String asString();

	default String getString(int color) {
		return this.asString() + "[" + color + "]";
	}

	IParticleColorType copy();

	private static IParticleColorType parse(String s) {
		if (s.startsWith("#")) {
			return new CustomParticleColorType(s);
		}
		ParticleColorTypeFactory factory = FACTORIES.get(s);
		if (factory == null) {
			return new StandardParticleColorType();
		}
		return factory.create(s);
	}

	interface ParticleColorTypeFactory {

		@NotNull IParticleColorType create(String s);

	}

}
