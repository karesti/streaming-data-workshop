package workshop.playground;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.concurrent.CompletableFuture;

import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import workshop.playgroud.PlayWithTheCache;

@RunWith(VertxUnitRunner.class)
public class PlayWithTheCacheTest {

  Vertx vertx;

  @Before
  public void before(TestContext context) {
    vertx = Vertx.vertx();
  }

  @After
  public void after(TestContext context) {
    vertx.close(context.asyncAssertSuccess());
  }

  @Test
  public void testDeployAndUndeploy(TestContext context) {
    // Deploy and undeploy a verticle
    vertx.deployVerticle(PlayWithCacheMockedRemoteCalls.class.getName(), context.asyncAssertSuccess(deploymentID ->
      vertx.undeploy(deploymentID, context.asyncAssertSuccess())));
  }

  @Test
  public void testPutAndGet(TestContext context) {
    HttpClient client = vertx.createHttpClient();
    Async async = context.async();
    vertx.deployVerticle(PlayWithCacheMockedRemoteCalls.class.getName());

    client.getNow(8080, "localhost", "/", resp -> {
      resp.bodyHandler(body -> {
        context.assertEquals("Welcome", body.toString());
        client.close();
        async.complete();
      });
    });
  }
}
