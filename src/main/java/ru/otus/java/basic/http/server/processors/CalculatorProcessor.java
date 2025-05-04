package ru.otus.java.basic.http.server.processors;

import ru.otus.java.basic.http.server.HttpRequest;
import ru.otus.java.basic.http.server.exceptions.BadRequestException;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class CalculatorProcessor implements RequestProcessor {
    private int getParameterAsInt(HttpRequest request, String parameterName) {
        if (!request.containsParameter(parameterName)) {
            throw new BadRequestException("INCORRECT_REQUEST_DATA", String.format("Отсутствует параметр запроса '%s'", parameterName));
        }
        final var parameterValue = request.getParameter(parameterName);
        try {
            return Integer.parseInt(parameterValue);
        } catch (NumberFormatException e) {
            throw new BadRequestException("400", String.format("Can`t parse to int, parameter.name: '%s', parameter.value: '%s'", parameterName, parameterValue));
        }
    }

    @Override
    public void execute(HttpRequest request, OutputStream output) throws IOException {
        int a = getParameterAsInt(request, "a");
        int b = getParameterAsInt(request, "b");

        String response = "" +
                "HTTP/1.1 200 OK\r\n" +
                "Content-Type: text/html\r\n" +
                "\r\n" +
                "<html><body><h1>" + a + " + " + b + " = " + (a + b) + "</h1></body></html>";
        output.write(response.getBytes(StandardCharsets.UTF_8));
    }
}
