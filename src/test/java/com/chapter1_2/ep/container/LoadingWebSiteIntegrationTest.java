package com.chapter1_2.ep.container;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.AutoConfigureWebClient;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.TEXT_HTML;

// 실제 애플리케이션을 구동하게 만든다.
// @SpringBootApplication 을 임의로 찾아서 내장 컨테이너를 실행시킨다.
// WebEnvironment.RANDOM_PORT는 임의의 포트에 내장 컨테이너를 바인딩한다.
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
// 애플리케이션에 요청을 날리는 WebTestClient를 생성한다.
@AutoConfigureWebTestClient
public class LoadingWebSiteIntegrationTest {

    @Autowired WebTestClient client;

    @Test
    void test() {
        client.get().uri("/").exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(TEXT_HTML)
                .expectBody(String.class)
                .consumeWith(exchangeResult -> {
                    assertThat(exchangeResult.getResponseBody()).contains("value=\"delete\"");
                });
    }

}
