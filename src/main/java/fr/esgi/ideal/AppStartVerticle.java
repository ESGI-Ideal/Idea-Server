package fr.esgi.ideal;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.CompositeFuture;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Verticle;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class AppStartVerticle extends AbstractVerticle {
    @Override
    public void start(final Future<Void> startFuture) {
        CompositeFuture.join(
                this.deploy(ApiRestVerticle.class, new DeploymentOptions().setInstances(4)),
                this.deploy(DatabaseVerticle.class, new DeploymentOptions().setInstances(1))
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
        //
        log.info("Application stopped successfully.");
        stopFuture.complete();
    }

    private Future<Void> deploy(@NonNull final Class<? extends Verticle> clazz, @NonNull final DeploymentOptions options) {
        final Future<Void> future = Future.future();
        this.vertx.deployVerticle(clazz, options, handler -> {
            if(handler.succeeded()) {
                log.info("{} started successfully (deployment identifier: {})", clazz.getSimpleName(), handler.result());
                future.complete();
            } else {
                log.error("{} deployment failed due to: ", clazz.getSimpleName(), handler.cause());
                future.fail(handler.cause());
            }
        });
        return future;
    }
}