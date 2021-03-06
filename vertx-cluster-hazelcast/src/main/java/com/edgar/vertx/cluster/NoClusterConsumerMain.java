package com.edgar.vertx.cluster;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;

/**
 * Created by Edgar on 2016/3/11.
 *
 * @author Edgar  Date 2016/3/11
 */
public class NoClusterConsumerMain {
  public static void main(String[] args) {
    Vertx.vertx().deployVerticle(new AbstractVerticle() {
      @Override
      public void start() throws Exception {
        vertx.eventBus().consumer("nocluster", msg -> {
          System.out.println(msg.body());
        });
      }
    });
  }
}
