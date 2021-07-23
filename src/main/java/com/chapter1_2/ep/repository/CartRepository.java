package com.chapter1_2.ep.repository;

import com.chapter1_2.ep.model.Cart;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface CartRepository extends ReactiveCrudRepository<Cart,String> {
}
