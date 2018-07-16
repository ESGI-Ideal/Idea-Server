package fr.esgi.ideal.api.database.codec;

import fr.pixel.dao.tables.interfaces.IPartners;
import fr.pixel.dao.tables.pojos.Partners;
import io.vertx.core.json.JsonObject;

public class PartnersMessageCodec extends AbstractMessageCodec<Partners> {
    @Override
    JsonObject encodeToWire(final Partners s) {
        return JsonObject.mapFrom(s);
    }

    @Override
    Partners decodeFromWire(final JsonObject json) {
        return new Partners(json.getLong("id"), json.getString("name"), json.getString("description"), json.getLong("image"));
    }
}
