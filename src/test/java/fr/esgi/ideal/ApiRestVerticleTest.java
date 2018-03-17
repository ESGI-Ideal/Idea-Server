package fr.esgi.ideal;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import lombok.NonNull;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.net.ServerSocket;

@RunWith(VertxUnitRunner.class)
public class ApiRestVerticleTest {
    private /*final static*/ int port = 8081;
    private Vertx vertx;

    @Before
    public void setUp(@NonNull final TestContext context) throws IOException {
        this.vertx = Vertx.vertx(new VertxOptions().setBlockedThreadCheckInterval(200000000));
        try(final ServerSocket socket = new ServerSocket(0)) {
            this.port = socket.getLocalPort();
        }
        this.vertx.deployVerticle(ApiRestVerticle.class.getName(),
                                  new DeploymentOptions().setConfig(new JsonObject().put("http.port", port)),
                                  context.asyncAssertSuccess());
    }

    @After
    public void tearDown(@NonNull final TestContext context) {
        this.vertx.close(context.asyncAssertSuccess());
    }

    /**
     * Rigourous Test :-)
     *
     * @param context vertx's test context
     */
    @Test
    public void testApp(@NonNull final TestContext context) {
        final Async async = context.async();
        this.vertx.createHttpClient().getNow(port, "localhost", "/", resp -> resp.handler(body -> {
            context.assertTrue(body.toString().contains("Hello World !"));
            async.complete();
        }));
    }
}
