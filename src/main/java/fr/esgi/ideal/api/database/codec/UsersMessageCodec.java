package fr.esgi.ideal.api.database.codec;

import fr.pixel.dao.tables.pojos.Users;
import io.vertx.core.json.JsonObject;

public class UsersMessageCodec extends AbstractMessageCodec<Users> {
    @Override
    JsonObject encodeToWire(final Users s) {
        return JsonObject.mapFrom(s);
    }

    @Override
    Users decodeFromWire(final JsonObject json) {
        return new Users(json.getLong("id"), json.getLong("image"), json.getString("mail"),
                         null/*created*/, json.getBoolean("admin"));
    }
}
