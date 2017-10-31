package workshop.playgroud;

import static workshop.shared.Constants.DATAGRID_HOST;
import static workshop.shared.Constants.DATAGRID_PORT;

import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;

public class PlayWithTheCache extends AbstractVerticle {

  protected RemoteCacheManager client;
  protected RemoteCache<Integer, String> defaultCache;

  @Override
  public void init(Vertx vertx, Context context) {
    super.init(vertx, context);
    initCache(vertx);
  }

  protected void initCache(Vertx vertx) {
    vertx.executeBlocking(fut -> {
      client = new RemoteCacheManager(
        new ConfigurationBuilder().addServer()
          .host(DATAGRID_HOST)
          .port(DATAGRID_PORT)
          .build());

      defaultCache = client.getCache();
      fut.complete();
    }, res -> {
      if (res.succeeded()) {
        System.out.println("Cache started");
      } else {
        res.cause().printStackTrace();
      }
    });
  }

  @Override
  public void start() throws Exception {
    Router router = Router.router(vertx);

    router.get("/").handler(rc -> {
      rc.response().end("Welcome");
    });

    router.get("/api").handler(rc -> {
      rc.response().end(new JsonObject().put("name", "duchess").put("version", 1).encode());
    });

    router.put("/api/duchess").handler(rc -> {
      JsonObject bodyAsJson = rc.getBodyAsJson();
      if (bodyAsJson.containsKey("id") && bodyAsJson.containsKey("name")) {
        defaultCache.putAsync(bodyAsJson.getInteger("id"), bodyAsJson.getString("name"))
          .thenAccept(s -> {
            rc.response().end("Duchess Added");
          });
      }
    });

    router.get("/api/duchess/:id").handler(rc -> {
      defaultCache.getAsync(Integer.parseInt(rc.request().getParam("id")))
        .thenAccept(value -> {
          String response = "Not found";
          if (value != null) {
            response = new JsonObject().put("Duchess Number 1", value).encode();
          }
          rc.response().end(response);
        });
    });

    vertx.createHttpServer()
      .requestHandler(router::accept)
      .listen(8080);
  }

  @Override
  public void stop(Future<Void> stopFuture) throws Exception {
    if (client != null) {
      client.stopAsync().whenComplete((e, ex) -> stopFuture.complete());
    }
  }
}
