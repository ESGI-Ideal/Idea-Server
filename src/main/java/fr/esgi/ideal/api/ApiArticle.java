package fr.esgi.ideal.api;

import com.fasterxml.jackson.core.type.TypeReference;
import fr.esgi.ideal.DatabaseVerticle;
import fr.esgi.ideal.api.dto.Article;
import fr.esgi.ideal.api.dto.DbConverter;
import fr.pixel.dao.tables.pojos.Articles;
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
public class ApiArticle implements SubApi<Articles, Article> {
    private final EventBus eventBus;

    @Override
    public Article mapTo(final Articles obj) {
        return DbConverter.toAPI(obj);
    }

    @Override
    public Articles mapFrom(final Article obj) {
        return DbConverter.toDB(obj);
    }

    @Override
    public Future<List<Articles>> getAll() {
        final Future<List<Articles>> future = Future.future();
        this.eventBus.<String>send(DatabaseVerticle.DB_ARTICLE_GET_ALL, null, asyncMsg -> {
            if(asyncMsg.succeeded())
                future.complete(Json.decodeValue(asyncMsg.result().body(), new TypeReference<List<Articles>>(){}));
            else {
                log.error("Get error from bus resquest", asyncMsg.cause());
                future.fail(asyncMsg.cause());
            }
        });
        return future;
    }

    @Override
    public Future<Optional<Articles>> get(@NonNull final Long id) {
        final Future<Optional<Articles>> future = Future.future();
        this.eventBus.<String>send(DatabaseVerticle.DB_ARTICLE_GET_BY_ID, id, asyncMsg -> {
            if(asyncMsg.succeeded())
                future.complete(Json.decodeValue(asyncMsg.result().body(), new TypeReference<Optional<Articles>>(){}));
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
        this.eventBus.<Void>send(DatabaseVerticle.DB_ARTICLE_DELETE_BY_ID, id, asyncMsg -> {
            if(asyncMsg.succeeded())
                future.complete();
            else {
                log.error("Get error from bus resquest", asyncMsg.cause());
                future.fail(asyncMsg.cause());
            }
        });
        return future;
    }

   /*
        articles.add(Article.builder().id(counter.getAndIncrement()).name("LaveTout").build());
        articles.add(Article.builder().id(counter.getAndIncrement()).name("LaveTout Plus").build());
        articles.add(Article.builder().id(counter.getAndIncrement()).name("L'ESGI pour les nuls").build());
        IntStream.rangeClosed(1, 20).forEach(i -> articles.add(Article.builder().id(counter.getAndIncrement()).name("Carte de crédit "+(i*200)+"pts").build()));
     */
}
