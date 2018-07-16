package fr.esgi.ideal.api;

import fr.esgi.ideal.DatabaseVerticle;
import fr.esgi.ideal.api.dto.DbConverter;
import fr.esgi.ideal.api.dto.User;
import fr.pixel.dao.tables.pojos.Users;
import io.vertx.core.Future;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;

@Slf4j
@AllArgsConstructor
public class ApiUser implements SubApiAlter<Users, User> {
    private final EventBus eventBus;

    @Override
    public User mapTo(final Users obj) {
        return DbConverter.toAPI(obj);
    }

    @Override
    public Users mapFrom(final User obj) {
        return DbConverter.toDB(obj);
    }

    @Override
    public User mapFrom(final JsonObject obj) {
        return DbConverter.jsonUser(obj);
    }

    @Override
    public Future<List<Users>> getAll() {
        final Future<List<Users>> future = Future.future();
        this.eventBus.<List<Users>>send(DatabaseVerticle.DB_USER_GET_ALL, null, asyncMsg -> {
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
    public Future<Optional<Users>> get(@NonNull final Long id) {
        final Future<Optional<Users>> future = Future.future();
        this.eventBus.<Users>send(DatabaseVerticle.DB_USER_GET_BY_ID, id, asyncMsg -> {
            if(asyncMsg.succeeded())
                future.complete(Optional.ofNullable(asyncMsg.result().body()));
            else {
                log.error("Get error from bus resquest", asyncMsg.cause());
                future.fail(asyncMsg.cause());
            }
        });
        return future;
    }

    @Override
    public Future<Void> delete(@NonNull final Long id) {
        final Future<Void> future = Future.future();
        this.eventBus.<Void>send(DatabaseVerticle.DB_USER_DELETE_BY_ID, id, asyncMsg -> {
            if(asyncMsg.succeeded())
                future.complete();
            else {
                log.error("Get error from bus resquest", asyncMsg.cause());
                future.fail(asyncMsg.cause());
            }
        });
        return future;
    }

    @Override
    public Future<Long> createOrUpdate(@NonNull final User data) {
        final Future<Long> future = Future.future();
        this.eventBus.<Long>send(DatabaseVerticle.DB_USER_CREATE, DbConverter.toDB(data), asyncMsg -> {
            if(asyncMsg.succeeded())
                //future.complete(DbConverter.toAPI(asyncMsg.result().body()));
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
