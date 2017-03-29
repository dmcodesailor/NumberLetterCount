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
public class NumberToWordsConverterThousands extends AbstractVerticle {
    @Override
    public void start(){
        try {
            super.start();
            this.vertx.eventBus().consumer("toWordsThousands", this::messageHandler);
        } catch (Exception ex) {
            System.out.println(ex.getMessage() + " while starting NumberToWordsConverterThousands");
        }
    }
    
    public void messageHandler(Message message) {
        String numberAsString = message.body().toString();
        String hundredsPart = numberAsString.substring(9,12);
        int number = Integer.parseInt(hundredsPart);
        System.out.println("NumberToWordsConverterThousands::messageHandler::" + numberAsString + ".substring(9,12) -> " + number + "; Sending " + hundredsPart);
        this.vertx.eventBus().send("toWordsLessThanOneThousand", hundredsPart, ar -> {
           if (ar.succeeded()) {
               message.reply(ar.result().body().toString());
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
            System.out.println(ex.getMessage() + " while stopping NumberToWordsConverterThousands");
        }
    }
}
