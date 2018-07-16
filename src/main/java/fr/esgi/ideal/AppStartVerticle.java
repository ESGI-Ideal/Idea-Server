package fr.esgi.ideal;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.CompositeFuture;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.DeploymentOptionsJson;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.lang.management.ManagementFactory;
import java.util.stream.Collectors;

@Slf4j
public final class AppStartVerticle extends AbstractVerticle {
    @Override
    public void start(final Future<Void> startFuture) {
        log.trace("process = {}", ManagementFactory.getRuntimeMXBean().getName());
        log.debug("config() = {}", this.config().encodePrettily());
        CompositeFuture.join(
            this.config().getJsonObject("verticles", new JsonObject()).stream()
                .peek(e -> log.debug("found from config verticle {}", e.getKey()))
                .map(e -> this.deploy(e.getKey(), DeploymentOptionsJson.fromJson(((JsonObject) e.getValue()).getJsonObject("deploymentOptions", new JsonObject()), new DeploymentOptions())))
                .collect(Collectors.toList())
        ).setHandler(ar -> {
            if(ar.succeeded())
                startFuture.complete();
            else
                startFuture.fail(ar.cause());
        });
        log.info("Module(s) and/or verticle(s) deployment... DONE");
    }

    @Override
    public void stop(final Future<Void> stopFuture) {
        log.debug("Undeploying verticle(s)... DONE");
        this.vertx.close();
        log.info("Application stopped successfully.");
        stopFuture.complete();
    }

    private Future<Void> deploy(@NonNull final String clazz, @NonNull final DeploymentOptions options) {
        final Future<Void> future = Future.future();
        log.trace("while deploy {} with {}", clazz, options.toJson().encodePrettily());
        this.vertx.deployVerticle(clazz, options, handler -> {
            if(handler.succeeded()) {
                log.info("{} started successfully (deployment identifier: {})", clazz, handler.result());
                future.complete();
            } else {
                log.error(clazz+" deployment failed due to: ", handler.cause());
                future.fail(handler.cause());
            }
        });
        return future;
    }
}