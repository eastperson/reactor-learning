package com.chapter1_2.ep.repository;

import com.chapter1_2.ep.model.Item;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface ItemRepository extends ReactiveCrudRepository<Item,String> {

    Flux<Item> findByNameContaining(String partialName);

//    @Query("{'name' : ?0, 'age' : ?1}")
//    Flux<Item> findItemsForCustomerMonthlyReport(String name, int age);

//    @Query(sort = "{'age' : -1}")
//    Flux<Item> findSortedStuffForWeeklyReport();

    // name 검색
    Flux<Item> findByNameContainingIgnoreCase(String partialName);

    // description 검색
    Flux<Item> findByDescriptionContainingIgnoreCase(String partialName);

    // name AND description 검색
    Flux<Item> findByNameContainingAndDescriptionContainingAllIgnoreCase(String partialName, String partialDesc);

    // name OR description 검색
    Flux<Item> findByNameContainingOrDescriptionContainingAllIgnoreCase(String partialName, String partialDesc);

    Flux<Item> findByName(String name);

//    Flux<Item> findAllAsList();
}
