package fr.esgi.ideal.api;

import com.fasterxml.jackson.core.type.TypeReference;
import fr.esgi.ideal.DatabaseVerticle;
import fr.esgi.ideal.api.dto.DbConverter;
import fr.esgi.ideal.api.dto.User;
import fr.pixel.dao.tables.pojos.Users;
import io.vertx.core.Future;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.Json;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;

@Slf4j
@AllArgsConstructor
public class ApiUser implements SubApi<Users, User> {
    private final EventBus eventBus;

    @Override
    public User map(final Users obj) {
        return DbConverter.fromApi(obj);
    }

    @Override
    public Future<List<Users>> getAll() {
        final Future<List<Users>> future = Future.future();
        this.eventBus.<String>send(DatabaseVerticle.DB_USER_GET_ALL, null, asyncMsg -> {
            if(asyncMsg.succeeded())
                future.complete(Json.decodeValue(asyncMsg.result().body(), new TypeReference<List<Users>>(){}));
            else {
                log.error("Get error from bus resquest", asyncMsg.cause());
                future.fail(asyncMsg.cause());
            }
        });
        return future;
    }

    @Override
    public Future<Optional<Users>> get(@NonNull final Long id) {
        final Future<Optional<Users>> future = Future.future();
        this.eventBus.<String>send(DatabaseVerticle.DB_USER_GET_BY_ID, id, asyncMsg -> {
            if(asyncMsg.succeeded())
                future.complete(Json.decodeValue(asyncMsg.result().body(), new TypeReference<Optional<Users>>(){}));
            else {
                log.error("Get error from bus resquest", asyncMsg.cause());
                future.fail(asyncMsg.cause());
            }
        });
        return future;
    }

    /*
    users.add(User.builder().id(1L).mail("user@mail.com").password("password").build()); //logged
    users.add(User.builder().id(2L).mail("other@mail.com").build()); //other
    users.add(User.builder().id(3L).mail("admin@mail.com").isAdmin(true).password("admin").build()); //logged
    */
}
