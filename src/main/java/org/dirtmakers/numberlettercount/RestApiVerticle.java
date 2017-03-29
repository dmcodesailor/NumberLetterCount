/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dirtmakers.numberlettercount;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.eventbus.Message;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;

/**
 *
 * @author brianlanham
 */
public class RestApiVerticle extends AbstractVerticle {
    
    private String baseRoute;
    private Router parentRouter;
    private int apiPort;

    RestApiVerticle(Router parentRouter, String baseRoute, int restApiPort) {
        this.parentRouter = parentRouter;
        this.baseRoute = baseRoute;
        this.apiPort = restApiPort;
    }
    
    @Override
    public void start() {
        try {
            super.start();
            
            // Build the REST API Web router object.
            Router router = getRouter();

            // Three (3) endpoints:
            // a  ) number-to-word converter:  Provide a single number and have it converted to words
            // 2  ) letter counter:  Using the provided words for one or more numbers, count the letters
            // https://projecteuler.net/problem=17
            // III) range computer:  Given a range of integers convert to words and count the letters
            
            // Option 'a'...
            router.get("/numtowords/:num").handler(this::numberToWordHandler);

            // Option '2'...
            router.post("/lettercount/").handler(this::letterCountHandler);
            
            // Option 'III'...
            router.get("/range/*").handler(this::rangeHandler);
            
            // Invoke an HTTP server to handle requests.
            HttpServer httpServer = vertx.createHttpServer();
            httpServer.requestHandler(router::accept).listen(this.apiPort);
            System.out.println("REST Server Started at " +this.apiPort);

        } catch (Exception ex) {
            System.out.println(ex.getMessage() + " while starting RestApiVerticle");
        }
    }
    
    private CorsHandler getCorsHandler() {
        CorsHandler corsHandler = CorsHandler.create ("*")
            .allowedMethod (HttpMethod.GET)
            .allowedMethod (HttpMethod.POST)
            .allowedMethod (HttpMethod.OPTIONS)
            .allowedHeader("content-type");
        return corsHandler;
    }
    
    private Router getRouter() {
        Router router = Router.router(vertx);
        router.route().handler(getCorsHandler());
        router.route().handler(BodyHandler.create());
        return router;
    }

    private void numberToWordHandler(RoutingContext context) {
        HttpServerResponse response = context.response();
        
        // Extract the number from the GET request.
        long num = Long.parseLong(context.request().getParam("num"));

        // Send the number to a worker verticle for converting to words.
        vertx.eventBus().send("toWords", num, (AsyncResult<Message<Object>> result) -> {
           if (result.succeeded()) {
               String responseMessage = result.result().body().toString();
               response.setStatusCode(200).end(responseMessage);
           } else {
               response.setStatusCode(500).end(result.cause().getMessage());
           }
        });
    }
    
    private void letterCountHandler(RoutingContext context) {
        HttpServerResponse response = context.response();
        
        // Extract the number from the POST request.
        String words = context.getBodyAsString();

        // Send the entire string to a worker verticle for processing.
        // The target worker verticle will split the string on spaces and in turn
        // pass individual words to other worker verticles for processing.
        vertx.eventBus().send("countLetters", words, (AsyncResult<Message<Object>> result) -> {
           if (result.succeeded()) {
               String responseMessage = "<p>" + result.result().body() + "</p>";
               response.setStatusCode(200).end(responseMessage);
           } else {
               response.setStatusCode(500).end(result.cause().getMessage());
           }
        });
    }
    
    private void rangeHandler(RoutingContext context) {
        HttpServerResponse response = context.response();
        
        // Extract the "from" and "to" parameters from the GET request.
        int fromNum = Integer.parseInt(context.request().getParam("from"));
        int toNum = Integer.parseInt(context.request().getParam("to"));
        
        JsonObject rangeObject = new io.vertx.core.json.JsonObject();
        rangeObject.put("fromNumber", fromNum);
        rangeObject.put("toNumber", toNum);
        
        // Send the range bookends to a worker verticle for processing.
        // The target worker verticle will generate the intermediary values and in turn
        // pass individual numbers to other worker verticles for processing.
        vertx.eventBus().send("range", rangeObject, (AsyncResult<Message<Object>> result) -> {
           if (result.succeeded()) {
               String responseMessage = "<p>" + result.result().body() + "</p>";
               response.setStatusCode(200).end(responseMessage);
           } else {
               response.setStatusCode(500).end(result.cause().getMessage());
           }
        });        
    }

    @Override
    public void stop() {
        try {
            super.stop();
        } catch (Exception ex) {
            System.out.println(ex.getMessage() + " while stopping RestApiVerticle");
        }
    }
    
}
