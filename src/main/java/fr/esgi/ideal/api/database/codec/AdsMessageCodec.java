package fr.esgi.ideal.api.database.codec;

import fr.pixel.dao.tables.pojos.Ads;
import io.vertx.core.json.JsonObject;

public class AdsMessageCodec extends AbstractMessageCodec<Ads> {
    @Override
    JsonObject encodeToWire(final Ads s) {
        return JsonObject.mapFrom(s);
    }

    @Override
    Ads decodeFromWire(final JsonObject json) {
        return new Ads(json.getLong("id"), json.getString("title"), json.getLong("image"));
    }
}
