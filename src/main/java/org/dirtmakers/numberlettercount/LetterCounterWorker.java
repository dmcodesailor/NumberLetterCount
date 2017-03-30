/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dirtmakers.numberlettercount;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.Json;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author brianlanham
 */
public class LetterCounterWorker extends AbstractVerticle {
    
    @Override
    public void start() {
        try {
            super.start();
            this.vertx.eventBus().consumer("countLettersWord", this::messageHandler);
        }
        catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }
    
    public void messageHandler(Message message) {
        String target = message.body().toString().trim();
        Map<java.lang.Character, Integer> count = new HashMap<java.lang.Character, Integer>();
        if (target.length() > 0) {
            for (char letter : target.toCharArray()) {
                int curLetterCount = 0;
                if (count.containsKey(letter)) {
                    curLetterCount = count.get(letter);
                }
                count.put(letter, ++curLetterCount);
            }
        }
        String json = Json.encode(count);
        message.reply(json);
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
