package com.chapter1_2.ep.controller;

import com.chapter1_2.ep.model.Item;
import com.chapter1_2.ep.repository.ItemRepository;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Links;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

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
        // IANA 표준에 따라 링크 이름을 item으로 명명한다.
        Mono<Link> aggregateLink = linkTo(controller.findAll()).withRel(IanaLinkRelations.ITEM).toMono();

        // 여러개의 비동기 ㅇㅅ청을 실행하고 각 결과를 하나로 합치기 위해 Mono.zip() 메소드를 사용한다.
        // findById() 메소드호출과 selflink, aggretatelink 생성 요청 결과를 타입 안정성이 보장되는 리액터 Tuple 타입에 넣고 Mono로 감싼다.
        return Mono.zip(itemRepository.findById(id),selfLink,aggregateLink)
                // map을 통해 Tuple에 있던 여러 비동기 요청 결과를 꺼내서 EntityModel로만들고 Mono로 감싸서 반환한다.
                .map(o -> EntityModel.of(o.getT1(), Links.of(o.getT2(),o.getT3())));

    }

}
