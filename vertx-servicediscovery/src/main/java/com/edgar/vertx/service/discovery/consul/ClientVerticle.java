package com.edgar.vertx.service.discovery.consul;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Launcher;
import io.vertx.core.json.JsonObject;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.consul.ConsulServiceImporter;

import java.util.Date;
import java.util.List;

/**
 * Created by edgar on 16-6-29.
 */
public class ClientVerticle extends AbstractVerticle {

    public static void main(String[] args) {

        new Launcher().execute("run", ClientVerticle.class.getName());
    }

    @Override
    public void start() throws Exception {
        ServiceDiscovery discovery = ServiceDiscovery.create(vertx)
                .registerServiceImporter(new ConsulServiceImporter(), new JsonObject()
                        .put("host", "10.11.0.31")
                        .put("port", 8500)
                        .put("scan-period", 2000));

//       vertx.setTimer(1000, l -> {
//           discovery.getRecords(r -> true, ar -> {
//               if (ar.succeeded()) {
//                   List<Record> records = ar.result();
//                   for (Record record : records) {
//                       System.out.println(record.getType());
//                       System.out.println(record.getLocation());
//                       System.out.println(record.getMetadata());
//                   }
//               }
//           });
//       });

        vertx.setPeriodic(2000, l -> {
            discovery.getRecords(r -> "db".equals(r.getMetadata().getString("ServiceName")), ar -> {
                if (ar.succeeded()) {
                    List<Record> records = ar.result();
                    System.out.println(new Date() + " records:" + records);
                    for (Record record : records) {
                        System.out.println(record.getType());
                        System.out.println(record.getLocation());
                        System.out.println(record.getMetadata());
                    }
                }
                System.out.println("--------------------");
            });
        });

//        discovery.close();
    }

}
