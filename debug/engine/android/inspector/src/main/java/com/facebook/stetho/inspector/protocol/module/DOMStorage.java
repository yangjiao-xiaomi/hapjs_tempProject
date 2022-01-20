/*
 * Copyright (c) 2014-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */

package com.facebook.stetho.inspector.protocol.module;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;
import com.facebook.stetho.inspector.console.CLog;
import com.facebook.stetho.inspector.domstorage.DOMStoragePeerManager;
import com.facebook.stetho.inspector.domstorage.SharedPreferencesHelper;
import com.facebook.stetho.inspector.jsonrpc.JsonRpcException;
import com.facebook.stetho.inspector.jsonrpc.JsonRpcPeer;
import com.facebook.stetho.inspector.jsonrpc.JsonRpcResult;
import com.facebook.stetho.inspector.protocol.ChromeDevtoolsDomain;
import com.facebook.stetho.inspector.protocol.ChromeDevtoolsMethod;
import com.facebook.stetho.json.ObjectMapper;
import com.facebook.stetho.json.annotation.JsonProperty;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.hapjs.inspector.V8Inspector;
import org.hapjs.runtime.inspect.protocols.IDOMStorage;
import org.json.JSONException;
import org.json.JSONObject;

public class DOMStorage implements ChromeDevtoolsDomain {
    private static final String TAG = "DOMStorage"; // INSPECTOR ADD

    private final Context mContext;
    private final DOMStoragePeerManager mDOMStoragePeerManager;
    private final ObjectMapper mObjectMapper = new ObjectMapper();
    // INSPECTOR ADD
    private IDOMStorage mDOMStorage;
    private boolean mSupportLocalStorage = true;
    // END

    public DOMStorage(Context context) {
        mContext = context;
        mDOMStoragePeerManager = new DOMStoragePeerManager(context);
    }

