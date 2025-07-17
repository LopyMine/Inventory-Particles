package net.lopymine.ip.debug;

import java.lang.annotation.*;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface HideInDebugRender {

}
