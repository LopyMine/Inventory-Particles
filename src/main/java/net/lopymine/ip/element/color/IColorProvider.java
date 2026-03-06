package net.lopymine.ip.element.color;

import com.mojang.serialization.Codec;
import java.util.*;
import net.lopymine.ip.debug.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.util.RandomSource;
import org.jetbrains.annotations.NotNull;

public interface IColorProvider extends IDebugRenderable {

	@HideInDebugRender
	Map<String, ParticleColorTypeFactory> FACTORIES = Map.of(
			"nbt", (s) -> new NbtColorProvider(),
			"nbt_list", (s) -> new NbtListColorProvider()
	);

	@HideInDebugRender
	Codec<IColorProvider> CODEC = Codec.STRING.xmap(IColorProvider::parse, IColorProvider::asString);

	default void compile(ItemStack stack, RandomSource random) { }

	int tick(RandomSource random);

	String asString();

	default String getString(int color) {
		return this.asString() + "[" + color + "]";
	}

	IColorProvider copy();

	private static IColorProvider parse(String s) {
		if (s.startsWith("#")) {
			return new CustomColorProvider(s);
		}
		ParticleColorTypeFactory factory = FACTORIES.get(s);
		if (factory == null) {
			return new StandardColorProvider();
		}
		return factory.create(s);
	}

	interface ParticleColorTypeFactory {

		@NotNull IColorProvider create(String s);

	}

}
