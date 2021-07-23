package com.chapter1_2.ep.controller;

import com.chapter1_2.ep.model.Cart;
import com.chapter1_2.ep.model.CartItem;
import com.chapter1_2.ep.model.Item;
import com.chapter1_2.ep.repository.CartRepository;
import com.chapter1_2.ep.repository.ItemRepository;
import com.chapter1_2.ep.service.CartService;
import com.chapter1_2.ep.service.ItemService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

// HomeController에 국한된 스프링 웹플럭스 슬라이스 테스트를 사용하도록 설정
@WebFluxTest(HomeController.class)
public class HomeControllerSliceTest {

    // WebTestClient 인스턴스가 생성되고 주입
    @Autowired WebTestClient client;

    // 협력자를 가짜 객체로 만들어 사용
    @MockBean ItemService itemService;
    @MockBean ItemRepository itemRepository;
    @MockBean CartRepository cartRepository;
    @MockBean CartService cartService;

    @Test
    void homePage(){
        when(itemService.getInventory()).thenReturn(Flux.just(
                new Item("id1","name1","desc1",1.99),
                new Item("id2","name2","desc2",1.99)
        ));

        Item sampleItem = new Item("item1","TV tray", "Alf TV tray", 19.99);
        CartItem sampleCartItem = new CartItem(sampleItem);
        Cart sampleCart = new Cart("My Cart", Collections.singletonList(sampleCartItem));
        when(cartRepository.findById(anyString())).thenReturn(Mono.empty());
        when(itemRepository.findById(anyString())).thenReturn(Mono.just(sampleItem));
        when(cartRepository.save(any(Cart.class))).thenReturn(Mono.just(sampleCart));


        when(itemService.getCart("My Cart"))
                .thenReturn(Mono.just(new Cart("My Cart")));

        client.get().uri("/").exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .consumeWith(exchangeResult -> {
                    assertThat(exchangeResult.getResponseBody()).contains("My Cart");
                });
    }
}
