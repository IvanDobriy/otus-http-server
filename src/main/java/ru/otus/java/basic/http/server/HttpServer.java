package ru.otus.java.basic.http.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class HttpServer {
    private int port;
    private Dispatcher dispatcher;
    private Executor executor = Executors.newFixedThreadPool(10);

    public HttpServer(int port) {
        this.port = port;
        this.dispatcher = new Dispatcher();
    }

    private Socket accept(ServerSocket serverSocket) {
        Socket socket = null;
        try {
            socket = serverSocket.accept();
        } catch (Exception e) {
            if (socket != null) {
                try {
                    socket.close();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            e.printStackTrace();
        }
        return socket;
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Сервер запущен на порту: " + port);
            while (true) {
                final Socket currentSocket = accept(serverSocket);
                if (currentSocket == null) {
                    continue;
                }
                executor.execute(() -> {
                    try {
                        byte[] buffer = new byte[8192];
                        int n = currentSocket.getInputStream().read(buffer);
                        if (n < 0) {
                            System.out.println("Получено битое сообщение");
                            return;
                        }
                        String rawRequest = new String(buffer, 0, n);
                        HttpRequest request = new HttpRequest(rawRequest);
                        request.info(true);
                        dispatcher.execute(request, currentSocket.getOutputStream());
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            currentSocket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
