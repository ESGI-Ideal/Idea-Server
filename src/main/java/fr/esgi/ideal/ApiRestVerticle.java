package fr.esgi.ideal;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.api.contract.RouterFactoryOptions;
import io.vertx.ext.web.api.contract.openapi3.OpenAPI3RouterFactory;
import lombok.NonNull;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class ApiRestVerticle extends AbstractVerticle {
    /**
     * Start the verticle.<p>
     * This is called by Vert.x when the verticle instance is deployed. Don't call it yourself.<p>
     * If your verticle does things in its startup which take some time then you can override this method
     * and call the startFuture some time later when start up is complete.
     *
     * @param startFuture a future which should be called when verticle start-up is complete.
     * @throws Exception
     */
    @Override
    public void start(@NonNull Future<Void> startFuture) throws Exception {
        this.start();
        OpenAPI3RouterFactory.create(vertx, "src/main/resources/openapi.yaml", ar -> {
            if(ar.failed()) {
                // Something went wrong during router factory initialization
                startFuture.fail(ar.cause());
            } else/*if(ar.succeeded())*/ {
                // Spec loaded with success
                final OpenAPI3RouterFactory routerFactory = ar.result();
                {
                    // Create and mount options to router factory
                    final RouterFactoryOptions options = new RouterFactoryOptions()
                            .setRequireSecurityHandlers(false) //TODO Temp
                            .setMountNotImplementedHandler(true)
                            .setMountValidationFailureHandler(true)
                            .setMountResponseContentTypeHandler(true);
                    routerFactory.setOptions(options);
                }
                /*{
                    // Add a security handler
                    routerFactory.addSecurityHandler("api_key", routingContext -> {
                        // Handle security here
                        routingContext.next();
                    });
                }*/
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
                    //part_auth(routerFactory);
                    //routerFactory.addHandlerByOperationId("doSearch", routingContext -> {}); //TODO
                    routerFactory.addHandlerByOperationId("getArticles", routingContext -> outJson.accept(routingContext, TmpDB.getInstance().getArticles().values()));
                    routerFactory.addHandlerByOperationId("getUsers", routingContext -> outJson.accept(routingContext, TmpDB.getInstance().getUsers().values()));
                    routerFactory.addHandlerByOperationId("getPartners", routingContext -> outJson.accept(routingContext, TmpDB.getInstance().getPartners().values()));
                }
                //routerFactory.addSecurityHandler("jwt_auth", JWTAuthHandler.create(jwtAuthProvider));
                final Router router = routerFactory.getRouter();
                //router.route("/*").handler(routCtx -> routCtx.fail(400)); //others paths
                this.vertx.createHttpServer()
                        .requestHandler(router::accept)
                        .listen(this.config().getInteger("http.port", 8080), result -> {
                            if(result.succeeded())
                                startFuture.complete();
                            else
                                startFuture.fail(result.cause());
                        });
            }
        });
    }

    private final static Function<RoutingContext, HttpServerResponse> respJson = ctx -> ctx.response().putHeader("content-type", "application/json; charset=utf-8");
    private final static BiConsumer<RoutingContext, Object> outJson = (ctx, obj) -> respJson.apply(ctx).end(Json.encodePrettily(obj));

    private static void ifId(@NonNull final RoutingContext ctx, @NonNull final BiConsumer<HttpServerResponse, Long> ifPresent) {
        final String id = ctx.request().getParam("id");
        if(id != null)
            ifPresent.accept(respJson.apply(ctx), Long.valueOf(id));
        else
            ctx.response().setStatusCode(400).end();
    }

    public static void deleteIdObj(@NonNull final RoutingContext ctx, @NonNull final Map<Long, ?> db) {
        db.remove(Long.valueOf(ctx.request().getParam("id")));
    }

    private static AtomicBoolean isConnected = new AtomicBoolean(false);
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

    private static void part_ads(@NonNull final OpenAPI3RouterFactory routerFactory) {
        final AtomicLong counter = new AtomicLong();
        routerFactory.addHandlerByOperationId("getNextAd", routingContext -> routingContext.reroute("/ad/"+counter.getAndIncrement()));
        routerFactory.addHandlerByOperationId("getAd", routingContext -> outJson.accept(routingContext, new JsonObject()));
    }
}
