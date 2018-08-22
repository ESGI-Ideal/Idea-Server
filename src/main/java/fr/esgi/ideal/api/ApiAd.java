package fr.esgi.ideal.api;

import fr.esgi.ideal.DatabaseVerticle;
import fr.esgi.ideal.api.dto.Ad;
import fr.esgi.ideal.api.dto.DbConverter;
import fr.esgi.ideal.dao.tables.pojos.Ads;
import io.vertx.core.Future;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.api.RequestParameters;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@AllArgsConstructor
public class ApiAd implements SubApiAlter<Ads, Ad> {
    private final EventBus eventBus;
    private final static AtomicLong rotate = new AtomicLong(0L);

    @Override
    public Ad mapTo(final Ads obj) {
        return DbConverter.toAPI(obj);
    }

    @Override
    public Ads mapFrom(final Ad obj) {
        return DbConverter.toDB(obj);
    }

    @Override
    public Ad mapFrom(final JsonObject obj) {
        return DbConverter.jsonAds(obj);
    }

    @Override
    public Future<List<Ads>> getAll() {
        final Future<List<Ads>> future = Future.future();
        this.eventBus.<List<Ads>>send(DatabaseVerticle.DB_AD_GET_ALL, null, asyncMsg -> {
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
    public Future<Optional<Ads>> get(@NonNull final Long id) {
        final Future<Optional<Ads>> future = Future.future();
        this.eventBus.<Ads>send(DatabaseVerticle.DB_AD_GET_BY_ID, id, asyncMsg -> {
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
        this.eventBus.<Void>send(DatabaseVerticle.DB_AD_DELETE_BY_ID, id, asyncMsg -> {
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
    public Future<Long> createOrUpdate(@NonNull final Ad data) {
        final Future<Long> future = Future.future();
        this.eventBus.<Long>send(DatabaseVerticle.DB_AD_CREATE, DbConverter.toDB(data), asyncMsg -> {
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

    void nextRotate(@NonNull final RoutingContext routingContext) {
        final Optional<Long> id = Optional.ofNullable(((RequestParameters) routingContext.get("parsedParameters")).pathParameter("id").getLong());
        this.get(rotate.getAndIncrement()).setHandler(res -> {
            if(res.succeeded()) {
                if(res.result().isPresent())
                    routingContext.response().setStatusCode(200).end(Json.encode(res.result().get()));
                else {
                    //maybe the rotate arrive at max, try with reset
                    rotate.set(0L);
                    this.get(rotate.get()).setHandler(res2 -> {
                        if(res2.succeeded() && res2.result().isPresent())
                            routingContext.response().setStatusCode(200).end(Json.encode(res2.result().get()));
                        else
                            routingContext.response().setStatusCode(500).end(new JsonObject().put("error", new JsonObject().put("reason", "An error occur on the server")).toString());
                    });
                }
            } else
                routingContext.response().setStatusCode(500).end(new JsonObject().put("error", new JsonObject().put("reason", "An error occur on the server")).toString());
        });
    }
}
