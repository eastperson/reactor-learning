package com.chapter1_2.ep.controller;

import com.chapter1_2.ep.model.Item;
import com.chapter1_2.ep.repository.ItemRepository;
import com.chapter1_2.ep.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Links;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import reactor.core.publisher.Mono;

import static org.springframework.hateoas.server.reactive.WebFluxLinkBuilder.linkTo;
import static org.springframework.hateoas.server.reactive.WebFluxLinkBuilder.methodOn;

@Controller
@RequestMapping("/item")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;
    private final ItemRepository itemRepository;
    static final String INVENTORY = "INVENTORY";
    private static final SimpleGrantedAuthority ROLE_INVENTORY = new SimpleGrantedAuthority("ROLE_" + INVENTORY);

    @PreAuthorize("hasRole('"+INVENTORY+"')")
    @PostMapping("/add")
    Mono<ResponseEntity<?>> addNewItem(@RequestBody Item item, Authentication auth) {
        return this.itemRepository.save(item)
                .map(Item::getId)
                .flatMap(id -> findOne(id,auth))
                .map(newModel -> ResponseEntity.created(newModel.getRequiredLink(IanaLinkRelations.SELF)
                .toUri()).build());
    }

    @GetMapping("/{id}")
    Mono<EntityModel<Item>> findOne(@PathVariable String id, Authentication auth) {

        ItemController controller = methodOn(ItemController.class);

        Mono<Link> selfLink = linkTo(controller.findOne(id,auth)).withSelfRel().toMono();

        Mono<Link> aggregateLink = //
                linkTo(controller.findAll(auth)) //
                        .withRel(IanaLinkRelations.ITEM) //
                        .toMono();

        // 사용자에게 반환할 링크 정보를 헤이티오스 링크 데이터 모음인 Links에 담는다.
        Mono<Links> allLinks;

        // 권한이 있으면 DELETE 기능의 링크와 애그리것 루트를 포함
        // 튜플로 반환
        if(auth.getAuthorities().contains(ROLE_INVENTORY)) {
            Mono<Link> deleteLink = linkTo(controller.deleteItem(id)).withRel("delete")
                    .toMono();
            allLinks = Mono.zip(selfLink,aggregateLink,deleteLink)
                    .map(links -> Links.of(links.getT1(),links.getT2(),links.getT3()));
        } else {
            allLinks = Mono.zip(selfLink,aggregateLink)
                    .map(links -> Links.of(links.getT1(),links.getT2()));
        }

        // 헤이티오스 컨테이너로 변환해서 반환
        return this.itemRepository.findById(id)
                .zipWith(allLinks)
                .map(o -> EntityModel.of(o.getT1(),o.getT2()));
    }

    @GetMapping("")
    Mono<CollectionModel<EntityModel<Item>>> findAll(Authentication auth) {

        return this.itemRepository.findAll() //
                .flatMap(item -> findOne(item.getId(),auth)) //
                .collectList() //
                .flatMap(entityModels -> linkTo(methodOn(HypermediaItemController.class) //
                        .findAll()).withSelfRel() //
                        .toMono() //
                        .map(selfLink -> CollectionModel.of(entityModels, selfLink)));
    }

    @PreAuthorize("hasRole('"+INVENTORY+"')")
    @DeleteMapping("/delete/{id}")
    Mono<ResponseEntity<?>> deleteItem(@PathVariable String id) {
        return this.itemRepository.deleteById(id)
                .thenReturn(ResponseEntity.noContent().build());
    }


}
