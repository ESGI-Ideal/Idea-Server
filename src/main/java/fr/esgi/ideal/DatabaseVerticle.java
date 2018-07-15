package fr.esgi.ideal;

import com.p6spy.engine.spy.P6ModuleManager;
import com.p6spy.engine.spy.P6SpyOptions;
import com.p6spy.engine.spy.option.SpyDotProperties;
import fr.esgi.ideal.internal.FSIO;
import fr.esgi.ideal.internal.P6Param;
import fr.esgi.ideal.internal.SqlParam;
import fr.pixel.dao.tables.daos.AdsDao;
import fr.pixel.dao.tables.daos.ImagesDao;
import fr.pixel.dao.tables.daos.PartnersDao;
import fr.pixel.dao.tables.daos.UsersDao;
import fr.pixel.dao.tables.pojos.Articles;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.SQLConnection;
import liquibase.Contexts;
import liquibase.Liquibase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.conf.Settings;
import org.jooq.impl.DSL;

import java.io.IOException;
import java.sql.Connection;
import java.util.InvalidPropertiesFormatException;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import static fr.pixel.dao.tables.Articles.ARTICLES;

@Slf4j
public class DatabaseVerticle extends AbstractVerticle {
    public static final String DB_ARTICLE_GET_ALL = "ApiDatabase_Article_GetAll";
    public static final String DB_ARTICLE_GET_BY_ID = "ApiDatabase_Article_GetById";
    public static final String DB_ARTICLE_DELETE_BY_ID = "ApiDatabase_Article_DeleteById";
    public static final String DB_PARTNER_GET_ALL = "ApiDatabase_Partner_GetAll";
    public static final String DB_PARTNER_GET_BY_ID = "ApiDatabase_Partner_GetById";
    public static final String DB_PARTNER_DELETE_BY_ID = "ApiDatabase_Partner_DeleteById";
    public static final String DB_USER_GET_ALL = "ApiDatabase_User_GetAll";
    public static final String DB_USER_GET_BY_ID = "ApiDatabase_User_GetById";
    public static final String DB_USER_AUTH_BY_NAME = "ApiDatabase_User_AuthByName";
    public static final String DB_USER_AUTH_USER_EXIST = "ApiDatabase_User_AuthTestUserExist";
    public static final String DB_USER_DELETE_BY_ID = "ApiDatabase_User_DeleteById";
    public static final String DB_AD_GET_ALL = "ApiDatabase_Ads_GetAll";
    public static final String DB_AD_GET_BY_ID = "ApiDatabse_Ads_GetById";
    public static final String DB_AD_DELETE_BY_ID = "ApiDatabse_Ads_DeleteById";
    public static final String DB_IMAGE_GET_ALL = "ApiDatabse_Image_GetAll";
    public static final String DB_IMAGE_GET_BY_ID = "ApiDatabse_Image_GetById";
    public static final String DB_IMAGE_DELETE_BY_ID = "ApiDatabse_Image_DeleteById";

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
        final JsonObject ds = this.config().getJsonObject("datasource");
        Optional.ofNullable(ds)
                .ifPresent(obj -> {
                    Optional.ofNullable(ds.getString("url"))
                            .ifPresent(url -> obj.put("url", url.replaceFirst("jdbc:", "jdbc:p6spy:")));
                    Optional.ofNullable(ds.getString("driver_class"))
                            .ifPresent(driver -> obj.put("driver_class", "com.p6spy.engine.spy.P6SpyDriver"));
        });
        try {
            final String dialect = this.config().getString("dialect");
            if(dialect == null)
                throw new InvalidPropertiesFormatException("A SQL dialect must be specified");
            final SqlParam param = SqlParam.valueOf(dialect);
            jqDialect = param.jooqSqlDialect;
            initP6spy(param.p6spyParams, this.vertx.getOrCreateContext().config().getString("name", "ideal-def"));
        } catch(final RuntimeException e) {
            throw new InvalidPropertiesFormatException(e);
        }
        this.client = JDBCClient.createShared(this.vertx, ds);
        //startLiquibase.setHandler(startFuture.completer());
        this.initLiquibase(this.config().getJsonObject("liquibase", new JsonObject().put("mustSafeToRunUpdate", true).put("forceDrop", false)),
                           startFuture/*.completer()*/);
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
        this.vertx.eventBus().<Void>consumer(DB_ARTICLE_GET_ALL,
                                             msg -> execSql(msg, dsl -> dsl.selectFrom(ARTICLES).fetchInto(Articles.class)));
        this.vertx.eventBus().<Long>consumer(DB_ARTICLE_GET_BY_ID,
                                             msg -> execSql(msg, dsl -> dsl.selectFrom(ARTICLES).where(ARTICLES.ID.equal(msg.body())).fetchInto(Articles.class)));
        this.vertx.eventBus().<Long>consumer(DB_ARTICLE_DELETE_BY_ID,
                                            msg -> execSqlNoReturn(msg, dsl -> dsl.deleteFrom(ARTICLES).where(ARTICLES.ID.equal(msg.body()))));
        /* Partners */
        this.vertx.eventBus().<Void>consumer(DB_PARTNER_GET_ALL,
                                             msg -> execSql(msg, dsl -> new PartnersDao(dsl.configuration()).findAll()));
        this.vertx.eventBus().<Long>consumer(DB_PARTNER_GET_BY_ID,
                                             msg -> execSql(msg, dsl -> new PartnersDao(dsl.configuration()).findById(msg.body())));
        this.vertx.eventBus().<Long>consumer(DB_PARTNER_DELETE_BY_ID,
                                             msg -> execSqlNoReturn(msg, dsl -> new PartnersDao(dsl.configuration()).deleteById(msg.body())));
        /* Users */
        this.vertx.eventBus().<Void>consumer(DB_USER_GET_ALL,
                                             msg -> execSql(msg, dsl -> new UsersDao(dsl.configuration()).findAll()));
        this.vertx.eventBus().<Long>consumer(DB_USER_GET_BY_ID,
                                             msg -> execSql(msg, dsl -> new UsersDao(dsl.configuration()).fetchOneById(msg.body())));
        this.vertx.eventBus().<Long>consumer(DB_USER_DELETE_BY_ID,
                                             msg -> execSqlNoReturn(msg, dsl -> new UsersDao(dsl.configuration()).deleteById(msg.body())));
        /* Ads */
        this.vertx.eventBus().<Void>consumer(DB_AD_GET_ALL,
                                             msg -> execSql(msg, dsl -> new AdsDao(dsl.configuration()).findAll()));
        this.vertx.eventBus().<Long>consumer(DB_AD_GET_BY_ID,
                                             msg -> execSql(msg, dsl -> new AdsDao(dsl.configuration()).fetchOneById(msg.body())));
        this.vertx.eventBus().<Long>consumer(DB_AD_DELETE_BY_ID,
                                             msg -> execSqlNoReturn(msg, dsl -> new AdsDao(dsl.configuration()).deleteById(msg.body())));
        /* Images */
        this.vertx.eventBus().<Void>consumer(DB_IMAGE_GET_ALL,
                                             msg -> execSql(msg, dsl -> new ImagesDao(dsl.configuration()).findAll()));
        this.vertx.eventBus().<Long>consumer(DB_IMAGE_GET_BY_ID,
                                             msg -> execSql(msg, dsl -> new ImagesDao(dsl.configuration()).fetchOneById(msg.body())));
        this.vertx.eventBus().<Long>consumer(DB_IMAGE_DELETE_BY_ID,
                                             msg -> execSqlNoReturn(msg, dsl -> new ImagesDao(dsl.configuration()).deleteById(msg.body())));
        /* Authentification */
        //TODO
    }

    private final static AtomicBoolean toInitP6spy = new AtomicBoolean(true);
    private synchronized static void initP6spy(@NonNull final P6Param params, @NonNull final String name) throws InvalidPropertiesFormatException { //all calls are synchronous, so in series
        if(toInitP6spy.compareAndSet(true, false)) { //only the first call will init env
            try {
                System.setProperty(SpyDotProperties.OPTIONS_FILE_PROPERTY, FSIO.getResourceAsExternal("spy.properties").toAbsolutePath().toString());
                P6ModuleManager.getInstance().reload(); // make sure to reinit
                // clean table plz (we need to make sure that all the configured factories will be re-loaded)
                //new DefaultJdbcEventListenerFactory().clearCache();

                Optional.ofNullable(P6SpyOptions.getActiveInstance()).ifPresent(instance -> {
                    Optional.ofNullable(instance.getDriverNames())
                            .ifPresent(drivers -> drivers.forEach(driver -> log.debug("P6Spy driver : {}", driver)));
                    log.debug("P6Spy JMX : {}", instance.getJmx());
                    log.debug("P6Spy JMX prefix = {}", instance.getJmxPrefix());
                    log.debug("P6Spy reload properties : {}", instance.getReloadProperties());
                    log.debug("P6Spy StackTrace : {}", instance.getStackTrace());
                    log.debug("P6Spy AutoFlush : {}", instance.getAutoflush());

                    instance.setDriverlist(params.driverJDBC);
                    params.dateFormat.ifPresent(instance::setDateformat);
                    instance.setJmxPrefix(name+"_");

                    log.debug("P6Spy driver-list = {}", instance.getDriverlist());
                    log.debug("P6Spy DB dialect date format : {}", instance.getDatabaseDialectDateFormat());
                    log.debug("P6Spy real DataSource : {}", instance.getRealDataSource());
                });
                System.setProperty(P6Param.P6SpyEnvPrefix+P6SpyOptions.DRIVERLIST, params.driverJDBC);
                params.dateFormat.ifPresent(dateformat ->System.setProperty(P6Param.P6SpyEnvPrefix+P6SpyOptions.DATEFORMAT, dateformat));
                System.setProperty(P6Param.P6SpyEnvPrefix+P6SpyOptions.JMX_PREFIX, "name"+"_");
                //params.URI_base
            } catch (final IOException e) {
                throw new RuntimeException(e); //during jvm init, so no logger and future.fail()
            }
                //driver_class
        }
        //P6Util.forName(driverName);
        //log.debug("FRAMEWORK USING DRIVER == " + DriverManager.getDriver(url).getClass().getName() + " FOR URL " + url);
    }

    private /*static*/ Future<Void> startLiquibase = Future.future();
    private final static AtomicBoolean toInit = new AtomicBoolean(true);
    private synchronized void initLiquibase(@NonNull final JsonObject params, @NonNull final Future<Void> future) {
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
                        else {
                            log.warn("Liquibase not safe to update");
                            if(params.getBoolean("mustSafeToRunUpdate", true))
                                throw new IllegalStateException("Database is not safe to update");
                        }
                        liquibase.update(new Contexts());
                        //liquibase.validate(); //throws if not
                        if(!liquibase.listUnrunChangeSets(null, null).isEmpty()) {
                            log.warn("Failed to upgrade the database!");
                            if(params.getBoolean("dropBeforeUpgrade", false)) {
                                log.warn("Drop database and re-run upgrade");
                                liquibase.dropAll();
                                liquibase.update((Contexts) null);
                                //no check because dropped
                            } else
                                throw new LiquibaseException("All changeSets have not been executed. Missing changeSets to liquibaseExecute !");
                        } //else
                        this.startLiquibase.complete();
                    } catch(final LiquibaseException|IllegalStateException e) {
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

    private static Settings jqConf;
    private /*static*/ SQLDialect jqDialect;

    private synchronized DSLContext mapDslContext(final Connection connection) {
        if(jqConf == null)
            jqConf = new Settings();
            //jqConf = JAXB.unmarshal(this.getClass().getClassLoader().getResourceAsStream("jooq-config.xml"), Settings.class);
            //settings.setStatementType(StatementType.STATIC_STATEMENT);
        return (connection==null)
                ? DSL.using(jqDialect,  jqConf)
                : DSL.using(connection, jqDialect, jqConf);
    }

    private <M> void execSql(@NonNull final Message<M> msg, @NonNull final Function<DSLContext, ?> getter) {
        this.client.getConnection(result -> {
            if (result.succeeded()) {
                final Object tmp = getter.apply(this.mapDslContext(result.result().unwrap()));
                result.result().close();
                msg.reply(Json.encode(tmp));
            } else
                msg.fail(-1, result.cause().getMessage());
        });
    }

    private <M> void execSqlNoReturn(@NonNull final Message<M> msg, @NonNull final Consumer<DSLContext> getter) {
        this.client.getConnection(result -> {
            if (result.succeeded()) {
                getter.accept(this.mapDslContext(result.result().unwrap()));
                result.result().close();
                msg.reply(null);
            } else
                msg.fail(-1, result.cause().getMessage());
        });
    }
}
