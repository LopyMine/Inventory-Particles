package net.lopymine.ip.editor;

import com.mojang.datafixers.util.Either;
import java.util.*;
import net.lopymine.ip.InventoryParticles;
import net.lopymine.ip.config.misc.CachedItem;
import net.lopymine.ip.config.particle.*;
import net.lopymine.ip.config.range.IntegerRange;
import net.lopymine.ip.element.color.StandardColorProvider;
import net.lopymine.ip.element.mod.spawner.ParticleSpawnAreaId;
import net.lopymine.ip.element.predicate.nbt.NbtNodeMatch;
import net.lopymine.ip.element.size.*;
import net.lopymine.mossylib.utils.CodecUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.*;

public class EditorTemplates {

	public static ParticleConfig createTemplateConfig() {
		return createTemplateConfig(getTemplateItem());
	}

	public static ParticleConfig createTemplateConfig(Item item) {
		ParticleConfig config = EditorConfigsIndex.deepCopy(CodecUtils.parseNewInstanceHacky(ParticleConfig.CODEC));
		config.setLifeTimeTicks(20);
		config.setSize(DynamicSizesWithInterpolation.fromStatic(new StaticSize(8.0D, 8.0D)));
		config.setHolders(new HashSet<>(Set.of(createTemplateHolder(item))));
		return config;
	}

	public static ParticleHolder createTemplateHolder(Item item) {
		return new ParticleHolder(
				"CreatedParticle@" + new Random().nextInt(100000),
				Either.left(new CachedItem(item)),
				NbtNodeMatch.ANY,
				new HashSet<>(),
				ParticleSpawnAreaId.STANDARD_SPAWN_AREA_ID,
				new IntegerRange(1, 3),
				new IntegerRange(10, 20),
				new StandardColorProvider(),
				0.1D
		);
	}

	public static Item getTemplateItem() {
		Identifier templateItemId = InventoryParticles.id("minecraft", "diamond_sword");
		return BuiltInRegistries.ITEM.getOptional(templateItemId).orElse(Items.STONE);
	}
}
