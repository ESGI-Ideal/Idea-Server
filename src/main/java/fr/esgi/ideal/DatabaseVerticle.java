package fr.esgi.ideal;

import com.p6spy.engine.spy.P6ModuleManager;
import com.p6spy.engine.spy.P6SpyOptions;
import com.p6spy.engine.spy.option.SpyDotProperties;
import fr.esgi.ideal.api.database.codec.AbstractListMessageCodec;
import fr.esgi.ideal.api.database.codec.AdsListMessageCodec;
import fr.esgi.ideal.api.database.codec.AdsMessageCodec;
import fr.esgi.ideal.api.database.codec.ArticlesListMessageCodec;
import fr.esgi.ideal.api.database.codec.ArticlesMessageCodec;
import fr.esgi.ideal.api.database.codec.ImagesListMessageCodec;
import fr.esgi.ideal.api.database.codec.ImagesMessageCodec;
import fr.esgi.ideal.api.database.codec.PartnersListMessageCodec;
import fr.esgi.ideal.api.database.codec.PartnersMessageCodec;
import fr.esgi.ideal.api.database.codec.UsersListMessageCodec;
import fr.esgi.ideal.api.database.codec.UsersMessageCodec;
import fr.esgi.ideal.dao.tables.daos.AdsDao;
import fr.esgi.ideal.dao.tables.daos.ArticlesDataDao;
import fr.esgi.ideal.dao.tables.daos.ArticlesRatesDao;
import fr.esgi.ideal.dao.tables.daos.ImagesDao;
import fr.esgi.ideal.dao.tables.daos.PartnersDao;
import fr.esgi.ideal.dao.tables.daos.UsersDao;
import fr.esgi.ideal.dao.tables.daos.UsersFavoritesDao;
import fr.esgi.ideal.dao.tables.pojos.Ads;
import fr.esgi.ideal.dao.tables.pojos.Articles;
import fr.esgi.ideal.dao.tables.pojos.ArticlesData;
import fr.esgi.ideal.dao.tables.pojos.ArticlesRates;
import fr.esgi.ideal.dao.tables.pojos.Images;
import fr.esgi.ideal.dao.tables.pojos.Partners;
import fr.esgi.ideal.dao.tables.pojos.Users;
import fr.esgi.ideal.dao.tables.pojos.UsersFavorites;
import fr.esgi.ideal.dao.tables.records.UsersFavoritesRecord;
import fr.esgi.ideal.internal.FSIO;
import fr.esgi.ideal.internal.P6Param;
import fr.esgi.ideal.internal.SqlParam;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.eventbus.DeliveryOptions;
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
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.ResultQuery;
import org.jooq.SQLDialect;
import org.jooq.SelectConditionStep;
import org.jooq.SelectLimitStep;
import org.jooq.SelectSelectStep;
import org.jooq.conf.Settings;
import org.jooq.impl.DSL;

import java.io.IOException;
import java.sql.Connection;
import java.util.InvalidPropertiesFormatException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import static fr.esgi.ideal.dao.tables.Ads.ADS;
import static fr.esgi.ideal.dao.tables.Articles.ARTICLES;
import static fr.esgi.ideal.dao.tables.ArticlesData.ARTICLES_DATA;
import static fr.esgi.ideal.dao.tables.ArticlesRates.ARTICLES_RATES;
import static fr.esgi.ideal.dao.tables.Partners.PARTNERS;
import static fr.esgi.ideal.dao.tables.Users.USERS;
import static fr.esgi.ideal.dao.tables.UsersFavorites.USERS_FAVORITES;
import static org.jooq.SortOrder.ASC;
import static org.jooq.SortOrder.DESC;

