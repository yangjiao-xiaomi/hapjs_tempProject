/*
 * Copyright (c) 2021, the hapjs-platform Project Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.hapjs.logging;

import java.util.Map;

public interface LogProvider {
    String NAME = "log";

    void logCountEvent(String appPackage, String category, String key);

    void logCountEvent(String appPackage, String category, String key, Map<String, String> params);

    void logCalculateEvent(String appPackage, String category, String key, long value);

    void logCalculateEvent(
            String appPackage, String category, String key, long value, Map<String, String> params);

    void logNumericPropertyEvent(String appPackage, String category, String key, long value);

    void logNumericPropertyEvent(
            String appPackage, String category, String key, long value, Map<String, String> params);

    void logStringPropertyEvent(String appPackage, String category, String key, String value);

    void logStringPropertyEvent(
            String appPackage, String category, String key, String value,
            Map<String, String> params);
}
