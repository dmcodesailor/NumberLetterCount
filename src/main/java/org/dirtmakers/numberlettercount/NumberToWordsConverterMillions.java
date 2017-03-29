/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dirtmakers.numberlettercount;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.Message;

/**
 *
 * @author brianlanham
 */
public class NumberToWordsConverterMillions extends AbstractVerticle {
    @Override
    public void start(){
        try {
            super.start();
            this.vertx.eventBus().consumer("toWordsMillions", this::messageHandler);
        } catch (Exception ex) {
            System.out.println(ex.getMessage() + " while starting NumberToWordsConverterMillions");
        }
    }
    
    public void messageHandler(Message message) {
        String numberAsString = message.body().toString();
        String millionsPart = numberAsString.substring(3,6);
        int number = Integer.parseInt(millionsPart);
        System.out.println("NumberToWordsConverterMillions::messageHandler::" + numberAsString + ".substring(3,6) -> " + number + "; Sending " + millionsPart);        
        this.vertx.eventBus().send("toWordsLessThanOneThousand", millionsPart, ar -> {
            if (ar.succeeded()) {
                String millionsWord = "";
                if (number != 0) {
                    millionsWord = ar.result().body().toString() + " million";
                }
                message.reply(millionsWord);
            } else {
                message.fail(0, ar.cause().getMessage());
            }
        });        
    }
    
    @Override
    public void stop() {
        try {
            super.stop();
        } catch (Exception ex) {
            System.out.println(ex.getMessage() + " while stopping NumberToWordsConverterMillions");
        }
    }
}
