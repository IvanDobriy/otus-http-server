package ru.otus.java.basic.http.server;

public class Application {
    public static void main(String[] args) {
         new HttpServer(8189).start();
    }
}
