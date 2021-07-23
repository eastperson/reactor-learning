package com.chapter1_2.ep.blockhound;

import com.chapter1_2.ep.model.Cart;
import com.chapter1_2.ep.model.CartItem;
import com.chapter1_2.ep.model.Item;
import com.chapter1_2.ep.repository.CartRepository;
import com.chapter1_2.ep.repository.ItemRepository;
import com.chapter1_2.ep.service.ItemService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
public class BlcokHoundIntegrationTest {

    ItemService itemService;

    @MockBean ItemRepository itemRepository;
    @MockBean CartRepository cartRepository;

    @BeforeEach
    void setUp(){
        // 테스트 데이터 정의
        Item sampleItem = new Item("item1","TV tray","Alf TV tray",19.99);
        CartItem sampleCartItem = new CartItem(sampleItem);
        Cart sampleCart = new Cart("My Cart", Collections.singletonList(sampleCartItem));

        // 협력자와의 가짜 상호작용 정의
        // 비어있는 결과를 리액터로부터 감춘다
        when(cartRepository.findById(anyString()))
                .thenReturn(Mono.<Cart> empty().hide());
        when(itemRepository.findById(anyString())).thenReturn(Mono.just(sampleItem));
        when(cartRepository.findById(anyString())).thenReturn(Mono.just(sampleCart));
        itemService = new ItemService(itemRepository,cartRepository);
    }

    @Test
    void blockHondShouldTrapBlockingCall(){

        //리액트 안에서 실행
        // delay()를 실행해서 후속 작업이 리액터 스레드 안에서 실행되도록한다.
        Mono.delay(Duration.ofSeconds(1))
                // tick 이벤트가 발생하면 addItemToCart()를 실행시킨다.
                .flatMap(tick -> itemService.addItemToCart("My Cart","item1"))
                .as(StepVerifier::create)
                // 블로킹 호출이 있으므로 예외가 발생하며 assert로 검증한다.
                .verifyErrorSatisfies(throwable -> {
                    assertThat(throwable).hasMessageContaining("block()/blockFirst()/blockLast are blocking");
                });
    }

}
