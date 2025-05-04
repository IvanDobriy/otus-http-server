package ru.otus.java.basic.http.server.processors;

import com.google.gson.Gson;
import ru.otus.java.basic.http.server.HttpRequest;
import ru.otus.java.basic.http.server.application.Item;
import ru.otus.java.basic.http.server.application.ItemsRepository;
import ru.otus.java.basic.http.server.exceptions.BadRequestException;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class CreateItemProcessor implements RequestProcessor {
    private ItemsRepository itemsRepository;

    public CreateItemProcessor(ItemsRepository itemsRepository) {
        this.itemsRepository = itemsRepository;
    }

    private void checkItem(Item item) {
        if (item.getTitle().isBlank()) {
            throw new BadRequestException("400", "item title is blank");
        }
        if (item.getPrice().doubleValue() < 0) {
            throw new BadRequestException("400", "item price is negative");
        }
    }

    @Override
    public void execute(HttpRequest request, OutputStream output) throws IOException {
        Gson gson = new Gson();
        Item newItem = gson.fromJson(request.getBody(), Item.class);
        checkItem(newItem);
        itemsRepository.addNewItem(newItem);
        String response = "" +
                "HTTP/1.1 201 Created\r\n" +
                "Content-Type: application/json\r\n" +
                "\r\n";
        output.write(response.getBytes(StandardCharsets.UTF_8));
    }
}
