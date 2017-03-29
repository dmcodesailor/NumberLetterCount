/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dirtmakers.numberlettercount;

import com.hazelcast.config.Config;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Verticle;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.ext.web.Router;
import io.vertx.spi.cluster.hazelcast.HazelcastClusterManager;

import java.util.ArrayList;

/**
 *
 * @author brianlanham
 */
public class BootstrapVerticle extends AbstractVerticle {
    
    io.vertx.core.Vertx vertx = io.vertx.core.Vertx.vertx();
    ArrayList<Verticle> getChildStandardVerticles(Router router, String baseRoute) {
        ArrayList<Verticle> result = new ArrayList<Verticle>();
        result.add(new org.dirtmakers.numberlettercount.RestApiVerticle(router, baseRoute, 8080));
        return result;
    }
    ArrayList<Verticle> getChildWorkerVerticles(String baseRoute) {
        ArrayList<Verticle> result = new ArrayList<Verticle>();
        result.add(new org.dirtmakers.numberlettercount.NumberToWordsVerticle());
        result.add(new org.dirtmakers.numberlettercount.NumberToWordsConverterMillions());
        result.add(new org.dirtmakers.numberlettercount.NumberToWordsConverterHundredThousands());
        result.add(new org.dirtmakers.numberlettercount.NumberToWordsConverterThousands());
        result.add(new org.dirtmakers.numberlettercount.NumberToWordsConverterLessThanOneThousand());
        return result;
    }

    public static void main(String[] args) {
        DeploymentOptions deployOptions = new io.vertx.core.DeploymentOptions();
        JsonObject vertxConfig = new io.vertx.core.json.JsonObject();
        vertxConfig.put("VERTX_BASE_ROUTE", "/numletcnt/");
        vertxConfig.put("REST_API_PORT", 80);
        vertxConfig.put("REST_API_BASE_URL", "/numletcnt/api/");
        deployOptions.setConfig(vertxConfig);
        String verticleName = BootstrapVerticle.class.getClass().getCanonicalName();
        BootstrapVerticle verticleInstance = new BootstrapVerticle();
        deployModuleVerticle(verticleName, verticleInstance, deployOptions);
    }
    
    BootstrapVerticle(){
        super();
    }
    
    @Override
    public void start() {
        this.deployChildVerticles("NumberLetterCounter");
    }
    
    @Override
    public void stop() { 
        
    }
    
    protected static void deployModuleVerticle (String moduleName, Verticle verticleInstance, DeploymentOptions options) {
        System.console().printf("BootstrapVerticle::deployModuleVerticle({0}, {1}, {2})", moduleName, verticleInstance.getClass().getName(), options.toJson());
        Vertx.vertx ().deployVerticle (verticleInstance, options,
                (AsyncResult<String> deployResult) -> {
                    if (deployResult.succeeded ()) {
                        System.out.println(moduleName + " module verticle deployment successful. Deployment id is: " + deployResult.result ());
                    } else {
                        deployResult.cause ().printStackTrace ();
                    }
                }
        );
    }

    protected void deployChildVerticles(String moduleName) {
        DeploymentOptions options = new DeploymentOptions ();
        JsonObject config = this.config ();
        options.setConfig (config);

        Config hazelcastConfig = new Config();
        ClusterManager myClusterManager = new HazelcastClusterManager (hazelcastConfig);

        Vertx.clusteredVertx (new VertxOptions ().setClusterManager(myClusterManager),result->{
            if (result.succeeded ())
            {
                HttpServer httpServer = vertx.createHttpServer ();
                String baseRoute = config ().getString ("REST_API_BASE_URL");
                int port = config ().getInteger ("REST_API_PORT");

                //deploy worker verticles first
                ArrayList<Verticle> verticlesToDeploy = getChildWorkerVerticles (baseRoute);
                for (Verticle child : verticlesToDeploy){
                    DeploymentOptions childOptions = new DeploymentOptions ();
                    childOptions.setWorker (true);
                    String name = child.getClass ().getName ();
                    this.vertx.deployVerticle (child, childOptions, (deployResult) -> {
                                if (deployResult.succeeded ()) {
                                    System.out.println (moduleName+" module worker verticle deployment successful(" + name +
                                            "). Deployment id is: " + deployResult.result ());
                                }
                                else {
                                    //noinspection ThrowableResultOfMethodCallIgnored
                                    deployResult.cause ().printStackTrace ();
                                }
                            }
                    );
                }

                final Router router = Router.router (this.vertx);
                //now deploy standard verticles
                verticlesToDeploy = getChildStandardVerticles (router, baseRoute);

                for (Verticle child : verticlesToDeploy){
                    String name = child.getClass ().getName ();
                    this.vertx.deployVerticle (child, (deployResult) -> {
                                if (deployResult.succeeded ()) {
                                    System.out.println (moduleName+" module standard verticle ("+name+")deployment successful. Deployment id is: " + deployResult.result ());
                                }
                                else {
                                    //noinspection ThrowableResultOfMethodCallIgnored
                                    deployResult.cause ().printStackTrace ();
                                }
                            }
                    );
                }

                httpServer.requestHandler (router::accept).listen (port, handler -> {
                    if (handler.succeeded()) {
                        System.out.println ("Starting HTTP server succeeded....");
                    }

                    if (handler.failed ()) {
                        System.out.println (handler.cause ().getMessage());
                        try {
                            this.stop ();
                        }
                        catch (Exception e) {
                            e.printStackTrace ();
                        }
                    }
                });
            }
            else
            {
                System.out.println ("Could not deploy "+ moduleName+" module verticle, reason:");
                System.out.println (result.cause ().getMessage ());
            }
        });
    }

}
