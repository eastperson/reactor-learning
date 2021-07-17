package com.example.ep.service;

import com.example.ep.model.Item;
import com.example.ep.repository.ItemByExampleRepository;
import com.example.ep.repository.ItemRepository;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.mongodb.core.ReactiveFluentMongoOperations;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import static org.springframework.data.mongodb.core.query.Criteria.byExample;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

@Service
public class ItemService {

    private final ItemRepository itemRepository;
    private final ItemByExampleRepository itemByExampleRepository;
    private final ReactiveFluentMongoOperations fluentMongoOperations;

    ItemService(ItemRepository itemRepository, ItemByExampleRepository itemByExampleRepository,ReactiveFluentMongoOperations fluentMongoOperations) {
        this.itemRepository = itemRepository;
        this.itemByExampleRepository = itemByExampleRepository;
        this.fluentMongoOperations = fluentMongoOperations;
    }

    // 최악의 분기처리
    Flux<Item> search(String partialName, String partialDescription, boolean useAnd) {
        if(partialName != null) {
            if(partialDescription != null) {
                if(useAnd) {
                    return itemRepository
                            .findByNameContainingAndDescriptionContainingAllIgnoreCase(partialName,partialDescription);
                } else {
                    return itemRepository.findByNameContainingOrDescriptionContainingAllIgnoreCase(partialName,partialDescription);
                }
            } else {
                return itemRepository.findByNameContaining(partialName);
            }
        } else {
            if(partialDescription != null) {
                return itemRepository.findByDescriptionContainingIgnoreCase(partialDescription);
            } else {
                return itemRepository.findAll();
            }
        }
    }

    public Flux<Item> searchByExample(String name, String description, boolean useAnd) {
        Item item = new Item(name,description,0.0);

        ExampleMatcher matcher = (useAnd
            ? ExampleMatcher.matchingAll()
            : ExampleMatcher.matchingAny())
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING)
                .withIgnoreCase()
                .withIgnorePaths("price");


        Example<Item> probe = Example.of(item,matcher);

        return itemByExampleRepository.findAll(probe);
    }

    public Flux<Item> searchByFluentExample(String name, String description) {

        return fluentMongoOperations.query(Item.class)
                .matching(query(where("TV tray").is(name).and("Smutf").is(description)))
                .all();
    }

    public Flux<Item> searchByFluentExample(String name, String description, boolean useAnd) {

        Item item = new Item(name,description,0.0);

        ExampleMatcher matcher = (useAnd
                ? ExampleMatcher.matchingAll()
                : ExampleMatcher.matchingAny())
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING)
                .withIgnoreCase()
                .withIgnorePaths("price");

        return fluentMongoOperations.query(Item.class)
                .matching(query(byExample(Example.of(item,matcher))))
                .all();
    }

}
