package com.example.ep.repository;

import com.example.ep.model.Item;
import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemByExampleRepository extends ReactiveQueryByExampleExecutor<Item> {
}
