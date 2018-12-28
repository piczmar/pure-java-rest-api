## REST API in pure Java without any frameworks

This is a demo application developed in Java 11 using 
[`jdk.httpserver`](https://docs.oracle.com/javase/10/docs/api/com/sun/net/httpserver/package-summary.html) module 
and a few additional Java libraries (like [vavr](http://www.vavr.io/), [lombok](https://projectlombok.org/)).

## Genesis of this project
I am a day-to-day Spring developer and I got used to this framework so much that I thought how it would be to forget about it for a while
and try to build such application from scratch. I thought it could be interesting from learning perspective and a bit refreshing.
When I started building this I often came across many situations when I missed some features which Spring provides out of the box.
This time, instead of turning on another Spring capability I had to rethink it and develop it myself.
It occurred that for real business case I would probably still prefer to use Spring instead of reinventing a wheel.
Still I believe the exercise was pretty interesting experience.

## Beginning.
I will go through this exercise step by step but not always pasting a complete code in text
but you can always checkout each step from a separate branch.
I started from empty `Application` main class. You can get an initial branch like that: 

```
git checkout step-1
```

## First endpoint

The starting point of the web application is `com.sun.net.httpserver.HttpServer` class. 
The most simple `/api/hello` endpoint could look as below: 

```java
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

import com.sun.net.httpserver.HttpServer;

class Application {

    public static void main(String[] args) throws IOException {
        int serverPort = 8000;
        HttpServer server = HttpServer.create(new InetSocketAddress(serverPort), 0);
        server.createContext("/api/hello", (exchange -> {
            String respText = "Hello!";
            exchange.sendResponseHeaders(200, respText.getBytes().length);
            OutputStream output = exchange.getResponseBody();
            output.write(respText.getBytes());
            output.flush();
            exchange.close();
        }));
        server.setExecutor(null); // creates a default executor
        server.start();
    }
}
```
When you run main program it will start web server at port `8000` and expose out first endpoint which is just printing `Hello!`, e.g. using curl:

```bash
curl localhost:8000/api/hello
```

Try it out yourself from branch:

```bash
git checkout part-2
```



