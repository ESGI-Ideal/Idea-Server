package io.vertx.core;

import io.vertx.core.json.JsonObject;
import lombok.NonNull;

/**
 * Simple public bridge to {@link VertxOptionsConverter}
 */
public class VertxOptionsJson extends VertxOptionsConverter {
    public static void fromJson(@NonNull final JsonObject json, @NonNull final VertxOptions obj) {
        VertxOptionsConverter.fromJson(json, obj);
    }

    public static void toJson(@NonNull final VertxOptions obj, @NonNull final JsonObject json) {
        VertxOptionsConverter.toJson(obj, json);
    }
}
