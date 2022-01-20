/*
 * Copyright (c) 2021, the hapjs-platform Project Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.hapjs.render;

public interface Autoplay {

    void start();

    void stop();

    boolean isRunning();
}
