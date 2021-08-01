package com.chapter1_2.ep.controller;


import com.chapter1_2.ep.model.Item;
import com.chapter1_2.ep.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Links;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import static org.springframework.hateoas.server.reactive.WebFluxLinkBuilder.linkTo;
import static org.springframework.hateoas.server.reactive.WebFluxLinkBuilder.methodOn;

@RestController
@RequiredArgsConstructor
public class AffordancesItemController {

    private final ItemRepository itemRepository;

    @PutMapping("/affordances/items/{id}")
    public Mono<ResponseEntity<?>> updateItem(@RequestBody Mono<EntityModel<Item>> item, @PathVariable String id) {
        return item
                // 클라이언트가 Item 객체를 보낼 수 있고 하이퍼미디어 형식일 수도 있음을 의미한다.
                .map(EntityModel::getContent)
                // Mono에 담긴 내용물을 꺼내서 새 Item을 생성하고 전달받은 id 값을 새 Item 객체의 식별자로 지정.
                // 몽고디비에 같은 id 값으로 저장돼 있던 레코드를 새로 만든 Item 정보로 덮어쓰거나, 몽고디비에 해당 id가 없다면 새 레코드가 추가된다.
                .map(content -> new Item(id, content.getName(), content.getDescription(),content.getPrice()))
                .flatMap(this.itemRepository::save)
                .then(findOne(id))
                .map(model -> ResponseEntity.noContent()
                        .location(model.getRequiredLink(IanaLinkRelations.SELF).toUri()).build());
    }

    @GetMapping("/affordances/items/{id}")
    Mono<EntityModel<Item>> findOne(@PathVariable String id) {
        AffordancesItemController controller = methodOn(AffordancesItemController.class);

        Mono<Link> selfLink = linkTo(controller.findOne(id))
                .withSelfRel()
                .andAffordance(controller.updateItem(null,id))
                .toMono();

        Mono<Link> aggregateLink = linkTo(controller.findAll())
                .withRel(IanaLinkRelations.ITEM)
                .toMono();

        return Mono.zip(itemRepository.findById(id),selfLink,aggregateLink)
                .map(o -> EntityModel.of(o.getT1(), Links.of(o.getT2(),o.getT3())));

    }

    @GetMapping("/affordances/items")
    Mono<CollectionModel<EntityModel<Item>>> findAll() {
        AffordancesItemController controller = methodOn(AffordancesItemController.class);

        Mono<Link> aggregateRoot = linkTo(controller.findAll())
                .withSelfRel()
                .andAffordance(controller.addNewItem(null))
                .toMono();

        return this.itemRepository.findAll()
                .flatMap(item -> findOne(item.getId()))
                .collectList()
                .flatMap(models -> aggregateRoot
                    .map(selfLink -> CollectionModel.of(models,selfLink)));
    }

    @PostMapping("/affordances/items")
    Mono<ResponseEntity<?>> addNewItem(@RequestBody Mono<EntityModel<Item>> item) {
        return item
                .map(EntityModel::getContent)
                .flatMap(this.itemRepository::save)
                .map(Item::getId)
                .flatMap(this::findOne)
                .map(newModel -> ResponseEntity.created(newModel
                    .getRequiredLink(IanaLinkRelations.SELF)
                        .toUri()).body(newModel.getContent()));
    }

}
