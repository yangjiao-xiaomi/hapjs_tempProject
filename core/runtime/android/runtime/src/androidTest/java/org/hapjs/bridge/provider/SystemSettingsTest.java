/*
 * Copyright (c) 2021, the hapjs-platform Project Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.hapjs.bridge.provider;

import android.content.Context;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class SystemSettingsTest {
    @Before
    public void setup() {
        Context appContext = InstrumentationRegistry.getTargetContext();
        appContext.getContentResolver()
                .delete(SettingsProvider.getSystemUri(appContext), null, null);
    }

    @Test
    public void testBoolean() throws Exception {
        boolean value = SystemSettings.getInstance().getBoolean("Boolean", false);
        Assert.assertTrue(!value);
        boolean result = SystemSettings.getInstance().putBoolean("Boolean", true);
        Assert.assertTrue(result);
        value = SystemSettings.getInstance().getBoolean("Boolean", false);
        Assert.assertTrue(value);
    }

    @Test
    public void testFloat() throws Exception {
        float value = SystemSettings.getInstance().getFloat("Float", 0f);
        Assert.assertTrue(value == 0f);
        boolean result = SystemSettings.getInstance().putFloat("Float", 1f);
        Assert.assertTrue(result);
        value = SystemSettings.getInstance().getFloat("Float", 0f);
        Assert.assertTrue(value == 1f);
    }

    @Test
    public void testInt() throws Exception {
        int value = SystemSettings.getInstance().getInt("Int", 0);
        Assert.assertEquals(0, value);
        boolean result = SystemSettings.getInstance().putInt("Int", 1);
        Assert.assertTrue(result);
        value = SystemSettings.getInstance().getInt("Int", 0);
        Assert.assertEquals(1, value);
    }

    @Test
    public void testLong() throws Exception {
        long value = SystemSettings.getInstance().getLong("Long", 0l);
        Assert.assertTrue(value == 0l);
        boolean result = SystemSettings.getInstance().putLong("Long", 1l);
        Assert.assertTrue(result);
        value = SystemSettings.getInstance().getLong("Long", 0l);
        Assert.assertTrue(value == 1l);
    }

    @Test
    public void testString() throws Exception {
        String value = SystemSettings.getInstance().getString("String", null);
        Assert.assertNull(value);
        value = SystemSettings.getInstance().getString("String", "text");
        Assert.assertEquals("text", value);
        boolean result = SystemSettings.getInstance().putString("String", "text");
        Assert.assertTrue(result);
        value = SystemSettings.getInstance().getString("String", null);
        Assert.assertEquals("text", value);
        result = SystemSettings.getInstance().putString("String", null);
        Assert.assertTrue(result);
        value = SystemSettings.getInstance().getString("String", null);
        Assert.assertNull(value);
        value = SystemSettings.getInstance().getString("String", "text");
        Assert.assertEquals("text", value);
    }
}
