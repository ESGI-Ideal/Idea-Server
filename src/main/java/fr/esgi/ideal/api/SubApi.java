package fr.esgi.ideal.api;

import io.vertx.core.Future;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.api.RequestParameters;
import lombok.NonNull;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

public interface SubApi<POJO, DTO> {
    DTO map(POJO obj);

    Future<List<POJO>> getAll();

    default void getAll(@NonNull final RoutingContext routingContext) {
        this.getAll().setHandler(res -> {
            if(res.succeeded())
                RouteUtils.send(routingContext, res.result().stream().map(this::map).collect(Collectors.toList()));
            else
                RouteUtils.error(routingContext, "An error occur on the server");
        });
    }

    Future<Optional<POJO>> get(@NonNull final Long id);

    default void get(@NonNull final RoutingContext routingContext) {
        final Optional<Long> id = Optional.ofNullable(((RequestParameters) routingContext.get("parsedParameters")).pathParameter("id").getLong());
        if(id.isPresent())
            this.get(id.get()).setHandler(res -> {
                if(res.succeeded()) {
                    if(res.result().isPresent())
                        RouteUtils.send(routingContext, this.map(res.result().get()));
                    else
                        RouteUtils.error(routingContext, new NoSuchElementException("This ID not exist"));
                } else
                    RouteUtils.error(routingContext, "An error occur on the server");
            });
        else
            RouteUtils.error(routingContext, "The Id passed is null");
    }

    /*void create();

    void update();

    void delete();

    void createOrUpdate();*/
}
