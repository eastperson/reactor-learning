package com.chapter1_2.ep.service;

import com.chapter1_2.ep.model.Item;
import com.chapter1_2.ep.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class LogTestService {

    private final ItemRepository itemRepository;

    public Mono getPriceLogging(String id){

        log.info("test start..");
        return itemRepository.findById(id)
                .log("find by id")
                .map(item -> item.getPrice());
    }

}
