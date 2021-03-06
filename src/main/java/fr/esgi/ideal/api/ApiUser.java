package fr.esgi.ideal.api;

import fr.esgi.ideal.DatabaseVerticle;
import fr.esgi.ideal.api.dto.Article;
import fr.esgi.ideal.api.dto.DbConverter;
import fr.esgi.ideal.api.dto.User;
import fr.esgi.ideal.dao.tables.pojos.Articles;
import fr.esgi.ideal.dao.tables.pojos.Users;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Future;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.api.RequestParameters;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Slf4j
@AllArgsConstructor
public class ApiUser implements SubApiAlter<Users, User> {
    private final EventBus eventBus;

    @Override
    public User mapTo(final Users obj) {
        return DbConverter.toAPI(obj);
    }

    @Override
    public Users mapFrom(final User obj) {
        return DbConverter.toDB(obj);
    }

    @Override
    public User mapFrom(final JsonObject obj) {
        return DbConverter.jsonUser(obj);
    }

    @Override
    public Future<List<Users>> getAll() {
        final Future<List<Users>> future = Future.future();
        this.eventBus.<List<Users>>send(DatabaseVerticle.DB_USER_GET_ALL, null, asyncMsg -> {
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
    public Future<Optional<Users>> get(@NonNull final Long id) {
        final Future<Optional<Users>> future = Future.future();
        this.eventBus.<Users>send(DatabaseVerticle.DB_USER_GET_BY_ID, id, asyncMsg -> {
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
        this.eventBus.<Void>send(DatabaseVerticle.DB_USER_DELETE_BY_ID, id, asyncMsg -> {
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
    public Future<Long> createOrUpdate(@NonNull final User data) {
        final Future<Long> future = Future.future();
        this.eventBus.<Long>send(DatabaseVerticle.DB_USER_CREATE, DbConverter.toDB(data), asyncMsg -> {
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

    public void getAllArticlesCreate(@NonNull final RoutingContext routingContext) {
        final Optional<Long> id = Optional.ofNullable(((RequestParameters) routingContext.get("parsedParameters")).pathParameter("id").getLong());
        if(id.isPresent()) {
            this.eventBus.<Set<Long>>send(DatabaseVerticle.DB_USER_GET_ARTICLES_CREATE, id, asyncMsg -> {
                if(asyncMsg.succeeded())
                    RouteUtils.send(routingContext, HttpResponseStatus.OK, asyncMsg.result().body());
                else
                    RouteUtils.error(routingContext, "An error occur on the server");
                });
        } else
            RouteUtils.error(routingContext, "The Id passed is null");
    }

    public void getUserInfo(@NonNull final RoutingContext routingContext) {
        this.eventBus.<JsonObject>send(DatabaseVerticle.DB_USER_GET_INFOS, ((User)routingContext.user()).getId(), asyncMsg -> {
            if(asyncMsg.succeeded())
                RouteUtils.send(routingContext, HttpResponseStatus.OK, asyncMsg.result().body());
            else
                RouteUtils.error(routingContext, "An error occur on the server");
        });
    }

    public void getUserFavorites(@NonNull final RoutingContext routingContext) {
        this.eventBus.<List<Articles>>send(DatabaseVerticle.DB_USER_GET_ARTICLES_FAVORITES, ((User)routingContext.user()).getId(), asyncMsg -> {
            if(asyncMsg.succeeded())
                RouteUtils.send(routingContext, HttpResponseStatus.OK, asyncMsg.result().body().parallelStream().map(DbConverter::toAPI).toArray(Article[]::new));
            else
                RouteUtils.error(routingContext, "An error occur on the server");
        });
    }

    public void addUserFavorite(@NonNull final RoutingContext routingContext) {
        final Optional<Long> id = Optional.ofNullable(((RequestParameters) routingContext.get("parsedParameters")).pathParameter("id").getLong());
        if(id.isPresent()) {
            this.eventBus.<Void>send(DatabaseVerticle.DB_USER_ADD_ARTICLE_FAVORITES,
                                     new JsonObject().put("article", id.get()).put("user", ((User)routingContext.user()).getId()),
                                     asyncMsg -> {
                if(asyncMsg.succeeded())
                    RouteUtils.send(routingContext, HttpResponseStatus.CREATED, null);
                else
                    RouteUtils.error(routingContext, "An error occur on the server");
            });
        } else
            RouteUtils.error(routingContext, "The Id passed is null");
    }

    public void deleteUserFavorite(@NonNull final RoutingContext routingContext) {
        final Optional<Long> id = Optional.ofNullable(((RequestParameters) routingContext.get("parsedParameters")).pathParameter("id").getLong());
        if(id.isPresent()) {
            this.eventBus.<Void>send(DatabaseVerticle.DB_USER_DELETE_ARTICLE_FAVORITES,
                    new JsonObject().put("article", id.get()).put("user", ((User)routingContext.user()).getId()),
                    asyncMsg -> {
                        if(asyncMsg.succeeded())
                            RouteUtils.send(routingContext, HttpResponseStatus.NO_CONTENT, null);
                        else
                            RouteUtils.error(routingContext, "An error occur on the server");
                    });
        } else
            RouteUtils.error(routingContext, "The Id passed is null");
    }

  /*
    users.add(User.builder().id(1L).mail("user@mail.com").password("password").build()); //logged
    users.add(User.builder().id(2L).mail("other@mail.com").build()); //other
    users.add(User.builder().id(3L).mail("admin@mail.com").isAdmin(true).password("admin").build()); //logged
    */
}
