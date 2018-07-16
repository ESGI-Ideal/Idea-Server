package fr.esgi.ideal.api;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.api.RequestParameters;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

public interface SubApiAlter<POJO, DTO> extends SubApi<POJO, DTO> {
    POJO mapFrom(DTO obj);
    DTO mapFrom(JsonObject obj);

    Future<Long> createOrUpdate(@NonNull final DTO data);

    /**
     * POST
     */
    default void create(@NonNull final RoutingContext routingContext) {
        this.createOrUpdate(mapFrom(((RequestParameters) routingContext.get("parsedParameters")).body().getJsonObject())).setHandler(res -> {
            if(res.succeeded()) {
                //routingContext.response().putHeader("location", Objects.toString(res.result())).setStatusCode(302).end();
                RouteUtils.send(routingContext, res.result());
            } else
                RouteUtils.error(routingContext, "An error occur on the server");
        });
    }

    /**
     * PATCH
     * /
    default void update(@NonNull final RoutingContext routingContext) {
        final Optional<Long> id = Optional.ofNullable(((RequestParameters) routingContext.get("parsedParameters")).pathParameter("id").getLong());
        final JsonObject body = ((RequestParameters) routingContext.get("parsedParameters")).body().getJsonObject();
        if(id.isPresent() && body != null)
            this.get(id.get()).setHandler(res -> {
                if(res.succeeded() && res.result().isPresent()) {
                    POJO prev = res.result().get();
                    JsonObject json = body.mergeIn(JsonObject.mapFrom(prev));
                    POJO next = json.mapTo(prev.getClass());
                    //RouteUtils.send(routingContext, null);
                    this.createOrUpdate().setHandler(ar -> {
                        if(ar.succeeded())
                            RouteUtils.send(routingContext, HttpResponseStatus.OK, ar.result());
                        else
                            RouteUtils.error(routingContext, "An error occur during update");
                    });
                } else
                    RouteUtils.error(routingContext, "An error occur on the server");
            });
        else
            RouteUtils.error(routingContext, "The Id passed is null");
    }*/
}