@Slf4j
public class DatabaseVerticle extends AbstractVerticle {
    public static final String DB_ARTICLE_GET_ALL = "ApiDatabase_Article_GetAll";
    public static final String DB_ARTICLE_GET_BY_ID = "ApiDatabase_Article_GetById";
    public static final String DB_ARTICLE_DELETE_BY_ID = "ApiDatabase_Article_DeleteById";
    public static final String DB_ARTICLE_CREATE = "ApiDatabase_Article_Create";
    public static final String DB_ARTICLE_SEARCH = "ApiDatabase_Article_Search";
    public static final String DB_ARTICLE_SEARCH_TOTAL = "ApiDatabase_Article_SearchTotal";
    public static final String DB_ARTICLE_VOTE = "ApiDatabase_Article_Vote";
    public static final String DB_ARTICLE_UNVOTE = "ApiDatabase_Article_UnVote";
    public static final String DB_PARTNER_GET_ALL = "ApiDatabase_Partner_GetAll";
    public static final String DB_PARTNER_GET_BY_ID = "ApiDatabase_Partner_GetById";
    public static final String DB_PARTNER_DELETE_BY_ID = "ApiDatabase_Partner_DeleteById";
    public static final String DB_PARTNER_CREATE = "ApiDatabase_Partner_Create";
    public static final String DB_USER_GET_ALL = "ApiDatabase_User_GetAll";
    public static final String DB_USER_GET_BY_ID = "ApiDatabase_User_GetById";
    //public static final String DB_USER_AUTH_BY_NAME = "ApiDatabase_User_AuthByName";
    //public static final String DB_USER_AUTH_USER_EXIST = "ApiDatabase_User_AuthTestUserExist";
    public static final String DB_USER_GET_BY_MAIL = "ApiDatabase_User_GetByName";
    public static final String DB_USER_DELETE_BY_ID = "ApiDatabase_User_DeleteById";
    public static final String DB_USER_CREATE = "ApiDatabase_User_Create";
    public static final String DB_USER_GET_ARTICLES_CREATE = "ApiDatabase_User_ArticlesCreate";
    public static final String DB_USER_GET_INFOS = "ApiDatabase_User_AboutMe";
    public static final String DB_USER_GET_ARTICLES_FAVORITES = "ApiDatabase_User_ArticlesFavorites_GetAll";
    public static final String DB_USER_ADD_ARTICLE_FAVORITES = "ApiDatabase_User_ArticlesFavorites_Add";
    public static final String DB_USER_DELETE_ARTICLE_FAVORITES = "ApiDatabase_User_ArticlesFavorites_Delete";
    public static final String DB_AD_GET_ALL = "ApiDatabase_Ads_GetAll";
    public static final String DB_AD_GET_BY_ID = "ApiDatabse_Ads_GetById";
    public static final String DB_AD_DELETE_BY_ID = "ApiDatabse_Ads_DeleteById";
    public static final String DB_AD_CREATE = "ApiDatabse_Ads_Create";
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
        this.vertx.eventBus().registerDefaultCodec(Users.class, new UsersMessageCodec());
        this.vertx.eventBus().registerDefaultCodec(Ads.class, new AdsMessageCodec());
        this.vertx.eventBus().registerDefaultCodec(Articles.class, new ArticlesMessageCodec());
        this.vertx.eventBus().registerDefaultCodec(Partners.class, new PartnersMessageCodec());
        this.vertx.eventBus().registerDefaultCodec(Images.class, new ImagesMessageCodec());
        this.vertx.eventBus().registerCodec(new UsersListMessageCodec());
        this.vertx.eventBus().registerCodec(new AdsListMessageCodec());
        this.vertx.eventBus().registerCodec(new ArticlesListMessageCodec());
        this.vertx.eventBus().registerCodec(new PartnersListMessageCodec());
        this.vertx.eventBus().registerCodec(new ImagesListMessageCodec());
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
                                             msg -> execSqlCodec(msg, ArticlesListMessageCodec.class,
                                                                 dsl -> dsl.selectFrom(ARTICLES).fetchInto(Articles.class)));
        this.vertx.eventBus().<Long>consumer(DB_ARTICLE_GET_BY_ID,
                                             msg -> execSqlRaw(msg, dsl -> {try{return dsl.selectFrom(ARTICLES).where(ARTICLES.ID.equal(msg.body())).fetchInto(Articles.class).get(0);}catch(RuntimeException r){return null;}}));
        this.vertx.eventBus().<Long>consumer(DB_ARTICLE_DELETE_BY_ID,
                                             msg -> execSqlNoReturn(msg, dsl -> {
                                                 dsl.deleteFrom(ARTICLES_RATES).where(ARTICLES_RATES.ARTICLE.eq(msg.body()));
                                                 dsl.deleteFrom(USERS_FAVORITES).where(USERS_FAVORITES.ARTICLEID.eq(msg.body()));
                                                 new ArticlesDataDao(dsl.configuration()).deleteById(msg.body());
                                             }));
        this.vertx.eventBus().<Articles>consumer(DB_ARTICLE_CREATE,
                msg -> execSqlRaw(msg, dsl -> dsl.insertInto(ARTICLES_DATA, ARTICLES_DATA.NAME, ARTICLES_DATA.DESCRIPTION, ARTICLES_DATA.PRICE,
                                                             ARTICLES_DATA.UPDATED, ARTICLES_DATA.CREATED, ARTICLES_DATA.IMAGE, ARTICLES_DATA.CREATEBY)
                          .values(/*msg.body().getId(),*/ msg.body().getName(), msg.body().getDescription(), msg.body().getPrice(),
                                  msg.body().getUpdated(), msg.body().getCreated(), msg.body().getImage(), msg.body().getCreateby())
                          .returning().fetchOne().getId()/*.into(Articles.class)*/));
        final BiFunction<JsonObject, SelectSelectStep<? extends Record>, ResultQuery<? extends Record>> sqlSearch = (jsobj, select) -> Optional
                .ofNullable(jsobj.getString("orderby"))
                .map(v -> (Function<SelectConditionStep<? extends Record>, SelectLimitStep<? extends Record>>)
                          (s -> s.orderBy(ARTICLES_DATA.field(v).sort(jsobj.getBoolean("order", false) ? ASC : DESC))))
                .orElse(s -> s)
                .apply(select.from(ARTICLES)
                             .where(jsobj.getJsonArray("keywords").stream().map(String.class::cast)
                                                                                .map(ARTICLES.NAME.trim().lower()::like)
                                                                                .map(Condition.class::cast) //seem to be explicit for reduce
                                                                                .reduce(Condition::or).orElse(null)))
                .limit(jsobj.getInteger("offset", 20), jsobj.getInteger("limit", 20));
        this.vertx.eventBus().<JsonObject>consumer(DB_ARTICLE_SEARCH,
                                                   msg -> execSqlCodec(msg, ArticlesListMessageCodec.class, dsl -> sqlSearch.apply(msg.body(), dsl.select(ARTICLES.fields())).fetchInto(Articles.class)));
        this.vertx.eventBus().<JsonObject>consumer(DB_ARTICLE_SEARCH_TOTAL,
                                                   msg -> execSqlRaw(msg, dsl -> sqlSearch.apply(msg.body(), dsl.selectCount()).fetchOne(0, int.class)));
        this.vertx.eventBus().<JsonObject>consumer(DB_ARTICLE_VOTE,
                                                   msg -> execSqlNoReturn(msg, dsl -> new ArticlesRatesDao(dsl.configuration()).insert(new ArticlesRates(msg.body().getLong("articleId"),
                                                                                                                                                         msg.body().getLong("userId"),
                                                                                                                                                         msg.body().getBoolean("like")))));
        this.vertx.eventBus().<JsonObject>consumer(DB_ARTICLE_UNVOTE,
                                                   msg -> execSqlNoReturn(msg, dsl -> dsl.deleteFrom(ARTICLES_RATES).where(ARTICLES_RATES.ARTICLE.eq(msg.body().getLong("articleId")))
                                                                                                                    .and(ARTICLES_RATES.USER.eq(msg.body().getLong("userId")))));

        /* Partners */
        this.vertx.eventBus().<Void>consumer(DB_PARTNER_GET_ALL,
                                             msg -> execSqlCodec(msg, PartnersListMessageCodec.class, dsl -> new PartnersDao(dsl.configuration()).findAll()));
        this.vertx.eventBus().<Long>consumer(DB_PARTNER_GET_BY_ID,
                                             msg -> execSqlRaw(msg, dsl -> new PartnersDao(dsl.configuration()).findById(msg.body())));
        this.vertx.eventBus().<Long>consumer(DB_PARTNER_DELETE_BY_ID,
                                             msg -> execSqlNoReturn(msg, dsl -> new PartnersDao(dsl.configuration()).deleteById(msg.body())));
        this.vertx.eventBus().<Partners>consumer(DB_PARTNER_CREATE,
                msg -> execSqlRaw(msg, dsl -> dsl.insertInto(PARTNERS, PARTNERS.NAME, PARTNERS.DESCRIPTION, PARTNERS.IMAGE)
                        .values(/*msg.body().getId(),*/ msg.body().getName(), msg.body().getDescription(), msg.body().getImage())
                        .returning(PARTNERS.ID).fetchOne().getId()/*.into(Partners.class)*/));
        /* Users */
        this.vertx.eventBus().<Void>consumer(DB_USER_GET_ALL,
                                             msg -> execSqlCodec(msg, UsersListMessageCodec.class, dsl -> new UsersDao(dsl.configuration()).findAll()));
        this.vertx.eventBus().<Long>consumer(DB_USER_GET_BY_ID,
                                             msg -> execSqlRaw(msg, dsl -> new UsersDao(dsl.configuration()).fetchOneById(msg.body())));
        this.vertx.eventBus().<Long>consumer(DB_USER_DELETE_BY_ID,
                                             msg -> execSqlNoReturn(msg, dsl -> new UsersDao(dsl.configuration()).deleteById(msg.body())));
        this.vertx.eventBus().<Users>consumer(DB_USER_CREATE,
                msg -> execSqlRaw(msg, dsl -> dsl.insertInto(USERS, USERS.MAIL, USERS.ADMIN, USERS.IMAGE, USERS.CREATED, USERS.PASSWORD, USERS.RGPD_ACCEPTED)
                        .values(/*msg.body().getId(),*/ msg.body().getMail(),msg.body().getAdmin(), msg.body().getImage(), msg.body().getCreated(), msg.body().getPassword(), msg.body().getRgpdAccepted())
                        .returning().fetchOne().getId()/*.into(Users.class)*/));
        this.vertx.eventBus().<Long>consumer(DB_USER_GET_ARTICLES_CREATE,
                                             msg -> execSqlRaw(msg, dsl -> new ArticlesDataDao(dsl.configuration()).fetchByCreateby(msg.body())
                                                                                .parallelStream().mapToLong(ArticlesData::getId).toArray()));
        //select users.*, count(*) as favorites from users left join users_favorites on id=userId group by id
        this.vertx.eventBus().<Long>consumer(DB_USER_GET_INFOS,
                                             msg -> execSqlRaw(msg, dsl -> new JsonObject(dsl.select(USERS.asterisk(), DSL.count()/*.over()*/.as("favorites"))
                                                                                             .from(USERS).leftJoin(USERS_FAVORITES).on(USERS.ID.eq(USERS_FAVORITES.USERID))
                                                                                             .where(USERS.ID.eq(msg.body()))
                                                                                             .groupBy(USERS.ID)
                                                                                             .fetchOneMap())));
        this.vertx.eventBus().<Long>consumer(DB_USER_GET_ARTICLES_FAVORITES,
                                             msg -> execSqlRaw(msg, dsl -> dsl.select(ARTICLES.asterisk())
                                                                              .from(USERS_FAVORITES).leftJoin(ARTICLES).on(USERS_FAVORITES.ARTICLEID.eq(ARTICLES.ID))
                                                                              .where(USERS_FAVORITES.USERID.eq(msg.body()))
                                                                              .fetchInto(Articles.class)));
        this.vertx.eventBus().<JsonObject>consumer(DB_USER_DELETE_ARTICLE_FAVORITES,
                                                   msg -> execSqlNoReturn(msg, dsl -> new UsersFavoritesDao(dsl.configuration())
                                                           .deleteById(new UsersFavoritesRecord(msg.body().getLong("user"),
                                                                                                msg.body().getLong("article")))));
        this.vertx.eventBus().<JsonObject>consumer(DB_USER_ADD_ARTICLE_FAVORITES,
                                                   msg -> execSqlNoReturn(msg, dsl -> new UsersFavoritesDao(dsl.configuration())
                                                           .insert(new UsersFavorites(msg.body().getLong("user"),
                                                                                      msg.body().getLong("article")))));
        this.vertx.eventBus().<String>consumer(DB_USER_GET_BY_MAIL, msg -> execSqlRaw(msg, sdl -> new UsersDao(sdl.configuration()).fetchOneByMail(msg.body())));
        /* Ads */
        this.vertx.eventBus().<Void>consumer(DB_AD_GET_ALL,
                                             msg -> execSqlCodec(msg, AdsListMessageCodec.class, dsl -> new AdsDao(dsl.configuration()).findAll()));
        this.vertx.eventBus().<Long>consumer(DB_AD_GET_BY_ID,
                                             msg -> execSqlRaw(msg, dsl -> new AdsDao(dsl.configuration()).fetchOneById(msg.body())));
        this.vertx.eventBus().<Long>consumer(DB_AD_DELETE_BY_ID,
                                             msg -> execSqlNoReturn(msg, dsl -> new AdsDao(dsl.configuration()).deleteById(msg.body())));
        this.vertx.eventBus().<Ads>consumer(DB_AD_CREATE,
               msg -> execSqlRaw(msg, dsl -> dsl.insertInto(ADS, ADS.IMAGE, ADS.TITLE)
                       .values(/*msg.body().getId(),*/ msg.body().getImage(), msg.body().getTitle()).returning().fetchOne().getId()/*.into(Ads.class)*/));
        /* Images */
        this.vertx.eventBus().<Void>consumer(DB_IMAGE_GET_ALL,
                                             msg -> execSqlCodec(msg, ImagesListMessageCodec.class, dsl -> new ImagesDao(dsl.configuration()).findAll()));
        this.vertx.eventBus().<Long>consumer(DB_IMAGE_GET_BY_ID,
                                             msg -> execSqlRaw(msg, dsl -> new ImagesDao(dsl.configuration()).fetchOneById(msg.body())));
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

    private <M> void execSqlRaw(@NonNull final Message<M> msg, @NonNull final Function<DSLContext, ?> getter) {
        this.client.getConnection(result -> {
            if (result.succeeded()) {
                final Object tmp = getter.apply(this.mapDslContext(result.result().unwrap()));
                result.result().close();
                msg.reply(tmp);
            } else
                msg.fail(-1, result.cause().getMessage());
        });
    }

    private <M> void execSqlCodec(@NonNull final Message<M> msg, @NonNull final Class<? extends AbstractListMessageCodec> codec, @NonNull final Function<DSLContext, List<?>> getter) {
        this.client.getConnection(result -> {
            if (result.succeeded()) {
                final Object tmp = getter.apply(this.mapDslContext(result.result().unwrap()));
                result.result().close();
                DeliveryOptions ops = new DeliveryOptions(); ops.setCodecName(codec.getSimpleName());
                msg.reply(tmp, ops);
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
