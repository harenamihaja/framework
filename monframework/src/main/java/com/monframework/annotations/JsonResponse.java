package com.monframework.annotations;

// package com.monframework.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface JsonResponse {
    // Optionnel : tu peux ajouter des params plus tard (ex: status code, headers...)
}
