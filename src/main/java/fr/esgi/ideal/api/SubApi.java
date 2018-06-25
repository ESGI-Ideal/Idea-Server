package fr.esgi.ideal.api;

import io.vertx.core.json.Json;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.api.RequestParameters;
import lombok.NonNull;

import java.util.Collection;
import java.util.Optional;

public interface SubApi<T> {
    Collection<T> getAll();

    default  void getAll(@NonNull final RoutingContext routingContext) {
        routingContext.response().setStatusCode(200).end(Json.encode(this.getAll()));
    }

    Optional<T> get(@NonNull final Long id);

    default void get(@NonNull final RoutingContext routingContext) {
        final Optional<T> res = this.get(((RequestParameters)routingContext.get("parsedParameters")).pathParameter("id").getLong());
        if(res.isPresent())
            routingContext.response().setStatusCode(200).end(Json.encode(res.get()));
        else
            routingContext.response().setStatusCode(404).end(/* TODO: map error to json */);
    }

    /*void create();

    void update();

    void delete();

    void createOrUpdate();*/
}
