package fr.esgi.ideal;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Launcher;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.VertxOptionsJson;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.core.logging.SLF4JLogDelegateFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.util.Optional;

import static io.vertx.core.logging.LoggerFactory.LOGGER_DELEGATE_FACTORY_CLASS_NAME;

public class AppLauncher extends Launcher {
    static {
        System.setProperty("java.util.logging.config.file", "logging.properties");
        System.setProperty(LOGGER_DELEGATE_FACTORY_CLASS_NAME, SLF4JLogDelegateFactory.class.getName());
        //System.setProperty("vertx.logger-delegate-factory-class-name", "io.vertx.core.logging.SLF4JLogDelegateFactory");
        //LoggerFactory.getLogger(LoggerFactory.class); // Required for Logback to work in Vertx
    }
    //static final Logger logger = LoggerFactory.getLogger("io.vertx.core.logging.SLF4JLogDelegateFactory");
    static final Logger mainLogger = LoggerFactory.getLogger(io.vertx.core.logging.SLF4JLogDelegateFactory.class);

    private static Logger logger = null;

    /**
     * Main entry point.
     *
     * @param args the user command line arguments.
     */
    public static void main(final String... args) {
        new AppLauncher().dispatch(args);
    }

    /**
     * Utility method to execute a specific command.
     *
     * @param cmd  the command
     * @param args the arguments
     */
    public static void executeCommand(final String cmd, final String... args) {
        new AppLauncher().execute(cmd, args);
    }

    /**
     * Hook for sub-classes of {@link Launcher} after the config has been parsed.
     *
     * @param config the read config, empty if none are provided.
     */
    @Override
    public void afterConfigParsed(final JsonObject config) {
        logger = LoggerFactory.getLogger(AppLauncher.class);
        logger.trace("afterConfigParsed : config={}", config.encodePrettily());
        this.config = config;
        logger.trace("Loading default config and override with custom config ...");
        logger.trace("default config file at {}", this.getClass().getClassLoader().getResource("api-conf.json"));
        try(final InputStream file = this.getClass().getClassLoader().getResourceAsStream("api-conf.json")) {
            final JsonObject def_conf = new JsonObject(Buffer.buffer(Utils.getStringFromInputStream(file)));
            config.mergeIn(def_conf.mergeIn(config, true), 1);
        } catch(final IOException e) {
            throw new RuntimeException(e);
        }
        super.afterConfigParsed(config);
    }

    private JsonObject config;

    /**
     * Hook for sub-classes of {@link Launcher} before the vertx instance is started.
     *
     * @param options the configured Vert.x options. Modify them to customize the Vert.x instance.
     */
    @Override
    public void beforeStartingVertx(final VertxOptions options) {
        System.setProperty(LOGGER_DELEGATE_FACTORY_CLASS_NAME, SLF4JLogDelegateFactory.class.getName());
        SLF4JBridgeHandler.removeHandlersForRootLogger(); // Optionally remove existing handlers attached to j.u.l root logger
        SLF4JBridgeHandler.install(); // add SLF4JBridgeHandler to j.u.l's root logger, should be done once during the initialization phase of your application
        //InternalLoggerFactory.setDefaultFactory(Slf4JLoggerFactory.INSTANCE); // Force Netty to use Slf4j
        this.lookups.forEach(cfl -> logger.trace("CommandFactoryLookup : {}", cfl));
        logger.trace("beforeStartingVertx : options={}", options);
        if(config.containsKey("VertxOptions")) {
            VertxOptionsJson.fromJson(config.getJsonObject("VertxOptions"/*, new JsonObject()*/), options);
            logger.debug("New VertxOptions : {}", options);
        }
        super.beforeStartingVertx(options);
    }

    /**
     * Hook for sub-classes of {@link Launcher} after the vertx instance is started.
     *
     * @param vertx the created Vert.x instance
     */
    @Override
    public void afterStartingVertx(final Vertx vertx) {
        logger.trace("afterStartingVertx : vertx={}", vertx);
        super.afterStartingVertx(vertx);
    }

    /**
     * Hook for sub-classes of {@link Launcher} before the verticle is deployed.
     *
     * @param deploymentOptions the current deployment options. Modify them to customize the deployment.
     */
    @Override
    public void beforeDeployingVerticle(final DeploymentOptions deploymentOptions) {
        logger.trace("beforeDeployingVerticle : deploymentOptions={}", deploymentOptions);
        super.beforeDeployingVerticle(deploymentOptions);
    }

    @Override
    public void beforeStoppingVertx(final Vertx vertx) {
        logger.trace("beforeStoppingVertx : vertx={}", vertx);
        super.beforeStoppingVertx(vertx);
    }

    @Override
    public void afterStoppingVertx() {
        logger.trace("afterStoppingVertx");
        super.afterStoppingVertx();
    }

    /**
     * A deployment failure has been encountered. You can override this method to customize the behavior.
     * By default it closes the `vertx` instance.
     *
     * @param vertx             the vert.x instance
     * @param mainVerticle      the verticle
     * @param deploymentOptions the verticle deployment options
     * @param cause             the cause of the failure
     */
    @Override
    public void handleDeployFailed(final Vertx vertx, final String mainVerticle, final DeploymentOptions deploymentOptions, final Throwable cause) {
        logger.warn("handleDeployFailed : mainVerticle={} ; deploymentOptions={} ; cause={}", mainVerticle, deploymentOptions, cause);
        super.handleDeployFailed(vertx, mainVerticle, deploymentOptions, cause);
    }
}
