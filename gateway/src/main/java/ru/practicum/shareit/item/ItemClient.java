package ru.practicum.shareit.item;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.client.BaseClient;
import ru.practicum.shareit.item.dto.CommentCreationDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.Map;

public class ItemClient extends BaseClient {

    private static final String API_PREFIX = "/items";

    @Autowired
    public ItemClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX))
                        .requestFactory(() -> new HttpComponentsClientHttpRequestFactory())
                        .build()
        );
    }

    public ResponseEntity<Object> add(long userId, ItemDto itemDto){
        return post("", userId, itemDto);
    }

    public ResponseEntity<Object> getAllByOwner(long ownerId){
        return get("", ownerId);
    }

    public ResponseEntity<Object> getById(long ownerId, long id){
        return get("/" + id, ownerId);
    }

    public ResponseEntity<Object> getSelection(long userId, String text){
        Map<String, Object> params = Map.of("text", text);
        return get("/search?text={text}", userId, params);
    }

    public ResponseEntity<Object> update(long ownerId, ItemDto itemDto){
        return patch("/" + itemDto.getId(), ownerId);
    }

    public ResponseEntity<Object> addComment(CommentCreationDto comment){
        return post("/" + comment.getAuthorId() + "/comment", comment);
    }


}
