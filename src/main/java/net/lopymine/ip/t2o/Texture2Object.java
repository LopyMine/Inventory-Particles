package net.lopymine.ip.t2o;

import org.jetbrains.annotations.Nullable;

public interface Texture2Object<T> {

	@Nullable
	T accept(int x, int y, int imageWidth, int imageHeight, int color);

}
