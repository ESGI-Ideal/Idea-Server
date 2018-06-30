package fr.esgi.ideal.api;

import com.fasterxml.jackson.core.type.TypeReference;
import fr.esgi.ideal.DatabaseVerticle;
import fr.esgi.ideal.api.dto.Ad;
import fr.esgi.ideal.api.dto.DbConverter;
import fr.pixel.dao.tables.pojos.Ads;
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
public class ApiAd implements SubApi<Ads, Ad> {
    private final EventBus eventBus;
    private final static AtomicLong rotate = new AtomicLong(0L);

    @Override
    public Ad map(final Ads obj) {
        return DbConverter.fromApi(obj);
    }

    @Override
    public Future<List<Ads>> getAll() {
        final Future<List<Ads>> future = Future.future();
        this.eventBus.<String>send(DatabaseVerticle.DB_AD_GET_ALL, null, asyncMsg -> {
            if(asyncMsg.succeeded())
                future.complete(Json.decodeValue(asyncMsg.result().body(), new TypeReference<List<Ads>>(){}));
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
        this.eventBus.<String>send(DatabaseVerticle.DB_AD_GET_BY_ID, id, asyncMsg -> {
            if(asyncMsg.succeeded())
                future.complete(Json.decodeValue(asyncMsg.result().body(), new TypeReference<Optional<Ads>>(){}));
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
