package fr.esgi.ideal;

import io.vertx.core.Vertx;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import lombok.NonNull;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class MyVerticleTest {
    private Vertx vertx;

    @Before
    public void setUp(@NonNull final TestContext context) {
        this.vertx = Vertx.vertx();
        this.vertx.deployVerticle(MyVerticle.class.getName(), context.asyncAssertSuccess());
    }

    @After
    public  void tearDown(@NonNull final TestContext context) {
        this.vertx.close(context.asyncAssertSuccess());
    }

    @Test
    public void testApp(@NonNull final TestContext context) {
        final Async async = context.async();
        this.vertx.createHttpClient().getNow(8080, "localhost", "/", resp -> resp.handler(body -> {
            context.assertTrue(body.toString().contains("Hello World !"));
            async.complete();
        }));
    }
}
