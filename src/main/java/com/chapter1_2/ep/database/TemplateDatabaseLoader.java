package com.chapter1_2.ep.database;

import com.chapter1_2.ep.model.Item;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.stereotype.Component;

@Component
public class TemplateDatabaseLoader {

    @Bean
    CommandLineRunner initialize(MongoOperations mongo) {
        return args -> {
            mongo.save(new Item("Alf alarm clock","description",19.99));
            mongo.save(new Item("Smurf TV tray","description",24.99));
        };
    }

}
