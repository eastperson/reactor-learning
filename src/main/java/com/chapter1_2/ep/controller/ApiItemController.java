package com.chapter1_2.ep.controller;

import com.chapter1_2.ep.model.Item;
import com.chapter1_2.ep.repository.ItemRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.net.URI;

@RestController
public class ApiItemController {

    private final ItemRepository itemRepository;

    public ApiItemController(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    // 0개 또는 그 이상의 Item 객체가 JSON 구조로 직렬화돼서 응답 본문에 기록
    @GetMapping("/api/items")
    Flux<Item> findAll(){
        return this.itemRepository.findAll();
    }

    @GetMapping("/api/items/{id}")
    Mono<Item> findOne(@PathVariable String id) {
        return this.itemRepository.findById(id);
    }

    @PostMapping("/api/items")
    // 인자타입이 리액터 타입인 Mono이므로 요청 처리를 위한 리액티브 플로우에서 구독이 발생하지 않으면
    // 요청 본문을 Item 타입으로 역직렬화하는 과정도 실행되지 않는다.
    Mono<ResponseEntity<?>> addNewItem(@RequestBody Mono<Item> item) {

        // 본문에 들어온 item 객체에 map이나 flatMap 연산을 적용한다.
        // 인자로 받은 Mono에서 스프링 데이터의 SAVE()메소드에 전달되고 다시 Mono를 반환하므로 flatMap을 사용한다.
        return item.flatMap(s -> this.itemRepository.save(s))
                .map(savedItem -> ResponseEntity
                    .created(URI.create("/api/items/" + savedItem.getId()))
                        // 생성된 item을 직렬화한다.
                        .body(savedItem));
    }

    @PutMapping("/api/items/{id}")
    public Mono<ResponseEntity<?>> updateItem(
            @RequestBody Mono<Item> item,
            @PathVariable String id
    ) {
        return item.map(content -> new Item(id, content.getName(), content.getDescription(), content.getPrice()))
                .flatMap(this.itemRepository::save)
                .map(ResponseEntity::ok);
    }

}
