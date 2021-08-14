package com.chapter1_2.ep.repository;

import com.chapter1_2.ep.model.User;
import org.springframework.data.repository.CrudRepository;
import reactor.core.publisher.Mono;

public interface UserRepository extends CrudRepository<User,String> {

    Mono<User> findByName(String name);
}
