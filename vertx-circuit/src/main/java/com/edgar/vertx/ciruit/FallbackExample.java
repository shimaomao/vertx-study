package com.edgar.vertx.ciruit;

import io.vertx.circuitbreaker.CircuitBreaker;
import io.vertx.circuitbreaker.CircuitBreakerOptions;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Launcher;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Edgar on 2016/7/1.
 *
 * @author Edgar  Date 2016/7/1
 */
public class FallbackExample extends AbstractVerticle {
  public static void main(String[] args) {

    new Launcher().execute("run", FallbackExample.class.getName());
  }

  @Override
  public void start() throws Exception {
    AtomicInteger seq = new AtomicInteger();
    vertx.createHttpServer().requestHandler(req -> {
      System.out.println(seq.incrementAndGet());
      req.response().setStatusCode(400).end();
    }).listen(8080);

    CircuitBreaker breaker = CircuitBreaker.create("my-circuit-breaker", vertx,
                                                   new CircuitBreakerOptions()
                                                           .setMaxFailures(5)
                                                           .setTimeout(1000)
                                                           .setResetTimeout(3000)
    ).fallback(t -> "HELLO")
            .openHandler(v -> {
              System.out.println("Circuit opened");
            }).closeHandler(v -> {
              System.out.println("Circuit closed");
            }).halfOpenHandler(v -> {
              System.out.println("reset (half-open state)");
            });

//        breaker.execute(future -> {
//            // some code executing with the breaker
//            // the code reports failures or success on the given future.
//            // if this future is marked as failed, the breaker increased the
//            // number of failures
//        }).setHandler(ar -> {
//            // Get the operation result.
//        });

    vertx.setPeriodic(1000, l -> {
      breaker.<String>execute(future -> {
        vertx.createHttpClient().getNow(8080, "localhost", "/", response -> {
          if (response.statusCode() != 200) {
            future.fail("HTTP error");
          } else {
            response
                    .exceptionHandler(future::fail)
                    .bodyHandler(buffer -> {
                      future.complete(buffer.toString());
                    });
          }
        });
      }).setHandler(ar -> {
        if (ar.succeeded()) {
          System.out.println(ar.result());
        } else {
          System.out.println(ar.cause());
        }
      });
    });
  }
}
