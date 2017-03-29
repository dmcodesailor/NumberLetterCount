/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dirtmakers.numberlettercount;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.eventbus.Message;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
/**
 *
 * @author brianlanham
 */
public class NumberToWordsVerticle extends AbstractVerticle {
    @Override
    public void start(){
        try {
            super.start();
            vertx.eventBus().consumer("toWords", this::eventBusRequestHandler);
        } catch (Exception ex) {
            System.out.println(ex.getMessage() + " while starting NumberToWordsConverterMillions");
        }
    }
    
    void eventBusRequestHandler(Message message) {
        long number = Long.parseLong(message.body().toString());
        
        // Determine how long the number is.  But don't.  Instead fix the number
        // format by padding with zeroes.  
        String mask = "000000000000";
        String numberAsString = Long.toString(number);
        DecimalFormat decimalFormat = new DecimalFormat(mask);
        numberAsString = decimalFormat.format(number);
        
        // Using the formatted value, post on the event bus for processing.
        // Use Futures to distribute the workload.
        List<Future> futures = new ArrayList<Future>();
        futures.add(this.processNumber(numberAsString, "toWordsMillions"));
        futures.add(this.processNumber(numberAsString, "toWordsHundredThousands"));
        futures.add(this.processNumber(numberAsString, "toWordsThousands"));
//        futures.add(this.processNumber(numberAsString, "toWordsLessThanOneThousand"));
    
        // Join the futures.
        // *** Assume the sequence of adding the futures is the same as the
        // result sequence for generating the result. ***
        // http://www.rgagnon.com/javadetails/java-0426.html
        CompositeFuture.join(futures).setHandler(ar -> {
            if (ar.succeeded()) {
//                String numberAsWords = ar.result().resultAt(3).toString();
                String numberAsWords = " " + ar.result().resultAt(0).toString();
                numberAsWords += " " + ar.result().resultAt(1).toString();
                numberAsWords += " " + ar.result().resultAt(2).toString();
                // Strip extra spaces.
                numberAsWords = numberAsWords.replaceAll("^\\s+", "").replaceAll("\\b\\s{2,}\\b", " ");
                message.reply(numberAsWords);
            } else {
                message.reply(ar.cause().getMessage());
            }
        });
        
    }
    
    private Future<String> processNumber(String number, String channel) {
        Future<String> resultFuture = Future.future();
        this.vertx.eventBus().send(channel, number, res -> {
            if (res.succeeded()) {
                resultFuture.complete(res.result().body().toString());
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
        } catch (Exception ex) {
            System.out.println(ex.getMessage() + " while stopping NumberToWordsConverterMillions");
        }
    }
}
