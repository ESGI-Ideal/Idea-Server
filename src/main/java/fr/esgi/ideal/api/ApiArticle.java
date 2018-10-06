package fr.esgi.ideal.api;

import fr.esgi.ideal.DatabaseVerticle;
import fr.esgi.ideal.api.dto.Article;
import fr.esgi.ideal.api.dto.DbConverter;
import fr.esgi.ideal.dao.tables.pojos.Articles;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Future;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.api.RequestParameter;
import io.vertx.ext.web.api.RequestParameters;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    public void searchArticle(@NonNull final RoutingContext routingContext) {
        final RequestParameters reqParams = routingContext.get("parsedParameters");
        final int limit = Optional.ofNullable(reqParams.queryParameter("limit")).map(RequestParameter::getInteger).orElse(20);
        final int offset = Optional.ofNullable(reqParams.queryParameter("offset")).map(RequestParameter::getInteger).orElse(20);
        final Optional<String> orderBy = Optional.ofNullable(reqParams.queryParameter("orderby")).map(RequestParameter::getString)
                                                 .map(String::trim)
                                                 .map(String::toLowerCase);
        final boolean order = "asc".equalsIgnoreCase(Optional.ofNullable(reqParams.queryParameter("order")).map(RequestParameter::getString).orElse("asc"));
        final Optional<Set<String>> keywords = Optional.ofNullable(reqParams.queryParameter("query")).map(RequestParameter::getString)
                .map(s -> Stream.of(s.split("\\s")).map(String::trim)
                                                         .map(String::toLowerCase)
                                                         .filter(kw -> !kw.isEmpty())
                                                         .collect(Collectors.toSet()))
                .filter(s -> !s.isEmpty());
        if(keywords.isPresent()) {
            final JsonObject jobj = new JsonObject().put("keywords", new JsonArray(new ArrayList<>(keywords.get())))
                                                    .put("limit", limit)
                                                    .put("offset", offset)
                                                    .mergeIn(orderBy.map(ob -> new JsonObject().put("orderby", orderBy.get()).put("order", order))
                                                                    .orElseGet(JsonObject::new));
            this.eventBus.<Integer>send(DatabaseVerticle.DB_ARTICLE_SEARCH_TOTAL, jobj, asyncTotal -> {
                if(asyncTotal.succeeded()) {
                    this.eventBus.<Collection<Articles>>send(DatabaseVerticle.DB_ARTICLE_SEARCH, jobj, asyncMsg -> {
                        if(asyncMsg.succeeded())
                            RouteUtils.send(routingContext, HttpResponseStatus.OK, jobj.put("totalResult", asyncTotal.result().body())
                                                                                       .put("result", asyncMsg.result().body()));
                        else
                            RouteUtils.error(routingContext, "An error occur on the server (phase 2)");
                    });
                } else
                    RouteUtils.error(routingContext, "An error occur on the server (phase 1)");
            });
        } else
            RouteUtils.error(routingContext, "The Id passed is null");
    }

    public void voteArticle(@NonNull final RoutingContext routingContext) {
        final Optional<Long> id = Optional.ofNullable(((RequestParameters) routingContext.get("parsedParameters")).pathParameter("id"))
                .map(RequestParameter::getLong);
        final Optional<Boolean> value = Optional.ofNullable(((RequestParameters) routingContext.get("parsedParameters")).body())
                .map(RequestParameter::getBoolean);
        if(id.isPresent() && value.isPresent())
            this.eventBus.<Void>send(DatabaseVerticle.DB_ARTICLE_VOTE,
                                     new JsonObject().put("articleId", id).put("userId", routingContext.user().principal().getLong("id")).put("like", value),
                                     asyncMsg -> {
                if(asyncMsg.succeeded())
                    RouteUtils.send(routingContext, HttpResponseStatus.OK, null);
                else {
                    log.error("Get error from bus resquest", asyncMsg.cause());
                    RouteUtils.error(routingContext, "An error occur on the server");
                }
            });
        else
            RouteUtils.error(routingContext, "The Id passed is null");
    }

    public void unvoteArticle(@NonNull final RoutingContext routingContext) {
        final Optional<Long> id = Optional.ofNullable(((RequestParameters) routingContext.get("parsedParameters")).pathParameter("id"))
                                          .map(RequestParameter::getLong);
        if(id.isPresent())
            this.eventBus.<Void>send(DatabaseVerticle.DB_ARTICLE_UNVOTE, new JsonObject().put("articleId", id).put("userId", routingContext.user().principal().getLong("id")), asyncMsg -> {
                if(asyncMsg.succeeded())
                    RouteUtils.send(routingContext, HttpResponseStatus.OK, null);
                else {
                    log.error("Get error from bus resquest", asyncMsg.cause());
                    RouteUtils.error(routingContext, "An error occur on the server");
                }
            });
        else
            RouteUtils.error(routingContext, "The Id passed is null");
    }

   /*
        articles.add(Article.builder().id(counter.getAndIncrement()).name("LaveTout").build());
        articles.add(Article.builder().id(counter.getAndIncrement()).name("LaveTout Plus").build());
        articles.add(Article.builder().id(counter.getAndIncrement()).name("L'ESGI pour les nuls").build());
        IntStream.rangeClosed(1, 20).forEach(i -> articles.add(Article.builder().id(counter.getAndIncrement()).name("Carte de crédit "+(i*200)+"pts").build()));
     */
}
