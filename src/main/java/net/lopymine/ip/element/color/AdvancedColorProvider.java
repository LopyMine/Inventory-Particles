package net.lopymine.ip.element.color;

import com.mojang.serialization.Codec;
import java.util.*;
import lombok.*;
import net.lopymine.ip.element.color.advanced.IAdvancedColorProvider;
import net.lopymine.ip.debug.HideInDebugRender;
import net.minecraft.world.item.ItemStack;
import net.minecraft.util.RandomSource;
import org.jetbrains.annotations.Nullable;
import static com.mojang.serialization.Codec.INT;
import static com.mojang.serialization.codecs.RecordCodecBuilder.create;
import static net.lopymine.mossylib.utils.CodecUtils.option;

@Getter
@Setter
public class AdvancedColorProvider implements IColorProvider {

	@HideInDebugRender
	public static final Codec<AdvancedColorProvider> CODEC = create((instance) -> instance.group(
			option("mode", "random_static", Codec.STRING, (type) -> type.getMode().asString()),
			option("values", new ArrayList<>(), IColorProvider.CODEC.listOf(), AdvancedColorProvider::getValues),
			option("speed", 0, INT, AdvancedColorProvider::getSpeed)
	).apply(instance, AdvancedColorProvider::new));

	private IAdvancedColorProvider mode;
	private List<IColorProvider> values;
	@Nullable
	private Integer[] compiledValues;
	private int speed;

	public AdvancedColorProvider(IAdvancedColorProvider mode, List<IColorProvider> values, int speed) {
		this.mode   = mode;
		this.values = values;
		this.speed  = speed;
	}

	public AdvancedColorProvider(String mode, List<IColorProvider> values, int speed) {
		this.mode   = IAdvancedColorProvider.parse(mode, speed);
		this.values = values;
		this.speed  = speed;
	}

	@Override
	public IColorProvider copy() {
		return new AdvancedColorProvider(this.mode.copy(), this.values, this.speed);
	}

	@Override
	public int tick(RandomSource random) {
		if (this.compiledValues.length == 0) {
			return -1;
		}
		return this.mode.tickResolve(this.compiledValues, random);
	}

	@Override
	public void compile(ItemStack stack, RandomSource random) {
		List<Integer> compiledColors = new ArrayList<>();
		for (IColorProvider type : this.values) {
			type.compile(stack, random);
			if (type instanceof IListInventoryElementColorProvider listType) {
				compiledColors.addAll(Arrays.asList(listType.getList()));
				continue;
			}
			compiledColors.add(type.tick(random));
		}

		this.compiledValues = compiledColors.toArray(Integer[]::new);;
	}

	@Override
	public String asString() {
		return "advanced";
	}

	@Override
	public String toString() {
		return this.asString();
	}
}
