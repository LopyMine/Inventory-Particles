package net.lopymine.ip.t2o;

import net.lopymine.ip.utils.ArgbUtils;

public interface Texture2ObjectPixelFilter {

	Texture2ObjectPixelFilter NOT_TRANSPARENT = () -> (x, y, imageWidth, imageHeight, color) -> ArgbUtils.getAlpha(color) != 0;

	Texture2Object<Boolean> getFilter();

}
