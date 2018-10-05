package fr.esgi.ideal;

import fr.esgi.ideal.api.ApiAd;
import fr.esgi.ideal.api.ApiArticle;
import fr.esgi.ideal.api.ApiAuth;
import fr.esgi.ideal.api.ApiImage;
import fr.esgi.ideal.api.ApiPartner;
import fr.esgi.ideal.api.ApiUser;
import fr.esgi.ideal.internal.FSIO;
import fr.esgi.ideal.storage.Storage;
import fr.esgi.ideal.storage.StorageType;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.api.contract.RouterFactoryOptions;
import io.vertx.ext.web.api.contract.openapi3.OpenAPI3RouterFactory;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.ext.web.handler.FaviconHandler;
import io.vertx.ext.web.handler.LoggerHandler;
import io.vertx.ext.web.handler.ResponseContentTypeHandler;
import io.vertx.ext.web.handler.ResponseTimeHandler;
import io.vertx.ext.web.handler.TimeoutHandler;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.util.ThumbnailatorUtils;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Stream;

@Slf4j
public class ApiRestVerticle extends AbstractVerticle {
    private HttpServer httpServer;
    private Storage storage;

    @Override
    public void start(@NonNull final Future<Void> startFuture) throws Exception {
        log.debug("Starting verticle ...");
        log.debug("config() = {}", this.config().encodePrettily());
        this.start();
        log.debug("Thumbnailator supported output formats : {}", String.join(", ", ThumbnailatorUtils.getSupportedOutputFormats()));
        this.init_storage()
            .compose(this::start_getOpenApiCotroller)
            .compose(this::start_server)
            .compose(x -> {
                this.httpServer = x;
                return Future.<Void>succeededFuture();
            })
            //.mapEmpty()
            //.map(this::init_storage)
            .setHandler(startFuture.completer());
        log.debug("Starting complete");
    }

    @Override
    public void stop(@NonNull final Future<Void> stopFuture) throws Exception {
        log.debug("Stopping verticle");
        final Future<Void> fSrv = Future.future();
        this.httpServer.close(fSrv);
        fSrv.compose(v->{final Future<Void> next = Future.future(); this.storage.close(next); return next;})
            .setHandler(stopFuture);
        /*final Future<Void> fStr = Future.future();
        this.storage.close(fStr);
        //((Future)CompositeFuture.join(fSrv, fStr)).setHandler(stopFuture.completer());*/
    }

    private Future<HttpServer> start_server(@NonNull final Router OAPI3Router) {
        // Spec loaded with success
        final Future<HttpServer> future = Future.future();
        /*router.get("/sql").handler(req -> {
            this.vertx.eventBus().<List<JsonArray>>send(DatabaseVerticle.DB_QUERY, "SELECT \"its OK\"", async -> {
                if(async.succeeded())
                    outJson.accept(req, async.result().body());
                else
                    req.response().setStatusCode(500).end(async.cause().getMessage());
            });
        });*/
        final Router router = Router.router(this.vertx);
        addFaviconHandler(router.route()
                .handler(LoggerHandler.create(/*TODO*/)))
                .handler(ResponseTimeHandler.create())
                .handler(TimeoutHandler.create(/*32000*/))
                .handler(CorsHandler.create("*")
                        .maxAgeSeconds(86400)
                        .allowedHeaders(new HashSet<>(Arrays.asList("x-requested-with", "Access-Control-Allow-Origin", "origin", "Content-Type", "accept", "X-PINGARUNER", "X-PINGOTHER", "Authorization", "Bearer")))
                        .allowedMethods(new HashSet<>(Arrays.asList(HttpMethod.values()))))
                /*.handler(context -> {
                    context.response()
                            .putHeader("Access-Control-Allow-Origin", "*")
                            .putHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
                    context.next();
                })*/
                .handler(ResponseContentTypeHandler.create());
        //.handler(BodyHandler.create(/*TODO*/));
        router.mountSubRouter("/", OAPI3Router);
        //router.route("/*").handler(routCtx -> routCtx.fail(400)); //others paths
        this.vertx.createHttpServer()
                .requestHandler(router::accept)
                .listen(this.config().getInteger("http.port", 8080), result -> future.completer());
        return future;
    }

    private Future<Void> init_storage() {
        final JsonObject conf = this.config().getJsonObject("storage");
        try {
            final StorageType type = StorageType.valueOf(conf.getString("type"));
            this.storage = type.getNewInstance().get();
            final Future<Void> future = Future.future();
            this.storage.init(this.vertx, conf.getJsonObject("params", new JsonObject()), future);
            return future;
        } catch (final RuntimeException r) {
            return Future.failedFuture(r);
        }
    }

