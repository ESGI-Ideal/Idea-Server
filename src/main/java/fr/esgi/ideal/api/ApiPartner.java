package fr.esgi.ideal.api;

import fr.esgi.ideal.DatabaseVerticle;
import fr.esgi.ideal.api.dto.DbConverter;
import fr.esgi.ideal.api.dto.Partner;
import fr.esgi.ideal.dao.tables.pojos.Partners;
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
public class ApiPartner implements SubApiAlter<Partners, Partner> {
    private final EventBus eventBus;

    @Override
    public Partner mapTo(final Partners obj) {
        return DbConverter.toAPI(obj);
    }

    @Override
    public Partners mapFrom(final Partner obj) {
        return DbConverter.toDB(obj);
    }

    @Override
    public Partner mapFrom(final JsonObject obj) {
        return DbConverter.jsonPartner(obj);
    }

    @Override
    public Future<List<Partners>> getAll() {
        final Future<List<Partners>> future = Future.future();
        this.eventBus.<List<Partners>>send(DatabaseVerticle.DB_PARTNER_GET_ALL, null, asyncMsg -> {
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
    public Future<Optional<Partners>> get(@NonNull final Long id) {
        final Future<Optional<Partners>> future = Future.future();
        this.eventBus.<Partners>send(DatabaseVerticle.DB_PARTNER_GET_BY_ID, id, asyncMsg -> {
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
        this.eventBus.<Void>send(DatabaseVerticle.DB_PARTNER_DELETE_BY_ID, id, asyncMsg -> {
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
    public Future<Long> createOrUpdate(@NonNull final Partner data) {
        final Future<Long> future = Future.future();
        this.eventBus.<Long>send(DatabaseVerticle.DB_PARTNER_CREATE, DbConverter.toDB(data), asyncMsg -> {
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
}
