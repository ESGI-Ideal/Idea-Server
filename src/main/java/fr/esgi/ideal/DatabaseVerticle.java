package fr.esgi.ideal;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.SQLConnection;
import liquibase.Contexts;
import liquibase.Liquibase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

@Slf4j
public class DatabaseVerticle extends AbstractVerticle {
    public static final String DB_ARTICLE_GET_ALL = "ApiDatabase_Article_GetAll";
    public static final String DB_ARTICLE_GET_BY_ID = "ApiDatabase_Article_GetById";
    public static final String DB_PARTNER_GET_ALL = "ApiDatabase_Partner_GetAll";
    public static final String DB_PARTNER_GET_BY_ID = "ApiDatabase_Partner_GetById";
    public static final String DB_USER_GET_ALL = "ApiDatabase_User_GetAll";
    public static final String DB_USER_GET_BY_ID = "ApiDatabase_User_GetById";
    public static final String DB_AD_GET_ALL = "ApiDatabase_Ads_GetAll";
    public static final String DB_AD_GET_BY_ID = "ApiDatabse_Ads_GetById";

    static {
        System.setProperty("hsqldb.reconfig_logging", "false"); //HSQLDB have little problem with loggers when embed
    }

    public final static String DB_QUERY = "ApiDatabase_Query";

    private /*SQLClient*/ JDBCClient client;
    private final static String DB_CHANGELOG = "liquibase/db-changelog.xml";

    @Override
    public void start(final Future<Void> startFuture) throws Exception {
        log.debug("Starting verticle ...");
        log.debug("config() = {}", this.config().encodePrettily());
        this.start();
        /*.put("driver_class", "org.postgresql.Driver")
        .put("url", "jdbc:postgresql://localhost:5432/ideal?tcpKeepAlive=true&loglevel=debug")
        .put("password", "ideal-pwd")*/
        this.client = JDBCClient.createShared(this.vertx, this.config().getJsonObject("datasource"));
        //startLiquibase.setHandler(startFuture.completer());
        this.initLiquibase(startFuture/*.completer()*/);
        this.initBusConsummers();
        log.debug("Starting complete");
    }

    @Override
    public void stop(final Future<Void> stopFuture) throws Exception {
        log.debug("Stopping verticle");
        this.stop();
        this.client.close(stopFuture.completer());
    }

    private void initBusConsummers() {
        /* Articles */
        this.vertx.eventBus().<Void>consumer(DB_ARTICLE_GET_ALL, msg -> this.client.query("Select * from articles", result -> {
            if(result.succeeded())
                msg.reply(result.result().getResults());
            else
                msg.fail(-1, result.cause().getMessage());
        }));
        this.vertx.eventBus().<Long>consumer(DB_ARTICLE_GET_BY_ID, msg -> this.client.queryWithParams("Select * from articles where id=?", new JsonArray().add(msg.body()), result -> {
            if(result.succeeded()) {
                if(result.result().getNumRows() > 0)
                    msg.reply(Optional.of(result.result().getResults().get(0)));
                else
                    msg.reply(Optional.empty());
            } else
                msg.fail(-1, result.cause().getMessage());
        }));
        /* Partners */
        this.vertx.eventBus().<Void>consumer(DB_PARTNER_GET_ALL, msg -> this.client.query("select * from partners", result -> {
            if(result.succeeded())
                msg.reply(result.result().getResults());
            else
                msg.fail(-1, result.cause().getMessage());
        }));
        this.vertx.eventBus().<Long>consumer(DB_PARTNER_GET_BY_ID, msg -> this.client.querySingleWithParams("select * from partners where id=? limit 1", new JsonArray().add(msg.body()), result -> {
            if(result.succeeded())
                msg.reply(Optional.ofNullable(result.result()));
            else
                msg.fail(-1, result.cause().getMessage());
        }));
        /* Users */
        this.vertx.eventBus().<Void>consumer(DB_USER_GET_ALL, msg -> this.client.query("select * from users", result -> {
            if(result.succeeded())
                msg.reply(result.result().getResults());
            else
                msg.fail(-1, result.cause().getMessage());
        }));
        this.vertx.eventBus().<Long>consumer(DB_USER_GET_BY_ID, msg -> this.client.querySingleWithParams("select * from users where id=? limit 1", new JsonArray().add(msg.body()), result -> {
            if(result.succeeded())
                msg.reply(Optional.ofNullable(result.result()));
            else
                msg.fail(-1, result.cause().getMessage());
        }));
        /* Ads */
        this.vertx.eventBus().<Void>consumer(DB_AD_GET_ALL, msg -> this.client.query("select * from ads", result -> {
            if(result.succeeded())
                msg.reply(result.result().getResults());
            else
                msg.fail(-1, result.cause().getMessage());
        }));
        this.vertx.eventBus().<Long>consumer(DB_AD_GET_BY_ID, msg -> this.client.querySingleWithParams("select * from ads where id=? limit 1", new JsonArray().add(msg.body()), result -> {
            if(result.succeeded())
                msg.reply(Optional.ofNullable(result.result()));
            else
                msg.fail(-1, result.cause().getMessage());
        }));
    }

    private /*static*/ Future<Void> startLiquibase = Future.future();
    private final static AtomicBoolean toInit = new AtomicBoolean(true);
    private synchronized void initLiquibase(@NonNull final Future<Void> future) {
        if(toInit.compareAndSet(true, false)) {
            log.debug("Choose for init database");
            startLiquibase.setHandler(future.completer());
            this.client.getConnection(ar -> {
                if(ar.succeeded()) {
                    try(final SQLConnection connection = ar.result()) {
                        final Liquibase liquibase = new Liquibase(DB_CHANGELOG, new ClassLoaderResourceAccessor(), new JdbcConnection(connection.unwrap()));
                        log.trace("liquibase's logger used : {}", liquibase.getLog().getClass().getName());
                        //~ Get State
                        log.debug("Use ChangeLog '{}'", liquibase.getChangeLogFile());
                        Stream.of(liquibase.listLocks()).forEach(lock -> log.debug("lock in database : {} the {} by {}", lock.getId(), lock.getLockGranted(), lock.getLockedBy()));
                        liquibase.listUnrunChangeSets(null, null)
                                .forEach(cs -> log.info("Changelog unrun : {} by {} in file {}", cs.getId(), cs.getAuthor(), cs.getFilePath()));
                        if(liquibase.isSafeToRunUpdate())
                            log.info("Liquibase safe to update");
                        else
                            log.warn("Liquibase not safe to update");
                        liquibase.update(new Contexts());
                        //liquibase.validate(); //throws if not
                        if(!liquibase.listUnrunChangeSets(null, null).isEmpty()) {
                            log.warn("Failed to upgrade the database!");
                            if(this.config().getBoolean("dropBeforeUpgrade", false)) {
                                liquibase.dropAll();
                                liquibase.update((Contexts) null);
                                //no check because dropped
                            } else
                                throw new LiquibaseException("All changeSets have not been executed. Missing changeSets to liquibaseExecute !");
                        } //else
                        this.startLiquibase.complete();
                    } catch(final LiquibaseException e) {
                        log.error("Error while checking/updating the database", e);
                        this.startLiquibase.fail(e);
                    }
                } else
                    this.startLiquibase.fail(ar.cause());
            });
        } else {
            log.debug("Wait database initialisation");
            future.complete();
            //startLiquibase.updateAndGet(prev -> Future.<Void>future().setHandler(completer));
        }
    }
}
