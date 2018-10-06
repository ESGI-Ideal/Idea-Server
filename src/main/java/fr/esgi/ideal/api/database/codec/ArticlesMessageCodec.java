package fr.esgi.ideal.api.database.codec;

import fr.esgi.ideal.dao.tables.pojos.Articles;
import io.vertx.core.json.JsonObject;

public class ArticlesMessageCodec extends AbstractMessageCodec<Articles> {
    @Override
    JsonObject encodeToWire(final Articles s) {
        return JsonObject.mapFrom(s);
    }

    @Override
    Articles decodeFromWire(final JsonObject json) {
        return json.mapTo(Articles.class);
        //return new Articles(json.getLong("id"), json.getString("name"), json.getLong("image"), json.getString("description"),
        //              null/*price*/, null/*created*/, null/*updated*/, json.getLong("rate"));
    }
}
