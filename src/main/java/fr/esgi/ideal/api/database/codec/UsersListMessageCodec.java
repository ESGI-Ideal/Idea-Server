package fr.esgi.ideal.api.database.codec;

import fr.esgi.ideal.dao.tables.pojos.Users;

public class UsersListMessageCodec extends AbstractListMessageCodec<Users> {
    public UsersListMessageCodec() {
        super(new UsersMessageCodec());
    }
}
