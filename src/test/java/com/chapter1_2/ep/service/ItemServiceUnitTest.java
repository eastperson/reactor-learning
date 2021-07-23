package com.chapter1_2.ep.service;

import com.chapter1_2.ep.model.Cart;
import com.chapter1_2.ep.model.CartItem;
import com.chapter1_2.ep.model.Item;
import com.chapter1_2.ep.repository.CartRepository;
import com.chapter1_2.ep.repository.ItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.mongodb.core.FluentMongoOperations;
import org.springframework.data.mongodb.core.ReactiveFluentMongoOperations;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

// @ExtendWith는 테스트 핸들러를 지정할 수 있는 API이다.
// SpringExtension.class는 스프링에 특화된 테스트 기능을 사용할 수 있게 해준다.
@ExtendWith(SpringExtension.class)
public class ItemServiceUnitTest {

    // 테스트 대상 클래스(CUT, class under test) 테스트시 초기화
    ItemService itemService;

    // ItemService에 주입되는 협력자 테스트 대상이 아니므로 가짜 객체를 만들어서 테스트에 사용.
    // 까자 객체를 만들고 스프링 빈으로 등록하기 위해 @MockBean 사용
    @MockBean private ItemRepository itemRepository;
    @MockBean private CartRepository cartRepository;

    // 모든 테스트 메서드 실행 전에 실행
    @BeforeEach
    void setUp(){
        // 테스트 데이터 정의
        Item sampleItem = new Item("item1","TV tray","Alf TV tray",19.99);
        CartItem sampleCartItem = new CartItem(sampleItem);
        Cart sampleCart = new Cart("My Cart", Collections.singletonList(sampleCartItem));

        // 협력자와의 상호작용 정의
        // 모키토를 사용해서 가짜 객체와 상호작용을 정의
        when(cartRepository.findById(anyString())).thenReturn(Mono.empty());
        when(itemRepository.findById(anyString())).thenReturn(Mono.just(sampleItem));
        when(cartRepository.save(any(Cart.class))).thenReturn(Mono.just(sampleCart));
        itemService = new ItemService(itemRepository,cartRepository);
    }

    @DisplayName("top-level 방식 테스트")
    @Test
    void addItemToEmptyCartShouldProduceOneCartItem() {
        itemService.addItemToCart("My Cart","item1")
                // 메서드 반환타입인 Mono<Cart>를 StepVerifier.create()에 연결해서 테스트 기능을 전담하는 리액터 타입 핸들러 생성
                // 구독을 기다린다.
                .as(StepVerifier::create)
                // 함수와 람다식을 사용해서 결과를 검증
                .expectNextMatches(cart -> {
                    // 수량 검증
                    assertThat(cart.getCartItems()).extracting(CartItem::getQuantity)
                            .containsExactlyInAnyOrder(1);

                    // 아이템 검증
                    assertThat(cart.getCartItems()).extracting(CartItem::getItem)
                            .containsExactly(new Item("item1","TV tray","Alf TV tray",19.99));

                    // 위의 검증 단계를 통과하면 true 반환
                    return true;
                // 성공 시그널(complete) 발생하면 완료된 것을 검증
                }).verifyComplete();
    }


    @DisplayName("top-level이 아닌 방식")
    @Test
    void alternativeWayToTest(){

        // 메소드의 인자까지 뒤져봐야 무엇이 테스트되는지 알 수 있어 지양해야 하는 메서드이다.
        StepVerifier.create(
                itemService.addItemToCart("My Cart","item1"))
                .expectNextMatches(cart -> {
                    assertThat(cart.getCartItems()).extracting(CartItem::getQuantity)
                            .containsExactlyInAnyOrder(1);

                    assertThat(cart.getCartItems()).extracting(CartItem::getItem)
                            .containsExactly(new Item("item1","TV tray","Alf TV tray",19.99));

                    return true;
                }).verifyComplete();

    }

}
