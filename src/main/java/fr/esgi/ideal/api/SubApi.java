package fr.esgi.ideal.api;

import io.vertx.core.Future;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.api.RequestParameters;
import lombok.NonNull;

import java.util.Collection;
import java.util.Optional;

public interface SubApi<T> {
    Future<Collection</*T*/?>> getAll();

    default void getAll(@NonNull final RoutingContext routingContext) {
        this.getAll().setHandler(res -> {
            if(res.succeeded())
                routingContext.response().setStatusCode(200).end(Json.encode(res.result()));
            else
                routingContext.response().setStatusCode(500).end(new JsonObject().put("error", new JsonObject().put("reason", "An error occur on the server")).toString());
        });
    }

    Future<Optional</*T*/?>> get(@NonNull final Long id);

    default void get(@NonNull final RoutingContext routingContext) {
        final Optional<Long> id = Optional.ofNullable(((RequestParameters) routingContext.get("parsedParameters")).pathParameter("id").getLong());
        if(id.isPresent())
            this.get(id.get()).setHandler(res -> {
                if(res.succeeded()) {
                    if(res.result().isPresent())
                        routingContext.response().setStatusCode(200).end(Json.encode(res.result().get()));
                    else
                        routingContext.response().setStatusCode(404).end(new JsonObject().put("error", new JsonObject().put("reason", "This ID not exist")).toString());
                } else
                    routingContext.response().setStatusCode(500).end(new JsonObject().put("error", new JsonObject().put("reason", "An error occur on the server")).toString());
            });
        else
            routingContext.response().setStatusCode(500).end(new JsonObject().put("error", new JsonObject().put("reason", "The Id passed is null")).toString());
    }

    /*void create();

    void update();

    void delete();

    void createOrUpdate();*/
}