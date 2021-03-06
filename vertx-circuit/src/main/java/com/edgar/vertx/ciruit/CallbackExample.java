package com.edgar.vertx.ciruit;

import io.vertx.circuitbreaker.CircuitBreaker;
import io.vertx.circuitbreaker.CircuitBreakerOptions;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Launcher;

/**
 * Created by Edgar on 2016/7/1.
 *
 * @author Edgar  Date 2016/7/1
 */
public class CallbackExample extends AbstractVerticle {
  public static void main(String[] args) {

    new Launcher().execute("run", CallbackExample.class.getName());
  }

  @Override
  public void start() throws Exception {

    vertx.createHttpServer().requestHandler(req -> {
      req.response().setStatusCode(400).end();
    }).listen(8080);

    CircuitBreaker breaker = CircuitBreaker.create("my-circuit-breaker", vertx,
                                                   new CircuitBreakerOptions()
                                                           .setMaxFailures(
                                                                   5) // number of failure before
                                                           // opening the circuit
                                                           .setTimeout(
                                                                   1000) // consider a failure if
                                                           // the operation does not succeed in time
                                                           .setResetTimeout(3000)
                                                   // time spent in open state before attempting
                                                   // to re-try
    )
            .openHandler(v -> {
              System.out.println("Circuit opened");
            }).closeHandler(v -> {
              System.out.println("Circuit closed");
            }).halfOpenHandler(v -> {
              System.out.println("reset (half-open state)");
            });
    ;

//        breaker.execute(future -> {
//            // some code executing with the breaker
//            // the code reports failures or success on the given future.
//            // if this future is marked as failed, the breaker increased the
//            // number of failures
//        }).setHandler(ar -> {
//            // Get the operation result.
//        });

    vertx.setPeriodic(1000, l -> {
      breaker.<String>executeWithFallback(future -> {
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
      }, r -> "Hello").setHandler(ar -> {
        if (ar.succeeded()) {
          System.out.println(ar.result());
        } else {
          System.out.println(ar.cause());
        }
      });
    });

  }
}
