package net.lopymine.ip.family;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import java.util.*;
import lombok.*;
import net.minecraft.resources.Identifier;
import static com.mojang.serialization.codecs.RecordCodecBuilder.create;
import static net.lopymine.mossylib.utils.CodecUtils.option;

@Getter
@Setter
@AllArgsConstructor
public class FamilyParticleConfig {

	public static final Codec<ArrayList<String>> COMPATIBLE_CODEC = Codec.either(Codec.BOOL, Codec.STRING.listOf()).xmap((either) -> {
		var e = either.mapLeft((bl) -> new ArrayList<>(List.of(bl ? "#all" : "#none")));
		return new ArrayList<>(e.right().orElseGet(() -> e.left().orElseThrow()));
	}, Either::right);

	public static final Codec<WhitelistAndBlacklist> LIST_CODEC = Codec.either(Codec.STRING.listOf(), WhitelistAndBlacklist.CODEC).xmap((either) -> {
		return either.left().map((list) -> new WhitelistAndBlacklist(new ArrayList<>(), new ArrayList<>(list)))
				.orElseGet(() -> either.right().orElseThrow());
	}, (wab) -> Either.left(wab.getWhitelist()));

	public static final Codec<FamilyParticleConfig> CODEC = create((instance) -> instance.group(
			option("keywords", new WhitelistAndBlacklist(), LIST_CODEC, FamilyParticleConfig::getKeywords),
			option("tags", new WhitelistAndBlacklist(), LIST_CODEC, FamilyParticleConfig::getTags),
			option("namespaces", new WhitelistAndBlacklist(), LIST_CODEC, FamilyParticleConfig::getNamespaces),
			option("particles", new ArrayList<>(), FamilyParticleData.CODEC, FamilyParticleConfig::getParticles),
			option("family_groups", new ArrayList<>(), Codec.STRING, FamilyParticleConfig::getFamilyGroups),
			option("compatible", new ArrayList<>(), COMPATIBLE_CODEC, FamilyParticleConfig::getCompatibleGroups),
			option("priority", 1000, Codec.INT, FamilyParticleConfig::getPriority)
	).apply(instance, FamilyParticleConfig::new));

	private Identifier location;

	private WhitelistAndBlacklist keywords;
	private WhitelistAndBlacklist tags;
	private WhitelistAndBlacklist namespaces;
	private ArrayList<FamilyParticleData> particles;
	private ArrayList<String> familyGroups;
	private ArrayList<String> compatibleGroups;
	private int priority;

	public FamilyParticleConfig(
			WhitelistAndBlacklist keywords,
			WhitelistAndBlacklist tags,
			WhitelistAndBlacklist namespaces,
			ArrayList<FamilyParticleData> particles,
			ArrayList<String> familyGroups,
			ArrayList<String> compatibleGroups,
			int priority
	) {
		this.keywords         = keywords;
		this.tags             = tags;
		this.namespaces       = namespaces;
		this.particles        = particles;
		this.familyGroups     = familyGroups;
		this.compatibleGroups = compatibleGroups;
		this.priority         = priority;
	}

	@AllArgsConstructor
	@Getter
	@Setter
	public static class WhitelistAndBlacklist {

		public static final Codec<WhitelistAndBlacklist> CODEC = create((instance) -> instance.group(
				option("blacklist", new ArrayList<>(), Codec.STRING, WhitelistAndBlacklist::getBlacklist),
				option("whitelist", new ArrayList<>(), Codec.STRING, WhitelistAndBlacklist::getWhitelist)
		).apply(instance, WhitelistAndBlacklist::new));

		private ArrayList<String> blacklist;
		private ArrayList<String> whitelist;

		public WhitelistAndBlacklist() {
			this.blacklist = new ArrayList<>();
			this.whitelist = new ArrayList<>();
		}
	}
}
