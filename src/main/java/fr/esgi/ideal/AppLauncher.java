package fr.esgi.ideal;

import io.vertx.core.Launcher;
import io.vertx.core.VertxOptions;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.core.logging.SLF4JLogDelegateFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import static io.vertx.core.logging.LoggerFactory.LOGGER_DELEGATE_FACTORY_CLASS_NAME;

public class AppLauncher extends Launcher {
    static {
        System.setProperty("java.util.logging.config.file", "logging.properties");
        System.setProperty(LOGGER_DELEGATE_FACTORY_CLASS_NAME, SLF4JLogDelegateFactory.class.getName());
        //System.setProperty("vertx.logger-delegate-factory-class-name", "io.vertx.core.logging.SLF4JLogDelegateFactory");
        //LoggerFactory.getLogger(LoggerFactory.class); // Required for Logback to work in Vertx
    }
    static final Logger logger = LoggerFactory.getLogger("io.vertx.core.logging.SLF4JLogDelegateFactory");

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
        super.beforeStartingVertx(options);
    }
}
