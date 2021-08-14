package com.chapter1_2.ep.controller;

import com.chapter1_2.ep.model.Cart;
import com.chapter1_2.ep.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.result.view.Rendering;
import reactor.core.publisher.Mono;

@Controller
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    private final ItemService itemService;
    static final String INVENTORY = "INVENTORY";

    @GetMapping
    Mono<Rendering> home(Authentication auth) {
        return Mono.just(Rendering.view("home2.html")
            .modelAttribute("items",this.itemService.getInventory())
            .modelAttribute("cart",this.itemService.getCart(cartName(auth))
                    .defaultIfEmpty(new Cart(cartName(auth))))
                .modelAttribute("auth",auth)
                .build());
    }


    @PostMapping("/add/{id}")
    Mono<String> addToCart(Authentication auth, @PathVariable String id) {
        return this.itemService.addItemToCart(cartName(auth),id)
                .thenReturn("redirect:/");
    }

    @DeleteMapping("/remove/{id}")
    Mono<String> removeFromCart(Authentication auth, @PathVariable String id) {
        return this.itemService.removeOneFromCart(cartName(auth),id)
                .thenReturn("redirect:/");
    }

    private static String cartName(Authentication auth) {
        return auth.getName() + "'s Cart";
    }


}
