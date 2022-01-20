/*
 * Copyright (c) 2021, the hapjs-platform Project Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.hapjs.model;

public interface RoutableInfo {
    String getName();

    String getPath();

    String getUri();

    String getComponent();

    String getLaunchMode();
}
