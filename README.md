# Idea-Server
API Server of Idea site

## Compile & Pack to JAR
Pour compiler :
```bash
mvn clean compile
```

Pour packager (jar + fat-jar) :
```bash
mvn package
```

Pour compiler sans exécuter les tests, ajouter le paramètre `-DskipTests`.


## Run
First compile the server :
```bash
mvn clean package
```

Then there is two way for start the server :
  * with Vert.x's [Launcher](http://vertx.io/docs/apidocs/io/vertx/core/Launcher.htmll)
    *Todo*
  * with the fat JAR (for deployed)
    ```bash
    java -jar api-server-{version}-fat.jar -conf src/main/api-conf.json
    ```


## Troubleshoots
### _There is an `io.vertx.core.VertxException: Thread blocked` when I launch my server_
This error was due to the `io.vertx.core.impl.BlockedThreadChecker` who check in shortest time
 the deployement of verticle, who signal `Thread Thread[vert.x-eventloop-thread-0,5,main]
  has been blocked for 6806 ms, time limit is 2000`.  
A solution was to force a longer time with this option : `-Dvertx.options.blockedThreadCheckInterval=200000000`.
