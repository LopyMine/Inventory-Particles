package net.lopymine.ip.utils;

//? if <1.21 {

import com.google.common.base.Suppliers;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.*;

import java.util.function.*;

public class RecursiveCodec<T> implements Codec<T> {

	private final String name;
	private final Supplier<Codec<T>> wrapped;

	public RecursiveCodec(final String name, final Function<Codec<T>, Codec<T>> wrapped) {
		this.name    = name;
		this.wrapped = Suppliers.memoize(() -> wrapped.apply(this));
	}

	@Override
	public <S> DataResult<Pair<T, S>> decode(final DynamicOps<S> ops, final S input) {
		return wrapped.get().decode(ops, input);
	}

	@Override
	public <S> DataResult<S> encode(final T input, final DynamicOps<S> ops, final S prefix) {
		return wrapped.get().encode(input, ops, prefix);
	}

	@Override
	public String toString() {
		return "RecursiveCodec[" + name + ']';
	}
}

//?}