    private static void assignByType(SharedPreferences.Editor editor, String key, Object value)
            throws IllegalArgumentException {
        if (value instanceof Integer) {
            editor.putInt(key, (Integer) value);
        } else if (value instanceof Long) {
            editor.putLong(key, (Long) value);
        } else if (value instanceof Float) {
            editor.putFloat(key, (Float) value);
        } else if (value instanceof Boolean) {
            editor.putBoolean(key, (Boolean) value);
        } else if (value instanceof String) {
            editor.putString(key, (String) value);
        } else if (value instanceof Set) {
            putStringSet(editor, key, (Set<String>) value);
        } else {
            throw new IllegalArgumentException("Unsupported type=" + value.getClass().getName());
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private static void putStringSet(SharedPreferences.Editor editor, String key,
                                     Set<String> value) {
        editor.putStringSet(key, value);
    }

    @ChromeDevtoolsMethod
    public void enable(JsonRpcPeer peer, JSONObject params) {
        mDOMStoragePeerManager.addPeer(peer);
    }
    // END

    @ChromeDevtoolsMethod
    public void disable(JsonRpcPeer peer, JSONObject params) {
        mDOMStoragePeerManager.removePeer(peer);
    }

    // INSPECTOR ADD
    @ChromeDevtoolsMethod
    public void clear(JsonRpcPeer peer, JSONObject params) throws JSONException {
        StorageId storage =
                mObjectMapper.convertValue(params.getJSONObject("storageId"), StorageId.class);
        if (storage != null && storage.isLocalStorage) {
            IDOMStorage domStorage = getDOMStorage();
            if (domStorage != null) {
                Log.d(TAG, "clear storage success");
                domStorage.clear();
            } else {
                Log.e(TAG, "can't find domStorage");
            }
        }
    }

    @ChromeDevtoolsMethod
    public JsonRpcResult getDOMStorageItems(JsonRpcPeer peer, JSONObject params)
            throws JSONException {
        StorageId storage =
                mObjectMapper.convertValue(params.getJSONObject("storageId"), StorageId.class);

        ArrayList<List<String>> entries = new ArrayList<List<String>>();

        // INSPECTOR ADD:
        if (storage != null) {
            String prefTag = storage.securityOrigin;
            if (storage.isLocalStorage) {
                // INSPECTOR ADD
                if (prefTag.equals(V8Inspector.getInstance().getDebugPackage())) {
                    IDOMStorage localStorage = getDOMStorage();
                    if (localStorage != null) {
                        Map<String, String> localEntries = localStorage.entries();
                        for (Map.Entry<String, String> localEntry : localEntries.entrySet()) {
                            ArrayList<String> entry = new ArrayList<String>(2);
                            entry.add(localEntry.getKey());
                            entry.add(localEntry.getValue());
                            entries.add(entry);
                        }
                    }
                } else {
                    SharedPreferences prefs =
                            mContext.getSharedPreferences(prefTag, Context.MODE_PRIVATE);
                    for (Map.Entry<String, ?> prefsEntry : prefs.getAll().entrySet()) {
                        ArrayList<String> entry = new ArrayList<String>(2);
                        entry.add(prefsEntry.getKey());
                        entry.add(SharedPreferencesHelper.valueToString(prefsEntry.getValue()));
                        entries.add(entry);
                    }
                }
            }
        }
        GetDOMStorageItemsResult result = new GetDOMStorageItemsResult();
        result.entries = entries;
        return result;
    }

    @ChromeDevtoolsMethod
    public void setDOMStorageItem(JsonRpcPeer peer, JSONObject params)
            throws JSONException, JsonRpcException {
        StorageId storage =
                mObjectMapper.convertValue(params.getJSONObject("storageId"), StorageId.class);
        String key = params.getString("key");
        String value = params.getString("value");

        // INSPECTOR MOD:
        // if (storage.isLocalStorage) {
        if (storage != null && storage.isLocalStorage) {
            // INSPECTOR ADD:
            String tag = storage.securityOrigin;
            if (tag.equals(V8Inspector.getInstance().getDebugPackage())) {
                IDOMStorage localStorage = getDOMStorage();
                if (localStorage != null) {
                    localStorage.setItem(key, value);
                }
                return;
            }
            // INSPECTOR END
            SharedPreferences prefs =
                    mContext.getSharedPreferences(storage.securityOrigin, Context.MODE_PRIVATE);
            Object existingValue = prefs.getAll().get(key);
            try {
                if (existingValue == null) {
                    throw new DOMStorageAssignmentException(
                            "Unsupported: cannot add new key " + key
                                    + " due to lack of type inference");
                } else {
                    SharedPreferences.Editor editor = prefs.edit();
                    try {
                        assignByType(
                                editor, key,
                                SharedPreferencesHelper.valueFromString(value, existingValue));
                        editor.apply();
                    } catch (IllegalArgumentException e) {
                        throw new DOMStorageAssignmentException(
                                String.format(
                                        Locale.US,
                                        "Type mismatch setting %s to %s (expected %s)",
                                        key,
                                        value,
                                        existingValue.getClass().getSimpleName()));
                    }
                }
            } catch (DOMStorageAssignmentException e) {
                CLog.writeToConsole(
                        mDOMStoragePeerManager,
                        Console.MessageLevel.ERROR,
                        Console.MessageSource.STORAGE,
                        e.getMessage());

                // Force the DevTools UI to refresh with the old value again (it assumes that the set
                // operation succeeded).  Note that we should be able to do this by throwing
                // JsonRpcException but the UI doesn't respect setDOMStorageItem failure.
                if (prefs.contains(key)) {
                    mDOMStoragePeerManager.signalItemUpdated(
                            storage, key, value,
                            SharedPreferencesHelper.valueToString(existingValue));
                } else {
                    mDOMStoragePeerManager.signalItemRemoved(storage, key);
                }
            }
        }
    }
    // END

    @ChromeDevtoolsMethod
    public void removeDOMStorageItem(JsonRpcPeer peer, JSONObject params) throws JSONException {
        StorageId storage =
                mObjectMapper.convertValue(params.getJSONObject("storageId"), StorageId.class);
        String key = params.getString("key");

        // INSPECTOR MOD:
        // if (storage.isLocalStorage) {
        if (storage != null && storage.isLocalStorage) {
            String tag = storage.securityOrigin;
            if (tag.equals(V8Inspector.getInstance().getDebugPackage())) {
                IDOMStorage localStorage = getDOMStorage();
                if (localStorage != null) {
                    localStorage.removeItem(key);
                }
                return;
            }
            // END
            SharedPreferences prefs =
                    mContext.getSharedPreferences(storage.securityOrigin, Context.MODE_PRIVATE);
            prefs.edit().remove(key).apply();
        }
    }

    // INSPECTOR ADD
    private IDOMStorage getDOMStorage() {
        if (mDOMStorage == null && mSupportLocalStorage) {
            try {
                Class<?> storageClass =
                        Class.forName("org.hapjs.features.storage.data.DOMStorageImpl");
                Method getInstanceMethod = storageClass.getMethod("getInstance", String.class);
                mDOMStorage =
                        (IDOMStorage)
                                getInstanceMethod
                                        .invoke(null, V8Inspector.getInstance().getDebugPackage());
                mSupportLocalStorage = true;
            } catch (NoSuchMethodException
                    | IllegalAccessException
                    | InvocationTargetException
                    | ClassNotFoundException e) {
                Log.e(TAG, "getDOMStorage", e);
                CLog.writeToConsole(
                        mDOMStoragePeerManager,
                        Console.MessageLevel.ERROR,
                        Console.MessageSource.STORAGE,
                        "Inspecting local storage is not supported, please update your platform");
                mSupportLocalStorage = false;
            }
        }
        return mDOMStorage;
    }

    public static class StorageId {
        @JsonProperty(required = true)
        public String securityOrigin;

        @JsonProperty(required = true)
        public boolean isLocalStorage;
    }

    private static class GetDOMStorageItemsResult implements JsonRpcResult {
        @JsonProperty(required = true)
        public List<List<String>> entries;
    }

    public static class DomStorageItemsClearedParams {
        @JsonProperty(required = true)
        public StorageId storageId;
    }

    public static class DomStorageItemRemovedParams {
        @JsonProperty(required = true)
        public StorageId storageId;

        @JsonProperty(required = true)
        public String key;
    }

    public static class DomStorageItemAddedParams {
        @JsonProperty(required = true)
        public StorageId storageId;

        @JsonProperty(required = true)
        public String key;

        @JsonProperty(required = true)
        public String newValue;
    }

    public static class DomStorageItemUpdatedParams {
        @JsonProperty(required = true)
        public StorageId storageId;

        @JsonProperty(required = true)
        public String key;

        @JsonProperty(required = true)
        public String oldValue;

        @JsonProperty(required = true)
        public String newValue;
    }

    /**
     * Exception thrown internally when we fail to honor {@link #setDOMStorageItem}.
     */
    private static class DOMStorageAssignmentException extends Exception {
        public DOMStorageAssignmentException(String message) {
            super(message);
        }
    }
}
