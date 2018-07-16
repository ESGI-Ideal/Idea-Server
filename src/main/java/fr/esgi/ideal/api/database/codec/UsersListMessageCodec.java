package fr.esgi.ideal.api.database.codec;

import fr.pixel.dao.tables.interfaces.IUsers;
import fr.pixel.dao.tables.pojos.Users;
import io.vertx.core.json.JsonObject;

public class UsersListMessageCodec extends AbstractListMessageCodec<Users> {
    public UsersListMessageCodec() {
        super(new UsersMessageCodec());
    }
}
