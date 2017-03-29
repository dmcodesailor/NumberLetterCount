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
public class NumberToWordsConverterLessThanOneThousand extends AbstractVerticle {
    
    private static final String[] tensNames = {
        "",
        " ten",
        " twenty",
        " thirty",
        " forty",
        " fifty",
        " sixty",
        " seventy",
        " eighty",
        " ninety"
    };

    private static final String[] numberNames = {
        "",
        " one",
        " two",
        " three",
        " four",
        " five",
        " six",
        " seven",
        " eight",
        " nine",
        " ten",
        " eleven",
        " twelve",
        " thirteen",
        " fourteen",
        " fifteen",
        " sixteen",
        " seventeen",
        " eighteen",
        " nineteen"
    };

    @Override
    public void start(){
        try {
            super.start();
            this.vertx.eventBus().consumer("toWordsLessThanOneThousand", this::messageHandler);
        } catch (Exception ex) {
            System.out.println(ex.getMessage() + " while starting NumberToWordsConverterThousands");
        }
    }
    
    public void messageHandler(Message message) {
        String numberAsString = message.body().toString();
        int number = Integer.parseInt(numberAsString);
        String numberAsWords = "";
        
        System.out.println("NumberToWordsConverterLessThanOneThousand::messageHandler::" + numberAsString + " -> " + number);
        
        if (number % 100 < 20){
            numberAsWords = numberNames[number % 100];
            number /= 100;
        } else {
            numberAsWords = numberNames[number % 10];
            number /= 10;
            
            numberAsWords = tensNames[number % 10] + numberAsWords;
            number /= 10;
        }
        
        if (number != 0) {
            numberAsWords = numberNames[number] + " hundred " + numberAsWords;
        }
        
        message.reply(numberAsWords);
    
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
