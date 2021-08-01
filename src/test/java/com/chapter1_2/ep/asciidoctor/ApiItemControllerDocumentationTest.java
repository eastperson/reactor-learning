package com.chapter1_2.ep.asciidoctor;

import com.chapter1_2.ep.controller.ApiItemController;
import com.chapter1_2.ep.model.Item;
import com.chapter1_2.ep.repository.ItemRepository;
import com.chapter1_2.ep.service.ItemService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.webtestclient.WebTestClientRestDocumentation.document;

// ApiItemController.class 이친구만 집중적으로 테스트한다.
// WebFlux 컨트롤러 테스트에 필용한 내용만 자동 설정
@WebFluxTest(controllers = ApiItemController.class)
// 스프링 레스트 독 사용에 필요한 내용을 자동으로 설정
@AutoConfigureRestDocs
public class ApiItemControllerDocumentationTest {

    @Autowired private WebTestClient webTestClient;
    // WebFlux 테스트는 웹플럭스 관련 빈만 주입하기 때문에 MockBean으로 넣어줘야 한다.
    @MockBean ItemService itemService;
    @MockBean ItemRepository itemRepository;


    @Test
    void findingAllItems(){
        // 특정 item을 반환하도록 지정
        when(itemRepository.findAll()).thenReturn(
                Flux.just(new Item("item-1","Alf alarm clock","nothing I really need",19.99)));

        // get 요청을 테스트
        this.webTestClient.get().uri("/api/items")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                // document는 스프링 레스트 독 정적 메소드이며 문서 생성ㅡㅇㄹ 한다.
                // 문서는 target/generated-snippets/findAll
                // document()는 인자를 두개를 받는다. 첫 번째 인자의 문자열로 디렉토리를 생성하고 그 안에 여러 .adoc를 생성한다.
                // 두번째 인자는 요청 결과로 반환되는 JSON 문자열을 보기 편한 형태로 출력해준다.
                .consumeWith(document("findAll",preprocessResponse(prettyPrint())));
    }

    @Test
    void postNewItem() {
        when(itemRepository.save(any())).thenReturn(
                Mono.just(new Item("1","Alf alarm clock","nothing important",19.99)));

        this.webTestClient.post().uri("/api/items")
                .bodyValue(new Item("Alf alarm clock","nothing important",19.99))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .consumeWith(document("post-new-item",preprocessResponse(prettyPrint())));
    }
}
