package fr.esgi.ideal.api.database.codec;

import fr.pixel.dao.tables.interfaces.IArticles;
import fr.pixel.dao.tables.interfaces.IImages;
import fr.pixel.dao.tables.pojos.Articles;
import fr.pixel.dao.tables.pojos.Images;
import io.vertx.core.json.JsonObject;

public class ArticlesMessageCodec extends AbstractMessageCodec<Articles> {
    @Override
    JsonObject encodeToWire(final Articles s) {
        return JsonObject.mapFrom(s);
    }

    @Override
    Articles decodeFromWire(final JsonObject json) {
        return new Articles(json.getLong("id"), json.getString("name"), json.getLong("image"), json.getString("description"),
                      null/*price*/, null/*created*/, null/*updated*/, json.getLong("rate"));
    }
}
