package com.chapter1_2.ep.controller;

import com.chapter1_2.ep.model.Item;
import com.chapter1_2.ep.repository.ItemRepository;
import org.springframework.hateoas.Affordance;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Links;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.mediatype.alps.Alps;
import org.springframework.hateoas.mediatype.alps.Type;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;

import static org.springframework.hateoas.mediatype.alps.Alps.alps;
import static org.springframework.hateoas.mediatype.alps.Alps.descriptor;
import static org.springframework.hateoas.server.reactive.WebFluxLinkBuilder.linkTo;
import static org.springframework.hateoas.server.reactive.WebFluxLinkBuilder.methodOn;

@RestController
public class HypermediaItemController {

    private final ItemRepository itemRepository;

    public HypermediaItemController(ItemRepository itemRepository){
        this.itemRepository = itemRepository;
    }

    @GetMapping("/hypermedia/items/{id}")
    Mono<EntityModel<Item>> findOne(@PathVariable String id) {

        // 컨트롤러에 대한 프록시를 생성
        HypermediaItemController controller = methodOn(HypermediaItemController.class);

        // findOne() 메소드에 대한 링크 생성.
        // 현재 메소드가 findOne() 메소드이므로 self라는 이름의 링크를 추가하고 리액터 Mono에 담아 반환
        Mono<Link> selfLink = linkTo(controller.findOne(id)).withSelfRel().toMono();

        // 모든 상품을 반환하는 findAll() 메소드를 찾아 애그리것 루트(aggregate root)에 대한 링크를 생성한다.
        // IANA 표준에 따라 링크 이름을 item으로` 명명한다.
        Mono<Link> aggregateLink = linkTo(controller.findAll())
                .withRel(IanaLinkRelations.ITEM).toMono();

        // 여러개의 비동기 요청을 실행하고 각 결과를 하나로 합치기 위해 Mono.zip() 메소드를 사용한다.
        // findById() 메소드호출과 selflink, aggretatelink 생성 요청 결과를 타입 안정성이 보장되는 리액터 Tuple 타입에 넣고 Mono로 감싼다.
        return Mono.zip(itemRepository.findById(id),selfLink,aggregateLink)
                // map을 통해 Tuple에 있던 여러 비동기 요청 결과를 꺼내서 EntityModel로만들고 Mono로 감싸서 반환한다.
                .map(o -> EntityModel.of(o.getT1(), Links.of(o.getT2(),o.getT3())));

    }

    @GetMapping(value = "/hypermedia/items/profile", produces = MediaTypes.ALPS_JSON_VALUE)
    public Alps profile(){
        return alps()
                    .descriptor(Collections.singletonList(descriptor()
                        .id(Item.class.getSimpleName() + "-repr")
                         .descriptor(Arrays.stream(
                                 Item.class.getDeclaredFields())
                                .map(field -> descriptor()
                                    .name(field.getName())
                                        .type(Type.SEMANTIC)
                                        .build())
                                 .collect(Collectors.toList()))
                            .build()))
                .build();
    }

    @GetMapping("/hypermedia")
    Mono<RepresentationModel<?>> root() {
        HypermediaItemController controller = //
                methodOn(HypermediaItemController.class);

        Mono<Link> selfLink = linkTo(controller.root()).withSelfRel().toMono();

        Mono<Link> itemsAggregateLink = //
                linkTo(controller.findAll()) //
                        .withRel(IanaLinkRelations.ITEM) //
                        .toMono();

        return selfLink.zipWith(itemsAggregateLink) //
                .map(links -> Links.of(links.getT1(), links.getT2())) //
                .map(links -> new RepresentationModel<>(links.toList()));
    }

    @GetMapping("/hypermedia/items")
    Mono<CollectionModel<EntityModel<Item>>> findAll() {

        return this.itemRepository.findAll() //
                .flatMap(item -> findOne(item.getId())) //
                .collectList() //
                .flatMap(entityModels -> linkTo(methodOn(HypermediaItemController.class) //
                        .findAll()).withSelfRel() //
                        .toMono() //
                        .map(selfLink -> CollectionModel.of(entityModels, selfLink)));
    }
    // end::find-all[]
    // end::find-one[]

    // tag::find-affordance[]
    @GetMapping("/hypermedia/items/{id}/affordances")
    // <1>
    Mono<EntityModel<Item>> findOneWithAffordances(@PathVariable String id) {
        HypermediaItemController controller = //
                methodOn(HypermediaItemController.class);

        Mono<Link> selfLink = linkTo(controller.findOne(id)).withSelfRel() //
                .andAffordance(controller.updateItem(null, id)) // <2>
                .toMono();

        Mono<Link> aggregateLink = linkTo(controller.findAll()).withRel(IanaLinkRelations.ITEM) //
                .toMono();

        return Mono.zip(itemRepository.findById(id), selfLink, aggregateLink) //
                .map(o -> EntityModel.of(o.getT1(), Links.of(o.getT2(), o.getT3())));
    }
    // end::find-affordance[]

    // tag::add-new-item[]
    @PostMapping("/hypermedia/items")
    Mono<ResponseEntity<?>> addNewItem(@RequestBody Mono<EntityModel<Item>> item) {
        return item //
                .map(EntityModel::getContent) //
                .flatMap(this.itemRepository::save) //
                .map(Item::getId) //
                .flatMap(this::findOne) //
                .map(newModel -> ResponseEntity.created(newModel //
                        .getRequiredLink(IanaLinkRelations.SELF) //
                        .toUri()).build());
    }
    // end::add-new-item[]

    // tag::update-item[]
    @PutMapping("/hypermedia/items/{id}") // <1>
    public Mono<ResponseEntity<?>> updateItem(@RequestBody Mono<EntityModel<Item>> item, // <2>
                                              @PathVariable String id) {
        return item //
                .map(EntityModel::getContent) //
                .map(content -> new Item(id, content.getName(), // <3>
                        content.getDescription(), content.getPrice())) //
                .flatMap(this.itemRepository::save) // <4>
                .then(findOne(id)) // <5>
                .map(model -> ResponseEntity.noContent() // <6>
                        .location(model.getRequiredLink(IanaLinkRelations.SELF).toUri()).build());
    }
    // end::update-item[]

}
