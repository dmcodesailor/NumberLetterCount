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
public class NumberToWordsConverterHundredThousands extends AbstractVerticle {
    @Override
    public void start(){
        try {
            super.start();
            this.vertx.eventBus().consumer("toWordsHundredThousands", this::messageHandler);
        } catch (Exception ex) {
            System.out.println(ex.getMessage() + " while starting NumberToWordsConverterHundredThousands");
        }
    }
    
    public void messageHandler(Message message) {
        String numberAsString = message.body().toString();
        String thousandsPart = numberAsString.substring(6,9);
        int number  = Integer.parseInt(thousandsPart);
        System.out.println("NumberToWordsConverterHundredThousands::messageHandler::" + numberAsString + ".substring(6,9) -> " + number + "; Sending " + thousandsPart);
        this.vertx.eventBus().send("toWordsLessThanOneThousand", thousandsPart, ar -> {
            if (ar.succeeded()) {
                String result = "";
                switch (number) {
                    case 0:
                        result = "";
                        break;
                    case 1:
                        result = "one thousand";
                        break;
                    default:
                        result = ar.result().body().toString() + " thousand";
                        break;
                }
                message.reply( result);
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
            System.out.println(ex.getMessage() + " while stopping NumberToWordsConverterHundredThousands");
        }
    }
}
