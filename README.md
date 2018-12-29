## REST API in pure Java without any frameworks

This is a demo application developed in Java 11 using 
[`jdk.httpserver`](https://docs.oracle.com/javase/10/docs/api/com/sun/net/httpserver/package-summary.html) module 
and a few additional Java libraries (like [vavr](http://www.vavr.io/), [lombok](https://projectlombok.org/)).

## Genesis of this project
I am a day-to-day Spring developer and I got used to this framework so much that I imagined how it would be to forget about it for a while
and try to build completely pure Java application from scratch. 

I thought it could be interesting from learning perspective and a bit refreshing.

When I started building this I often came across situations when I missed some features which Spring provides out of the box.

At that times, instead of switching on another Spring capability, I had to rethink it and develop it myself.

It occurred that for real business case I would probably still prefer to use Spring instead of reinventing a wheel.

Still, I believe the exercise was pretty interesting experience.

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

## Parsing request params
Parsing request params is another "feature" which we'll need to implement ourselves in contrary to utilising a framework.
Let's say we would like our hello api to respond with a name passed as a param, e.g.: 

```bash
curl localhost:8000/api/hello?name=Marcin

Hello Marcin!

```
We could parse params with a method like: 

```java
public static Map<String, List<String>> splitQuery(String query) {
        if (query == null || "".equals(query)) {
            return Collections.emptyMap();
        }

        return Pattern.compile("&").splitAsStream(query)
            .map(s -> Arrays.copyOf(s.split("="), 2))
            .collect(groupingBy(s -> decode(s[0]), mapping(s -> decode(s[1]), toList())));

    }
```

and use it as below: 

```java
 Map<String, List<String>> params = splitQuery(exchange.getRequestURI().getRawQuery());
String noNameText = "Anonymous";
String name = params.getOrDefault("name", List.of(noNameText)).stream().findFirst().orElse(noNameText);
String respText = String.format("Hello %s!", name);
           
```

You can find complete example in branch:

```bash
git checkout step-4
```

Similarly if we wanted to use path params, e.g.: 

```bash
curl localhost:8000/api/items/1
```
to get item by id=1, we would need to parse the path ourselves to extract an id from it. This is getting cumbersome.


## Secure endpoint
A common case in each REST API is to protect some endpoints with credentials, e.g. using basic authentication.
For each server context we can set an authenticator as below: 

```java
HttpContext context =server.createContext("/api/hello", (exchange -> {
  // this part remains unchanged
}));
context.setAuthenticator(new BasicAuthenticator("myrealm") {
    @Override
    public boolean checkCredentials(String user, String pwd) {
        return user.equals("admin") && pwd.equals("admin");
    }
});
```

The "myrealm" in `BasicAuthenticator` is a realm name. Realm is a virtual name which can be used to separate different authentication spaces. 
You can read more about it in [RFC 1945](https://tools.ietf.org/html/rfc1945#section-11)

You can now invoke this protected endpoint by adding an `Authorization` header like that: 

```bash
curl -v localhost:8000/api/hello?name=Marcin -H 'Authorization: Basic YWRtaW46YWRtaW4='
```

The text after `Basic` is a Base64 encoded `admin:admin`  which are credentials hardcoded in our example code.
In real application to authenticate user you would probably get it from the header and compare with username and password store in database.
If you skip the header the API will respond with status
```
HTTP/1.1 401 Unauthorized

```

Check out the complete code from branch:

```bash
git checkout step-5
```

## JSON, exception handlers and others

Now it's time for more complex example. 

From my past experience in software development the most common API I was developing was exchanging JSON.

We're going to develop an API to register new users. We will use an in-memory database to store them.

Our user domain object will be simple: 

```java
@Value
@Builder
public class User {

    String id;
    String login;
    String password;
}

```
I'm using Lombok annotations to save me from constructor and getters boilerplate code, it will be generated in build time.

In REST API I want to pass only login and password so I created a separate domain object: 

```java
@Value
@Builder
public class NewUser {

    String login;
    String password;
}

```

Users will be created in a service which I will use in my API handler. The service method is simply storing the user. 
In complete application it could do more, like send events after successful user registration.

```java
public String create(NewUser user) {
    return userRepository.create(user);
}
```

Our in-memory implementation of repository is as follows: 
```java

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.consulner.domain.user.NewUser;
import com.consulner.domain.user.User;
import com.consulner.domain.user.UserRepository;

public class InMemoryUserRepository implements UserRepository {

    private static final Map USERS_STORE = new ConcurrentHashMap();

    @Override
    public String create(NewUser newUser) {
        String id = UUID.randomUUID().toString();
        User user = User.builder()
            .id(id)
            .login(newUser.getLogin())
            .password(newUser.getPassword())
            .build();
        USERS_STORE.put(newUser.getLogin(), user);

        return id;
    }
}
```
Finally, let's glue all together in handler:

```java
protected void handle(HttpExchange exchange) throws IOException {
        if (!exchange.getRequestMethod().equals("POST")) {
            throw new UnsupportedOperationException();
        }

        RegistrationRequest registerRequest = readRequest(exchange.getRequestBody(), RegistrationRequest.class);

        NewUser user = NewUser.builder()
            .login(registerRequest.getLogin())
            .password(PasswordEncoder.encode(registerRequest.getPassword()))
            .build();

        String userId = userService.create(user);

        exchange.getResponseHeaders().set(Constants.CONTENT_TYPE, Constants.APPLICATION_JSON);
        exchange.sendResponseHeaders(StatusCode.CREATED.getCode(), 0);

        byte[] response = writeResponse(new RegistrationResponse(userId));

        OutputStream responseBody = exchange.getResponseBody();
        responseBody.write(response);
        responseBody.close();
    }
```

It translates JSON request into `RegistrationRequest` object: 

```java
@Value
class RegistrationRequest {

    String login;
    String password;
}
```

which I later map to domain object `NewUser` to finally save it in database and write response as JSON.

I need to translate `RegistrationResponse` object back to JSON string.

Marshalling and unmarshalling JSON is done with Jackson object mapper (`com.fasterxml.jackson.databind.ObjectMapper`).

And this is how I instantiate the new handler in application main method: 

```java
 public static void main(String[] args) throws IOException {
        int serverPort = 8000;
        HttpServer server = HttpServer.create(new InetSocketAddress(serverPort), 0);

        RegistrationHandler registrationHandler = new RegistrationHandler(getUserService(), getObjectMapper(),
            getErrorHandler());
        server.createContext("/api/users/register", registrationHandler::handle);
        
        // here follows the rest.. 

 }
```

You can find the working example in separate git branch, where I also added a global exception handler which is used
by the API to respond with a standard JSON error message in case, e.g. when HTTP method is not supported or API request is malformed.

```java
git checkout step-6
```

You can run the application and try one of the example requests below: 

```bash
curl -X POST localhost:8000/api/users/register -d '{"login": "test" , "password" : "test"}'
```

response: 
```bash
{"id":"395eab24-1fdd-41ae-b47e-302591e6127e"}
```

```bash
curl -v -X POST localhost:8000/api/users/register -d '{"wrong": "request"}'
```

response: 
```bash
< HTTP/1.1 400 Bad Request
< Date: Sat, 29 Dec 2018 00:11:21 GMT
< Transfer-encoding: chunked
< Content-type: application/json
< 
* Connection #0 to host localhost left intact
{"code":400,"message":"Unrecognized field \"wrong\" (class com.consulner.app.api.user.RegistrationRequest), not marked as ignorable (2 known properties: \"login\", \"password\"])\n at [Source: (sun.net.httpserver.FixedLengthInputStream); line: 1, column: 21] (through reference chain: com.consulner.app.api.user.RegistrationRequest[\"wrong\"])"}
```

Also, by chance I encountered a project [java-express](https://github.com/Simonwep/java-express) 
which is a Java counterpart of Node.js [Express](https://expressjs.com/) framework 
and is using jdk.httpserver as well, so all the concepts covered in this article you can find in real-life application framework :) 
which is also small enough to digest the codes quickly.