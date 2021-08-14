package com.chapter1_2.ep.controller;

import com.chapter1_2.ep.model.Cart;
import com.chapter1_2.ep.repository.CartRepository;
import com.chapter1_2.ep.repository.ItemRepository;
import com.chapter1_2.ep.service.CartService;
import com.chapter1_2.ep.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.reactive.result.view.Rendering;
import reactor.core.publisher.Mono;

@Controller
@RequiredArgsConstructor
@RequestMapping("/home")
public class HomeController {

    private final ItemRepository itemRepository;
    private final CartRepository cartRepository;
    private final CartService cartService;
    private final ItemService itemService;

    // 뷰(view)와 애트리뷰트(attribute)를 포함하는 웹플럭스 컨테이너인 Mono<Rendering>을 반환한다.
    @GetMapping
    Mono<Rendering> home(
            // OAuth2User를 주입받아 OAuth 클라이언트 정보를 받는다.
            @RegisteredOAuth2AuthorizedClient OAuth2AuthorizedClient auth2AuthorizedClient,
            // 로그인한 사용자 정보를 받는다.
            @AuthenticationPrincipal OAuth2User oAuth2User
            ) {
        return Mono.just(Rendering.view("home3.html")
                .modelAttribute("items",this.itemService.getInventory())
                .modelAttribute("cart",this.itemService.getCart(cartName(oAuth2User))
                .defaultIfEmpty(new Cart(cartName(oAuth2User))))
                .modelAttribute("userName",oAuth2User.getName())
                .modelAttribute("authorities",oAuth2User.getAuthorities())
                .modelAttribute("clientName",auth2AuthorizedClient.getClientRegistration().getClientName())
                .modelAttribute("userAttributes",oAuth2User.getAttributes())
                .build());

    }

    @PostMapping("/add/{id}")
    Mono<String> addToCart(@PathVariable String id) {
        return cartService.addToCart("My Cart",id).thenReturn("redirect:/");
    }

    private static String cartName(OAuth2User oAuth2User) {
        return oAuth2User.getAttributes() + "'s Cart";
    }


    /*
    @GetMapping("/search")
    Mono<Rendering> search(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String description,
            @RequestParam boolean useAnd) {
        return Mono.just(Rendering.view("home.html")
                .modelAttribute("results", itemService.searchByExample(name,description,useAnd))
                .build());
    }
     */

}