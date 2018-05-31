package fr.esgi.ideal;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;

public class DatabaseVerticle extends AbstractVerticle {
    static {
        System.setProperty("hsqldb.reconfig_logging", "false"); //HSQLDB have little problem with loggers when embed
    }

    private JDBCClient jdbcClient;

    @Override
    public void start(final Future<Void> startFuture) throws Exception {
        this.start();
        this.jdbcClient = JDBCClient.createShared(this.vertx, this.config().getJsonObject("database", new JsonObject()
                .put("url", "jdbc:hsqldb:file:db/default")
                .put("driver_class", "org.hsqldb.jdbcDriver")
                .put("max_pool_size", 30)));
        this.jdbcClient.getConnection(ar -> {
            if(ar.succeeded()) {
                //TODO check/create/valid tables
                ar.result().close();
                startFuture.complete();
            } else
                startFuture.fail(ar.cause());
        });
    }

    @Override
    public void stop(final Future<Void> stopFuture) throws Exception {
        this.stop();
        this.jdbcClient.close(stopFuture.completer());
    }
}
