package fr.esgi.ideal;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Verticle;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class AppStartVerticle extends AbstractVerticle {
    @Override
    public void start(final Future<Void> startFuture) {
        this.deploy(ApiRestVerticle.class, new DeploymentOptions().setInstances(4));
        log.info("Module(s) and/or verticle(s) deployment...DONE");
        //startFuture.complete();
    }

    @Override
    public void stop(final Future<Void> stopFuture) {
        log.debug("Undeploying verticle(s)...DONE");
        log.info("Application stopped successfully. Enjoy the elevator music while we're offline...");
        stopFuture.complete();
    }

    private Future<Void> deploy(@NonNull final Class<? extends Verticle> clazz, @NonNull final DeploymentOptions options) {
        final Future<Void> future = Future.future();
        this.vertx.deployVerticle(clazz, options, handler -> {
            if(handler.succeeded()) {
                log.debug("{} started successfully (deployment identifier: {})", clazz.getSimpleName(), handler.result());
                future.complete();
            } else {
                log.error("{} deployment failed due to: ", clazz.getSimpleName(), handler.cause());
                future.fail(handler.cause());
            }
        });
        return future;
    }
}