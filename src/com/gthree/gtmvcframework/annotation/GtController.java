package com.gthree.gtmvcframework.annotation;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface GtController {
    /**
     *
     * @return 控制器别名
     */
    String value() default "";
}