    private Future<Router> start_getOpenApiCotroller(final Void nop) {
        final Future<Router> future = Future.future();
        try {
            OpenAPI3RouterFactory.create(vertx, FSIO.getResourceAsExternal("openapi_bundle.yaml").toString(), ar -> future.handle(ar.map(routerFactory -> {
                {
                    // Create and mount options to router factory
                    final RouterFactoryOptions options = new RouterFactoryOptions()
                            .setRequireSecurityHandlers(true) //TODO Temp
                            .setMountNotImplementedHandler(true)
                            .setMountValidationFailureHandler(true)
                            .setMountResponseContentTypeHandler(true);
                    routerFactory.setOptions(options);
                }
//                routerFactory.addSecurityHandler("OAuth2", OAuth2AuthHandler.create(
//                        OAuth2Auth.create(vertx, OAuth2FlowType.PASSWORD, new OAuth2ClientOptions().setClientID("acme").setClientSecret("secret").setSite("localhost"))));
                /*{
                    // Add a security handler
                    routerFactory.addSecurityHandler("api_key", routingContext -> {
                        // Handle security here
                        routingContext.next();
                    });
                }*/
                //SwaggerParseResult swaggerParseResult = new OpenAPIV3Parser().readLocation(url, null, OpenApi3Utils.getParseOptions());
                //      if (swaggerParseResult.getMessages().isEmpty()) ...
                {
                    /*routerFactory.addHandlerByOperationId("awesomeOperation", routingContext -> {
                        RequestParameters params = routingContext.get("parsedParameters");
                        RequestParameter body = params.body();
                        JsonObject jsonBody = body.getJsonObject();
                        // Do something with body
                    });
                    routerFactory.addFailureHandlerByOperationId("awesomeOperation", routingContext -> {
                        // Handle failure
                    });*/
                    Stream.<BiConsumer<EventBus, OpenAPI3RouterFactory>>of(ApiRestVerticle::addHandleArticle,
                            ApiRestVerticle::addHandleUser,
                            ApiRestVerticle::addHandlePartner,
                            ApiRestVerticle::addHandleAd)
                            .forEach(fnAdd -> fnAdd.accept(this.vertx.eventBus(), routerFactory));
                    addHandleRoot(routerFactory);
                    addHandleImage(this.vertx, this.vertx.eventBus(), routerFactory, this.storage);
                    //part_auth(routerFactory);
                    //routerFactory.addHandlerByOperationId("doSearch", routingContext -> {}); //TODO
                }
                //routerFactory.addSecurityHandler("jwt_auth", JWTAuthHandler.create(jwtAuthProvider));
                return routerFactory.getRouter();
            })));
        } catch(final IOException e) {
            future.fail(e);
        }
        return future;
    }

    private /*static*/ void addHandleRoot(@NonNull final OpenAPI3RouterFactory routerFactory) {
        routerFactory.addHandlerByOperationId("apiInfos", routingContext -> routingContext.response().end(new JsonObject()
                .put("serveur", new JsonObject()
                                .put("implementation", new JsonObject()
                                        .put("title",  this.getClass().getPackage().getImplementationTitle())
                                        .put("vendor",  this.getClass().getPackage().getImplementationVendor())
                                        .put("version", this.getClass().getPackage().getImplementationVersion()))
                                .put("specification", new JsonObject()
                                        .put("title",  this.getClass().getPackage().getSpecificationTitle())
                                        .put("vendor",  this.getClass().getPackage().getSpecificationVendor())
                                        .put("version", this.getClass().getPackage().getSpecificationVersion()))
                        /*.toString()*/)
                .put("api", new JsonObject().put("version", (String)null))
                .toString()));
    }

    private static void addHandleArticle(@NonNull final EventBus eventBus, @NonNull final OpenAPI3RouterFactory routerFactory) {
        final ApiArticle api = new ApiArticle(eventBus);
        routerFactory.addHandlerByOperationId("getArticles", api::getAll);
        routerFactory.addHandlerByOperationId("getArticle", api::get);
        routerFactory.addHandlerByOperationId("deleteArticle", api::delete);
        routerFactory.addHandlerByOperationId("newArticle", api::create);
        routerFactory.addHandlerByOperationId("doSearch", api::searchArticle);
        routerFactory.addHandlerByOperationId("articleVote", api::voteArticle);
        routerFactory.addHandlerByOperationId("articleUnvote", api::unvoteArticle);
    }

    private static void addHandleUser(@NonNull final EventBus eventBus, @NonNull final OpenAPI3RouterFactory routerFactory) {
        final ApiUser api = new ApiUser(eventBus);
        routerFactory.addHandlerByOperationId("getUsers", api::getAll);
        routerFactory.addHandlerByOperationId("getUser", api::get);
        routerFactory.addHandlerByOperationId("deleteUser", api::delete);
        routerFactory.addHandlerByOperationId("newUser", api::create);
        routerFactory.addHandlerByOperationId("getUserArticlesCreate", api::getAllArticlesCreate);
        //getCurrentUser
        final ApiAuth auth = new ApiAuth(eventBus);
        /*routerFactory.addSecurityHandler("OAuth2", new AuthHandler() {
            @Override
            public AuthHandler addAuthority(String authority) {
                return null;
            }

            @Override
            public AuthHandler addAuthorities(Set<String> authorities) {
                return null;
            }

            @Override
            public void parseCredentials(RoutingContext context, Handler<AsyncResult<JsonObject>> handler) {
            }

            @Override
            public void authorize(User user, Handler<AsyncResult<Void>> handler) {
            }

            @Override
            public void handle(RoutingContext event) {
            }
        });*/
        ///TODO: re-enable authentication
        routerFactory.addHandlerByOperationId("oauth2Token", auth::token);
        routerFactory.addSecurityHandler("OAuth2", auth::prepare_oauth);
        routerFactory.addSecuritySchemaScopeValidator("OAuth2", "user", auth::check_scope_user);
        routerFactory.addSecuritySchemaScopeValidator("OAuth2", "admin", auth::check_scope_admin);
    }

