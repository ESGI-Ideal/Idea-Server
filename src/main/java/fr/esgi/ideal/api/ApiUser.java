package fr.esgi.ideal.api;

import fr.esgi.ideal.DatabaseVerticle;
import fr.esgi.ideal.dto.User;
import io.vertx.core.Future;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonArray;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Slf4j
@AllArgsConstructor
public class ApiUser implements SubApi<User> {
    private final EventBus eventBus;

    @Override
    public Future<Collection<?>> getAll() {
        final Future<Collection<?>> future = Future.future();
        this.eventBus.<List<JsonArray>>send(DatabaseVerticle.DB_USER_GET_ALL, null, asyncMsg -> {
            if(asyncMsg.succeeded())
                future.complete(asyncMsg.result().body());
            else {
                log.error("Get error from bus resquest", asyncMsg.cause());
                future.fail(asyncMsg.cause());
            }
        });
        return future;
    }

    @Override
    public Future<Optional<?>> get(@NonNull final Long id) {
        final Future<Optional<?>> future = Future.future();
        this.eventBus.<Optional<JsonArray>>send(DatabaseVerticle.DB_USER_GET_BY_ID, id, asyncMsg -> {
            if(asyncMsg.succeeded())
                future.complete(asyncMsg.result().body());
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
