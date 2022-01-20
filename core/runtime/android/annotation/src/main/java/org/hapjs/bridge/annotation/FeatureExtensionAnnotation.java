/*
 * Copyright (c) 2021, the hapjs-platform Project Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.hapjs.bridge.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS)
public @interface FeatureExtensionAnnotation {
    String name();

    ActionAnnotation[] actions();

    ResidentType residentType() default ResidentType.FORBIDDEN;

    enum ResidentType {
        FORBIDDEN,
        USEABLE,
        RESIDENT_NORMAL,
        RESIDENT_IMPORTANT
    }
}
