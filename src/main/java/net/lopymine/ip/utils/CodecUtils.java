package net.lopymine.ip.utils;

import com.google.gson.*;

import com.mojang.serialization.*;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.lopymine.ip.InventoryParticles;
import net.lopymine.ip.client.InventoryParticlesClient;

import java.util.*;
import java.util.function.*;

@SuppressWarnings("unused")
public final class CodecUtils {

	public static <A> Codec<A> recursive(String name, Function<Codec<A>, Codec<A>> wrapped) {
		//? if >=1.21 {
		return Codec.recursive(name, wrapped);
		//?} else {
		/*return new RecursiveCodec<>(name, wrapped);
		 *///?}
	}


	public static <A> A parseNewInstanceHacky(Codec<A> codec) {
		try {
			return codec.decode(JsonOps.INSTANCE, /*? <=1.17.1 {*//*new JsonParser().parse("{}")*//*?} else {*/JsonParser.parseString("{}")/*?}*/)/*? if >=1.20.5 {*/.getOrThrow()/*?} else {*//*.getOrThrow(false, InventoryParticlesClient.LOGGER::error)*//*?}*/.getFirst();
		} catch (Exception e) {
			throw new IllegalArgumentException("Failed to create new instance of config in the %s mod".formatted(InventoryParticles.MOD_NAME), e);
		}
	}

	public static <A, B> RecordCodecBuilder<A, B> option(String optionId, Supplier<B> defValue, Codec<B> codec, Function<A, B> getter) {
		return codec.optionalFieldOf(optionId).xmap(o -> o.orElse(defValue.get()), Optional::ofNullable).forGetter(getter);
	}

	public static <A, B> RecordCodecBuilder<A, B> option(String optionId, Codec<B> codec, Function<A, B> getter, Object... ignored) {
		return codec.fieldOf(optionId).forGetter(getter);
	}

	public static <A, B> RecordCodecBuilder<A, B> option(String optionId, B defValue, Codec<B> codec, Function<A, B> getter) {
		return codec.optionalFieldOf(optionId).xmap((o) -> o.orElse(defValue), Optional::ofNullable).forGetter(getter);
	}

	public static <A, B> RecordCodecBuilder<A, Optional<B>> option(String optionId, Codec<B> codec, Function<A, Optional<B>> getter) {
		return codec.optionalFieldOf(optionId).forGetter(getter);
	}

	public static <A, B> RecordCodecBuilder<A, HashSet<B>> option(String optionId, HashSet<B> defValue, Codec<B> codec, Function<A, HashSet<B>> getter) {
		return codec.listOf().xmap(HashSet::new, ArrayList::new).optionalFieldOf(optionId).xmap((o) -> o.orElse(defValue), Optional::ofNullable).forGetter(getter);
	}

	public static <A, B> RecordCodecBuilder<A, ArrayList<B>> option(String optionId, ArrayList<B> defValue, Codec<B> codec, Function<A, ArrayList<B>> getter) {
		return codec.listOf().xmap(ArrayList::new, ArrayList::new).optionalFieldOf(optionId).xmap((o) -> o.orElse(defValue), Optional::ofNullable).forGetter(getter);
	}

	public static <T, A, B> RecordCodecBuilder<T, HashMap<A, B>> option(String optionId, HashMap<A, B> defValue, Codec<A> codecA, Codec<B> codecB, Function<T, HashMap<A, B>> getter) {
		return Codec.unboundedMap(codecA, codecB).xmap(HashMap::new, HashMap::new).optionalFieldOf(optionId).xmap((o) -> o.orElse(defValue), Optional::ofNullable).forGetter(getter);
	}

	public static <T> void decode(Codec<T> codec, JsonElement o, Consumer<T> consumer) {
		try {
			T value = codec.decode(JsonOps.INSTANCE, o)/*? if >=1.20.5 {*/.getOrThrow()/*?} else {*//*.getOrThrow(false, InventoryParticlesClient.LOGGER::error)*//*?}*/.getFirst();
			consumer.accept(value);
		} catch (Exception e) {
			InventoryParticlesClient.LOGGER.warn("Failed to decode JsonElement:", e);
		}
	}

	public static <T> T decode(String id, Codec<T> codec, JsonObject o) {
		if (o.has(id)) {
			try {
				return codec.decode(JsonOps.INSTANCE, o.get(id))/*? if >=1.20.5 {*/.getOrThrow()/*?} else {*//*.getOrThrow(false, InventoryParticlesClient.LOGGER::error)*//*?}*/.getFirst();
			} catch (Exception e) {
				InventoryParticlesClient.LOGGER.warn("Failed to decode \"%s\" from JsonObject:".formatted(id), e);
			}
		}
		return null;
	}

	public static <T> T decode(String id, T fallback, Codec<T> codec, JsonObject o) {
		if (o.has(id)) {
			try {
				return codec.decode(JsonOps.INSTANCE, o.get(id))/*? if >=1.20.5 {*/.getOrThrow()/*?} else {*//*.getOrThrow(false, InventoryParticlesClient.LOGGER::error)*//*?}*/.getFirst();
			} catch (Exception e) {
				InventoryParticlesClient.LOGGER.warn("Failed to decode \"%s\" from JsonObject:".formatted(id), e);
			}
		}
		return fallback;
	}

}
