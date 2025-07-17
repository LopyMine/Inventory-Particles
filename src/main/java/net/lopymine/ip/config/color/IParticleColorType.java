package net.lopymine.ip.config.color;

import com.mojang.serialization.Codec;
import java.util.*;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.NotNull;

public interface IParticleColorType {

	Map<String, ParticleColorTypeFactory> FACTORIES = Map.of(
			"nbt", (s) -> new NbtParticleColorType()
	);

	Codec<IParticleColorType> CODEC = Codec.STRING.xmap(IParticleColorType::parse, IParticleColorType::getAsString);

	int getColor(ItemStack stack, Random random);

	String getAsString();

	private static IParticleColorType parse(String s) {
		if (s.startsWith("#")) {
			return CustomParticleColorType.parse(s);
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
