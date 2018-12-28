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





