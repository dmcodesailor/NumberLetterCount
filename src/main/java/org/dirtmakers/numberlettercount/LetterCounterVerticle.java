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
import java.util.Map.Entry;

/**
 *
 * @author brianlanham
 */
public class LetterCounterVerticle extends AbstractVerticle {
        
    @Override
    public void start() {
        try {
            super.start();
            this.vertx.eventBus().consumer("countLetters", this::messageHandler);
        }
        catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }
    
    public void messageHandler(Message message) {
        String phrase = message.body().toString().trim();
        List<Future> futures = new ArrayList<Future>();
        for (String word : phrase.split(" ")) {
            futures.add(this.processWord(word));
        }
        Map<java.lang.Character, Integer> count = new HashMap<java.lang.Character, Integer>();
        CompositeFuture.join(futures).setHandler((AsyncResult<CompositeFuture> ar) -> {
            if (ar.succeeded()) {
                for(int i=0; i < futures.size(); i++) {
                    HashMap<java.lang.Character, Integer> wordCounts = (HashMap<java.lang.Character, Integer>)futures.get(i).result();
                    for (Entry<java.lang.Character, Integer> entry : wordCounts.entrySet()) {
                        int curCount = 0;
                        if (count.containsKey(entry.getKey())) {
                            curCount = count.get(entry.getKey());
                        }
                        curCount += entry.getValue();
                        count.put(entry.getKey(), curCount);
                    }
                }
            } else {
                
            }
        });
        String json = Json.encode(count);
        message.reply(json);
    }
    
    private Future<Map<java.lang.Character, Integer>> processWord(String word) {
        Future<Map<java.lang.Character, Integer>> resultFuture = Future.future();
        this.vertx.eventBus().send("countLettersWord", word, res -> {
            if (res.succeeded()) {
//                Map<java.lang.Character, Integer> response = Json.decodeValue(res.result().body().toString(), Map<java.lang.Character, Integer>);
                resultFuture.complete();
            } else {
                resultFuture.fail(res.cause().getMessage());
            }
        });
        return resultFuture;
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
