package ru.otus.java.basic.http.server;

import com.google.gson.Gson;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.otus.java.basic.http.server.application.ItemsRepository;
import ru.otus.java.basic.http.server.exceptions.BadRequestException;
import ru.otus.java.basic.http.server.exceptions.ErrorDto;
import ru.otus.java.basic.http.server.processors.*;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Dispatcher {
    private Map<String, RequestProcessor> processors;
    private ItemsRepository itemsRepository;
    private RequestProcessor defaultNotFoundProcessor;
    private RequestProcessor defaultStaticResourceProcessor;
    private final Logger logger = LogManager.getLogger(this.getClass().getName());
    private final Gson gson;
    public Dispatcher() {
        this.itemsRepository = new ItemsRepository();
        this.processors = new HashMap<>();
        this.processors.put("GET /hello", new HelloProcessor());
        this.processors.put("GET /calculator", new CalculatorProcessor());
        this.processors.put("GET /items", new GetItemProcessor(itemsRepository));
        this.processors.put("POST /items", new CreateItemProcessor(itemsRepository));
        this.defaultNotFoundProcessor = new DefaultNotFoundProcessor();
        this.defaultStaticResourceProcessor = new DefaultStaticResourcesProcessor();
        this.gson = new Gson();
    }

    public void execute(HttpRequest request, OutputStream output) throws IOException {
        if (Files.exists(Paths.get("static/", request.getUri().substring(1)))) {
            defaultStaticResourceProcessor.execute(request, output);
            return;
        }
        if (!processors.containsKey(request.getRoutingKey())) {
            defaultNotFoundProcessor.execute(request, output);
            return;
        }
        try {
            processors.get(request.getRoutingKey()).execute(request, output);
        } catch (BadRequestException e) {
            logger.info("Bad request: {}, code: {}, stack trace: {}", e.getMessage(), e.getCode(), Arrays.asList(e.getStackTrace()));
            ErrorDto errorDto = new ErrorDto(e.getCode(), e.getMessage());
            String errorDtoJson = gson.toJson(errorDto);
            String response = "" +
                    "HTTP/1.1 400 Bad Request\r\n" +
                    "Content-Type: application/json\r\n" +
                    "\r\n" + errorDtoJson;
            output.write(response.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            logger.warn("Bad request: {}, stack trace: {}", e.getMessage(), Arrays.asList(e.getStackTrace()));
            // 500 Internal Server Error
            ErrorDto errorDto = new ErrorDto("500", e.getMessage());
            String errorDtoJson = gson.toJson(errorDto);
            String response = "" +
                    "HTTP/1.1 500 Internal server error\r\n" +
                    "Content-Type: application/json\r\n" +
                    "\r\n" + errorDtoJson;
            output.write(response.getBytes(StandardCharsets.UTF_8));
        }
    }
}
