package com.chapter1_2.ep.service;

import com.chapter1_2.ep.model.Cart;
import com.chapter1_2.ep.model.CartItem;
import com.chapter1_2.ep.repository.CartRepository;
import com.chapter1_2.ep.repository.ItemRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class CartService {
    private final ItemRepository itemRepository;
    private final CartRepository cartRepository;

    CartService(ItemRepository itemRepository,CartRepository cartRepository) {
        this.itemRepository = itemRepository;
        this.cartRepository = cartRepository;
    }

    public Mono<Cart> addToCart(String cartId, String id) {
        return this.cartRepository.findById(cartId)
                .defaultIfEmpty(new Cart("My Cart"))
                .flatMap(cart -> cart.getCartItems().stream()
                        .filter(cartItem -> cartItem.getItem().getId().equals(id))
                        .findAny().map(cartItem -> {
                            cartItem.increment();
                            return Mono.just(cart);
                        })
                        .orElseGet(() ->
                            this.itemRepository.findById(id)
                                    .map(CartItem::new)
                                    .doOnNext(cartItem -> cart.getCartItems().add(cartItem))
                                    .map(cartItem -> cart)))
                        .flatMap(this.cartRepository::save);
    }

}
