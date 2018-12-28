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
git checkout step-2
```

## Support different HTTP methods
Our first endpoint works like a charm but you will notice that no matter which HTTP method you'll use it will respond the same.
E.g.: 

```bash
curl -X POST localhost:8000/api/hello
curl -X PUT localhost:8000/api/hello
```

The first gotcha when building the API ourselves without a framework is that we need to add our own code to distinguish the methods, e.g.:

```java
        server.createContext("/api/hello", (exchange -> {

            if ("GET".equals(exchange.getRequestMethod())) {
                String respText = "Hello!";
                exchange.sendResponseHeaders(200, respText.getBytes().length);
                OutputStream output = exchange.getResponseBody();
                output.write(respText.getBytes());
                output.flush();
            } else {
                exchange.sendResponseHeaders(405, -1);// 405 Method Not Allowed
            }
            exchange.close();
        }));
```

Now try again request: 
```bash
curl -v -X POST localhost:8000/api/hello
```
and the response would be like: 

```bash
> POST /api/hello HTTP/1.1
> Host: localhost:8000
> User-Agent: curl/7.61.0
> Accept: */*
> 
< HTTP/1.1 405 Method Not Allowed
```

There are also a few things to remember, like to flush output or close exchange every time we return from the api.
When I used Spring I even did not have to think about it.

Try this part from branch:

```bash
git checkout step-3
```