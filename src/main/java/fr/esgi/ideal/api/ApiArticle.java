package fr.esgi.ideal.api;

import fr.esgi.ideal.DatabaseVerticle;
import fr.esgi.ideal.api.dto.Article;
import fr.esgi.ideal.api.dto.DbConverter;
import fr.pixel.dao.tables.pojos.Articles;
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
public class ApiArticle implements SubApiAlter<Articles, Article> {
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
    public Article mapFrom(final JsonObject obj) {
        return DbConverter.jsonArticle(obj);
    }

    @Override
    public Future<List<Articles>> getAll() {
        final Future<List<Articles>> future = Future.future();
        this.eventBus.<List<Articles>>send(DatabaseVerticle.DB_ARTICLE_GET_ALL, null, asyncMsg -> {
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
    public Future<Optional<Articles>> get(@NonNull final Long id) {
        final Future<Optional<Articles>> future = Future.future();
        this.eventBus.<Articles>send(DatabaseVerticle.DB_ARTICLE_GET_BY_ID, id, asyncMsg -> {
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

    @Override
    public Future<Long> createOrUpdate(@NonNull final Article data) {
        final Future<Long> future = Future.future();
        this.eventBus.<Long>send(DatabaseVerticle.DB_ARTICLE_CREATE, DbConverter.toDB(data), asyncMsg -> {
            if(asyncMsg.succeeded()) {
                //future.complete(DbConverter.toAPI(asyncMsg.result().body()));
                log.debug("Insert id {}", asyncMsg.result().body());
                future.complete(asyncMsg.result().body()); }
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
