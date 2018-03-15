package fr.esgi.ideal;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import lombok.NonNull;

public class MyVerticle extends AbstractVerticle {
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
        this.vertx.createHttpServer()
                .requestHandler(req -> req.response().end("Hello World !"))
                .listen(this.config().getInteger("http.port", 8080), result -> {
                    if(result.succeeded())
                        startFuture.complete();
                    else
                        startFuture.fail(result.cause());
                });
    }
}
