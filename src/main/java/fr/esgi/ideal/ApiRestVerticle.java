package fr.esgi.ideal;

import fr.esgi.ideal.dto.Article;
import fr.esgi.ideal.dto.TmpMap;
import fr.esgi.ideal.dto.User;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.IntStream;

import static io.vertx.core.http.HttpMethod.DELETE;
import static io.vertx.core.http.HttpMethod.GET;
import static io.vertx.core.http.HttpMethod.PATCH;
import static io.vertx.core.http.HttpMethod.POST;
import static io.vertx.core.http.HttpMethod.PUT;

@Slf4j
public class ApiRestVerticle extends AbstractVerticle {
    private HttpServer httpServer;

    @Override
    public void start(@NonNull Future<Void> startFuture) throws Exception {
        log.debug("Starting verticle ...");
        log.debug("config() = {}", this.config().encodePrettily());
        this.start();
        final Router router = Router.router(vertx);
        part_article(router);
        part_user(router);
        part_auth(router);
        part_ads(router);
        router.route("/*").handler(routCtx -> routCtx.fail(400)); //others paths
        this.httpServer = this.vertx.createHttpServer()
                .requestHandler(router::accept)
                .listen(this.config().getInteger("http.port", 8080), result -> {
                    if(result.succeeded())
                        startFuture.complete();
                    else
                        startFuture.fail(result.cause());
                });
        log.debug("Starting complete");
    }

    @Override
    public void stop(final Future<Void> stopFuture) throws Exception {
        log.debug("Stopping verticle");
        this.stop();
        this.httpServer.close(stopFuture.completer());
    }

    private final static Function<RoutingContext, HttpServerResponse> respJson = ctx -> ctx.response().putHeader("content-type", "application/json; charset=utf-8");
    private final static BiConsumer<RoutingContext, Object> outJson = (ctx, obj) -> respJson.apply(ctx).end(Json.encodePrettily(obj));

    private static <O extends TmpMap> void add(@NonNull final Map<Integer, O> map, @NonNull final O... obj) {
        Arrays.stream(obj).forEach(o -> map.put(o.getId(), o));
    }

    private static void ifId(@NonNull final RoutingContext ctx, @NonNull final BiConsumer<HttpServerResponse, Integer> ifPresent) {
        final String id = ctx.request().getParam("id");
        if(id != null)
            ifPresent.accept(respJson.apply(ctx), Integer.valueOf(id));
        else
            ctx.response().setStatusCode(400).end();
    }

    private static void part_article(@NonNull final Router router) {
        AtomicInteger counter = new AtomicInteger(1);
        final LinkedHashMap<Integer, Article> articles = new LinkedHashMap<>();
        add(articles, Article.builder().id(counter.getAndIncrement()).name("LaveTout").build());
        add(articles, Article.builder().id(counter.getAndIncrement()).name("LaveTout Plus").build());
        add(articles, Article.builder().id(counter.getAndIncrement()).name("L'ESGI pour les nuls").build());
        IntStream.rangeClosed(1, 20).forEach(i -> add(articles, Article.builder().id(counter.getAndIncrement()).name("Carte de crédit "+(i*200)+"pts").build()));
        router.route(HttpMethod.GET, "/article").handler(routingContext -> outJson.accept(routingContext, articles.values()));
        router.route(POST, "/article").handler(routingContext -> routingContext.reroute(PUT, "/article/"+counter.getAndIncrement()));
        router.route("/article").handler(BodyHandler.create()); //handler for reading body of requests
        router.route(GET, "/article/:id").handler(routingContext -> outJson.accept(routingContext, articles.get(Integer.valueOf(routingContext.request().getParam("id")))));
        router.route(PUT, "/article/:id").handler(routingContext -> ifId(routingContext, (resp, id) -> {
            final Article article = Json.decodeValue(routingContext.getBodyAsString(), Article.class);
            if(article.getId() == null) article.setId(counter.getAndIncrement());
            resp.setStatusCode(201).end(Json.encodePrettily(articles.putIfAbsent(article.getId(), article)));
        }));
        router.route(DELETE, "/article/:id").handler(routingContext -> ifId(routingContext, (resp, id) -> {
            articles.remove(id);
            resp.setStatusCode(204).end();
        }));
        router.route(PATCH, "/article/:id").handler(routingContext -> ifId(routingContext, (resp, id) -> resp.end(Json.encode("Not yet implemented")))); //TODO: implements
        router.route(GET, "/search").handler(routingContext -> routingContext.reroute("/article")); //TODO: to implements
    }

    private final static User
            logged = User.builder().id(1).mail("user@mail.com").password("password").build(),
            other = User.builder().id(2).mail("other@mail.com").build();
    private static AtomicBoolean isConnected = new AtomicBoolean(false);
    private static void part_auth(@NonNull final Router router) { //TODO: implements
        router.route(GET, "/auth").handler(ctx -> respJson.apply(ctx).end("{isConnected:"+isConnected.get()+(isConnected.get()?", {as_user:1":"")+"}"));
        router.route(POST, "/auth/login").handler(ctx -> {isConnected.set(true); outJson.accept(ctx, logged);});
        router.route(POST, "/auth/logout").handler(ctx -> {isConnected.set(false); respJson.apply(ctx).end();});
        router.route(POST, "/auth/register").handler(ctx -> ctx.reroute("/auth/login"));
    }

    private static void part_user(@NonNull final Router router) {
        router.route(GET, "/user").handler(routingContext -> outJson.accept(routingContext, logged));
        router.route(PUT, "/user").handler(routingContext -> ifId(routingContext, (resp, id) -> resp.end(Json.encode("Not yet implemented")))); //TODO: implements
        router.route(PATCH, "/user").handler(routingContext -> ifId(routingContext, (resp, id) -> resp.end(Json.encode("Not yet implemented")))); //TODO: implements
        router.route(GET, "/user/:id").handler(routingContext -> ifId(routingContext, (resp, id) -> resp.end(Json.encode(other))));
    }

    private static void part_ads(@NonNull final Router router) {
        final AtomicInteger counter = new AtomicInteger();
        router.route(GET, "/ads/next").handler(ctx -> ctx.reroute("/ads/"+counter.getAndIncrement()));
        router.route(GET, "/ads/:id").handler(ctx -> outJson.accept(ctx, new Object()));
    }
}
