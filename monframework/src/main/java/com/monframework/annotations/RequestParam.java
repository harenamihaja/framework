package com.monframework.annotations;

// com.monframework.annotations.RequestParam.java

import java.lang.annotation.*;

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequestParam {
    String value() default "";  // nom du champ dans le formulaire
    boolean required() default true;
}