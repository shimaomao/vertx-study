package com.edgar.vertx.unit;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunnerWithParametersFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;

@RunWith(Parameterized.class)
@Parameterized.UseParametersRunnerFactory(VertxUnitRunnerWithParametersFactory.class)
public class ParameterizedTest {
  /**
   * @return the test ports
   */
  @Parameterized.Parameters
  public static Iterable<Integer> ports() {
    return Arrays.asList(8080, 8081, 8082);
  }

  private final int port;

  private Vertx vertx;

  public ParameterizedTest(int port) {
    this.port = port;
  }

  @Before
  public void before() {
    vertx = Vertx.vertx();
  }

  @Test
  public void test(TestContext context) {
    System.out.println(port);
    HttpServer server = vertx.createHttpServer().requestHandler(req -> {
      context.assertEquals(port, req.localAddress().port());
      req.response().end();
    });
    server.listen(port, "localhost", context.asyncAssertSuccess(s -> {
      HttpClient client = vertx.createHttpClient();
      Async async = context.async();
      HttpClientRequest req = client.get(port, "localhost", "/", resp -> {
        context.assertEquals(200, resp.statusCode());
        async.complete();
      });
      req.exceptionHandler(context::fail);
      req.end();
    }));
  }

  @After
  public void after(TestContext context) {
    vertx.close(context.asyncAssertSuccess());
  }
}