    private static void addHandlePartner(@NonNull final EventBus eventBus, @NonNull final OpenAPI3RouterFactory routerFactory) {
        final ApiPartner api = new ApiPartner(eventBus);
        routerFactory.addHandlerByOperationId("getPartners", api::getAll);
        routerFactory.addHandlerByOperationId("getPartner", api::get);
        routerFactory.addHandlerByOperationId("deletePartner", api::delete);
        routerFactory.addHandlerByOperationId("newPartner", api::create);
    }

    private static void addHandleAd(@NonNull final EventBus eventBus, @NonNull final OpenAPI3RouterFactory routerFactory) {
        final ApiAd api = new ApiAd(eventBus);
        routerFactory.addHandlerByOperationId("getAds", api::getAll);
        routerFactory.addHandlerByOperationId("getAd", api::get);
        routerFactory.addHandlerByOperationId("deleteAd", api::delete);
        routerFactory.addHandlerByOperationId("newAd", api::create);
    }

    private static void addHandleImage(@NonNull final Vertx vertx, @NonNull final EventBus eventBus, @NonNull final OpenAPI3RouterFactory routerFactory, @NonNull final Storage storage) {
        final ApiImage api = new ApiImage(vertx, eventBus, storage);
        routerFactory.addHandlerByOperationId("getImages", api::getAll);
        routerFactory.addHandlerByOperationId("newImage", api::upload);
        routerFactory.addHandlerByOperationId("getImageMetadata", api::get);
        routerFactory.addHandlerByOperationId("deleteImage", api::delete);
        routerFactory.addHandlerByOperationId("getImageFile", api::getFile);
        routerFactory.addHandlerByOperationId("getImageThumb", api::getThumb);
    }

    /* ************************************************************ */

    private final static Function<RoutingContext, HttpServerResponse> respJson = ctx -> ctx.response().putHeader("content-type", "application/json; charset=utf-8");
    private final static BiConsumer<RoutingContext, Object> outJson = (ctx, obj) -> respJson.apply(ctx).end(Json.encodePrettily(obj));

    private static void ifId(@NonNull final RoutingContext ctx, @NonNull final BiConsumer<HttpServerResponse, Long> ifPresent) {
        final String id = ctx.request().getParam("id");
        if(id != null)
            ifPresent.accept(respJson.apply(ctx), Long.valueOf(id));
        else
            ctx.response().setStatusCode(400).end();
    }

    /*private static void part_auth(@NonNull final OpenAPI3RouterFactory routerFactory) { //TODO: implements
        routerFactory.addHandlerByOperationId("getCurrentUser", routingContext -> respJson.apply(routingContext).end("{isConnected:"+isConnected.get()+(isConnected.get()?", {as_user:1":"")+"}"));
        routerFactory.addHandlerByOperationId("loginUser", routingContext -> {isConnected.set(true); outJson.accept(routingContext, logged);});
        routerFactory.addHandlerByOperationId("logoutUser", routingContext -> {isConnected.set(false); respJson.apply(routingContext).end();});
        routerFactory.addHandlerByOperationId("createUser", routingContext -> routingContext.reroute("/auth/login"));
    }*/

    /*private static void part_user(@NonNull final OpenAPI3RouterFactory routerFactory) {
        routerFactory.addHandlerByOperationId("getCurrentUser", routingContext -> outJson.accept(routingContext, logged));
        routerFactory.addHandlerByOperationId("createUser", routingContext -> ifId(routingContext, (resp, id) -> resp.end(Json.encode("Not yet implemented")))); //TODO: implements
        routerFactory.addHandlerByOperationId("updateUser", routingContext -> ifId(routingContext, (resp, id) -> resp.end(Json.encode("Not yet implemented")))); //TODO: implements
        routerFactory.addHandlerByOperationId("getUserById", routingContext -> ifId(routingContext, (resp, id) -> resp.end(Json.encode(other))));
    }*/

    private static Route addFaviconHandler(@NonNull final Route route) {
        try {
            route.handler(FaviconHandler.create(FSIO.getResourceAsExternal("favicon.ico").toString()));
        } catch(final IOException e) {
            log.warn("can't attach favicon", e);
        }
        return route;
    }
}
