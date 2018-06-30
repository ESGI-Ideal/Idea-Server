package fr.esgi.ideal.api;

import com.fasterxml.jackson.core.type.TypeReference;
import fr.esgi.ideal.DatabaseVerticle;
import fr.esgi.ideal.api.dto.DbConverter;
import fr.esgi.ideal.api.dto.Partner;
import fr.pixel.dao.tables.pojos.Partners;
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
public class ApiPartner implements SubApi<Partners, Partner> {
    private final EventBus eventBus;

    @Override
    public Partner map(final Partners obj) {
        return DbConverter.fromApi(obj);
    }

    @Override
    public Future<List<Partners>> getAll() {
        final Future<List<Partners>> future = Future.future();
        this.eventBus.<String>send(DatabaseVerticle.DB_PARTNER_GET_ALL, null, asyncMsg -> {
            if(asyncMsg.succeeded())
                future.complete(Json.decodeValue(asyncMsg.result().body(), new TypeReference<List<Partners>>(){}));
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
        this.eventBus.<String>send(DatabaseVerticle.DB_PARTNER_GET_BY_ID, id, asyncMsg -> {
            if(asyncMsg.succeeded())
                future.complete(Json.decodeValue(asyncMsg.result().body(), new TypeReference<Optional<Partners>>(){}));
            else {
                log.error("Get error from bus resquest", asyncMsg.cause());
                future.fail(asyncMsg.cause());
            }
        });
        return future;
    }
}
