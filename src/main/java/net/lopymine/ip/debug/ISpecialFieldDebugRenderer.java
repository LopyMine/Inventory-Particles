package net.lopymine.ip.debug;

import java.util.function.*;

@FunctionalInterface
public interface ISpecialFieldDebugRenderer {

	void consumeDebugRender(BiConsumer<String, Object> renderLabel, Consumer<String> decoration, Object value);

}
