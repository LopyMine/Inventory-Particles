package net.lopymine.ip.color.advanced;

import com.mojang.serialization.Codec;
import java.util.*;
import lombok.*;
import net.lopymine.ip.color.*;
import net.lopymine.ip.color.advanced.mode.IAdvancedParticleColorTypeMode;
import net.lopymine.ip.debug.HideInDebugRender;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.Nullable;
import static com.mojang.serialization.Codec.INT;
import static com.mojang.serialization.codecs.RecordCodecBuilder.create;
import net.lopymine.mossylib.utils.CodecUtils;
import static net.lopymine.mossylib.utils.CodecUtils.option;

@Getter
@Setter
public class AdvancedParticleColorType implements IParticleColorType {

	@HideInDebugRender
	public static final Codec<AdvancedParticleColorType> CODEC = create((instance) -> instance.group(
			option("mode", "random_static", Codec.STRING, (type) -> type.getMode().asString()),
			option("values", new ArrayList<>(), IParticleColorType.CODEC.listOf(), AdvancedParticleColorType::getValues),
			option("speed", 0, INT, AdvancedParticleColorType::getSpeed)
	).apply(instance, AdvancedParticleColorType::new));

	private IAdvancedParticleColorTypeMode mode;
	private List<IParticleColorType> values;
	@Nullable
	private Integer[] compiledValues;
	private int speed;

	public AdvancedParticleColorType(IAdvancedParticleColorTypeMode mode, List<IParticleColorType> values, int speed) {
		this.mode   = mode;
		this.values = values;
		this.speed  = speed;
	}

	public AdvancedParticleColorType(String mode, List<IParticleColorType> values, int speed) {
		this.mode   = IAdvancedParticleColorTypeMode.parse(mode, speed);
		this.values = values;
		this.speed  = speed;
	}

	@Override
	public IParticleColorType copy() {
		return new AdvancedParticleColorType(this.mode.copy(), this.values, this.speed);
	}

	@Override
	public int tick(Random random) {
		if (this.compiledValues.length == 0) {
			return -1;
		}
		return this.mode.tickResolve(this.compiledValues, random);
	}

	@Override
	public void compile(ItemStack stack, Random random) {
		List<Integer> compiledColors = new ArrayList<>();
		for (IParticleColorType type : this.values) {
			type.compile(stack, random);
			if (type instanceof IListParticleColorType listType) {
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
