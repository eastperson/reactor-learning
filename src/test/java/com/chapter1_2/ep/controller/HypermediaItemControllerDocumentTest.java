package com.chapter1_2.ep.controller;

import com.chapter1_2.ep.model.Item;
import com.chapter1_2.ep.repository.ItemRepository;
import com.chapter1_2.ep.service.ItemService;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.mockito.Mockito.when;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.linkWithRel;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.links;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.webtestclient.WebTestClientRestDocumentation.document;

@WebFluxTest(controllers = HypermediaItemController.class)
@AutoConfigureRestDocs
public class HypermediaItemControllerDocumentTest {

    @Autowired private WebTestClient webTestClient;
    @MockBean ItemService itemService;
    @MockBean ItemRepository itemRepository;

    @Test
    void findOneItem() {
        when(itemRepository.findById("item-1")).thenReturn(Mono.just(
                new Item("item-1","Alf alarm clock","nothing I really need",19.99)));
        this.webTestClient.get().uri("/hypermedia/items/item-1")
                .exchange()
                .expectStatus().isOk()
                .expectBody().consumeWith(document("findOne-hypermedia",preprocessResponse(prettyPrint()),
                    // 스프링 레스트 독의 HypermediaDocumentation 클래스의 links() 메소드를 호출해서 응답에 링크가 포함된 문서조각을 만든다.
                    links(
                            // Item 객체 자신을 나타내는 self 링크를 찾고 description()에 전달된 설명과 함께 문서화한다.
                            linkWithRel("self").description("이 `Item`에 대한 공식 링크"),
                            // 애그리것 루트로 연결되는 item 링크를 찾고 description()에 전달된 설명과 함께 문서화한다.
                            linkWithRel("item").description("`Item` 목록 링크"))));

    }

}
