package com.gthree.gtmvcframework.annotation;

import java.lang.annotation.*;

@Target({ElementType.TYPE,ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface GtRequestMapping {
    /**
     *
     * @return 访问url
     */
    String value() default "";
}
