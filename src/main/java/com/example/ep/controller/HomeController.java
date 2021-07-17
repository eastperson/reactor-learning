package com.example.ep.controller;

import com.example.ep.model.Cart;
import com.example.ep.model.CartItem;
import com.example.ep.repository.CartRepository;
import com.example.ep.repository.ItemRepository;
import com.example.ep.service.CartService;
import com.example.ep.service.ItemService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.reactive.result.view.Rendering;
import reactor.core.publisher.Mono;

@Controller
public class HomeController {

    private ItemRepository itemRepository;
    private CartRepository cartRepository;
    private CartService cartService;
    private ItemService itemService;

    public HomeController(ItemRepository itemRepository, CartRepository cartRepository,CartService cartService, ItemService itemService) {
        this.itemRepository = itemRepository;
        this.cartRepository = cartRepository;
        this.cartService = cartService;
        this.itemService = itemService;
    }

    // 뷰(view)와 애트리뷰트(attribute)를 포함하는 웹플럭스 컨테이너인 Mono<Rendering>을 반환한다.
    @GetMapping
    Mono<Rendering> home() {
        return Mono.just(Rendering.view("home.html")
                .modelAttribute("items",this.itemRepository.findAll())
                .modelAttribute("cart",this.cartRepository.findById("My Cart").defaultIfEmpty(new Cart("My Cart")))
                .build());

    }

    @PostMapping("/add/{id}")
    Mono<String> addToCart(@PathVariable String id) {
        return cartService.addToCart("My Cart",id).thenReturn("redirect:/");
    }

    @GetMapping("/search")
    Mono<Rendering> search(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String description,
            @RequestParam boolean useAnd) {
        return Mono.just(Rendering.view("home.html")
                .modelAttribute("results", itemService.searchByExample(name,description,useAnd))
                .build());
    }
}