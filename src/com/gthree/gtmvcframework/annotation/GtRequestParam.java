package com.gthree.gtmvcframework.annotation;

import java.lang.annotation.*;

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface GtRequestParam {

    /**
     *
     * @return 参数别名，必填
     */
    String value();
}
