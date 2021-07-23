package com.chapter1_2.ep.db;

import com.chapter1_2.ep.model.Item;
import com.chapter1_2.ep.repository.ItemRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;

// 스프링 부트 기능 중 스프링 데이터 몽고디비 활용에 초점을 둔 몽고디비 테스트 관련기능을 활성화하며
// @ExtendWith(SpringExtension.class)를 포함한다.
// 수행속도 차이가 꽤 크다.
@DataMongoTest
public class MongoDbSliceTest {

    @Autowired ItemRepository itemRepository;

    @Test
    void itemRepositorySavesItesm(){
        Item sampleItem = new Item("name","description",1.99);
        itemRepository.save(sampleItem)
                .as(StepVerifier::create)
                .expectNextMatches(item -> {
                    assertThat(item.getId()).isNotNull();
                    assertThat(item.getName()).isEqualTo("name");
                    assertThat(item.getDescription()).isEqualTo("description");
                    assertThat(item.getPrice()).isEqualTo(1.99);

                    return true;
                }).verifyComplete();
    }

}
