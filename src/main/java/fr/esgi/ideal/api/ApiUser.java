package fr.esgi.ideal.api;

import fr.esgi.ideal.dto.User;

import java.util.HashSet;
import java.util.Set;

public class ApiUser extends SubApiAdaptor<User> {
    public ApiUser() {
        super(User::getId, initUsers());
    }

    private static User[] initUsers() {
        Set<User> users = new HashSet<>(2);
        users.add(User.builder().id(1L).mail("user@mail.com").password("password").build()); //logged
        users.add(User.builder().id(2L).mail("other@mail.com").build()); //other
        users.add(User.builder().id(3L).mail("admin@mail.com").isAdmin(true).password("admin").build()); //logged
        return users.toArray(new User[users.size()]);
    }
}
