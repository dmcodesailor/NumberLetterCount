/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dirtmakers.numberlettercount;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.Json;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author brianlanham
 */
public class RangeWorker extends AbstractVerticle {
        
    @Override
    public void start() {
        try {
            super.start();
            this.vertx.eventBus().consumer("range", this::messageHandler);
        }
        catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }
    
    public void messageHandler(Message message) {
        String target = message.body().toString().trim();
        io.vertx.core.json.JsonObject range = new io.vertx.core.json.JsonObject(target);
        System.out.println(range);
        List<Future> futures = new ArrayList<>();
        for (int i = range.getInteger("fromNumber"); i <= range.getInteger("toNumber"); i++) {
            Future<String> resultFuture = Future.future();
            String numberAsString = String.valueOf(i);
            this.vertx.eventBus().send("toWords", numberAsString, res -> {
                if (res.succeeded()) {
                    resultFuture.complete(res.result().body().toString());
                } else {
                    resultFuture.fail(res.cause().getMessage());
                }
            });
            futures.add(resultFuture);
        }
        List<String> results = new ArrayList<>();
        CompositeFuture.join(futures).setHandler((AsyncResult<CompositeFuture> ar) -> {
            if (ar.succeeded()) {
                for(int i=0; i < futures.size(); i++) {
                    String phrase = ar.result().resultAt(i).toString();
                    results.add(phrase);
                }
                String json = Json.encode(results);
                message.reply(json);
            } else {
                results.add(ar.cause().getMessage());
            }
        });
    }

    @Override
    public void stop() {
        try {
            super.stop();
        }
        catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }
}
