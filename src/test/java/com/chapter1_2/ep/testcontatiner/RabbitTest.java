//package com.chapter1_2.ep.testcontatiner;
//
//import com.chapter1_2.ep.model.Item;
//import com.chapter1_2.ep.repository.ItemRepository;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.context.ContextConfiguration;
//import org.springframework.test.context.DynamicPropertyRegistry;
//import org.springframework.test.context.DynamicPropertySource;
//import org.springframework.test.web.reactive.server.WebTestClient;
//import org.testcontainers.containers.RabbitMQContainer;
//import org.testcontainers.junit.jupiter.Container;
//import org.testcontainers.junit.jupiter.Testcontainers;
//import reactor.test.StepVerifier;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
//@SpringBootTest
//@AutoConfigureWebTestClient
//// JUnit5 제공하는 애너테이션
//@Testcontainers
//// 테스트 실행 전에 먼저 애플리케이션 컨텐스에 로딩
//@ContextConfiguration
//public class RabbitTest {
//
//    // 테스트에 사용할 RabbitMQContainer를 생성
//    // RabbitMQContainer는 테스트에 사용할 래빗엠큐 인스턴스를 관리한다.
//    @Container static RabbitMQContainer container = new RabbitMQContainer("rabbitmq:3.7.25-management-alpine");
//
//    @Autowired WebTestClient webTestClient;
//    @Autowired ItemRepository itemRepository;
//
//    // 자바 8의 함수형 인터페이스인 Supplier를 사용해서 환경설정 내용을 Enviroment에 동적으로 추가
//    // container::getContainerIpAddress와 container::getAmqpPort 메소드 핸들을 사용해서
//    // 테스트컨테이너에서 실행한 래빗엠큐 브로커의 호스트 이름과 포트 번호를 가져온다.
//    // 래빗앰큐 연결 세부정보를 테스트 컨테이너에서 읽어와서 스프링 AMQP에서 사용할 수 있도록 스프링 부트 환경설정 정보에 저장.
//    @DynamicPropertySource
//    static void configure(DynamicPropertyRegistry registry) {
//        registry.add("spring.rabbitmq.host",container::getContainerIpAddress);
//        registry.add("spring.rabbitmq.port",container::getAmqpPort);
//    }
//
//    @Test
//    void verifyMessagingThroughAmqp() throws InterruptedException{
//        // 새 item 데이터를 /items에 POST로 요청한다. 요청에 대한 응답으로 HTTP 201 Created 상태 코드가 반환
//       this.webTestClient.post().uri("/items")
//            .bodyValue(new Item("Alf alarm clock","nothing important",19.99))
//            .exchange()
//            .expectStatus().isCreated()
//            .expectBody();
//
//       // 메시지 브로커를 거쳐 데이터 저장소에 저장될 때 까지 기다린다.
//       Thread.sleep(1500L);
//
//       // 2번째 POST 요청
//       this.webTestClient.post().uri("/items")
//               .bodyValue(new Item("Smurf TV tray","nothing important",29.99))
//               .exchange()
//               .expectStatus().isCreated()
//               .expectBody();
//
//       // 기다린다.
//       Thread.sleep(2000L);
//
//       // 2개의 item이 저장되었는지 확인한다.
//       this.itemRepository.findAll()
//               .as(StepVerifier::create)
//               .expectNextMatches(item -> {
//                   assertThat(item.getName()).isEqualTo("Alf alarm clock");
//                   assertThat(item.getDescription()).isEqualTo("nothing important");
//                   assertThat(item.getPrice()).isEqualTo(19.99);
//                   return true;
//               })
//                .expectNextMatches(item -> {
//                    assertThat(item.getName()).isEqualTo("Smurf TV tray");
//                    assertThat(item.getDescription()).isEqualTo("nothing important");
//                    assertThat(item.getPrice()).isEqualTo(29.99);
//                    return true;
//                })
//                .verifyComplete();
//
//    }
//
//}