package io.vertx.core;

import io.vertx.core.json.JsonObject;
import lombok.NonNull;

/**
 * Simple bridge to {@link DeploymentOptionsConverter}
 */
public class DeploymentOptionsJson  {
    public static DeploymentOptions fromJson(@NonNull final JsonObject json, @NonNull final DeploymentOptions obj) {
        DeploymentOptionsConverter.fromJson(json, obj);
        return obj;
    }

    public static void toJson(@NonNull final DeploymentOptions obj, @NonNull final JsonObject json) {
        DeploymentOptionsConverter.toJson(obj, json);
    }
}
