//package com.chapter1_2.ep.controller;
//
//import com.chapter1_2.ep.model.Item;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.amqp.core.AmqpTemplate;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RestController;
//import reactor.core.publisher.Mono;
//import reactor.core.scheduler.Scheduler;
//import reactor.core.scheduler.Schedulers;
//
//import java.net.URI;
//
//@RestController
//public class SpringAmqpItemController {
//
//    private static final Logger log = LoggerFactory.getLogger(SpringAmqpItemController.class);
//
//    // spring-boot-starter-amqp는 스프링 AMQP를 클래스패스에 추가한다.
//    // 스프링 부트 자동설정을 통해 AmqpTemplate를 테스트에 사용할 수 있다.
//    // 래빗엠큐를 사용하므로 실제 구현체로 RabbitTemplate이 사용된다.
//    private final AmqpTemplate template;
//
//    public SpringAmqpItemController(AmqpTemplate template) {
//        this.template = template;
//    }
//
//    @PostMapping("/items")
//    Mono<ResponseEntity<?>> addNewItemUsingSpringAmqp(@RequestBody Mono<Item> item) {
//        return item
//                // AmqpTemplate는 블로킹 API를 호출하므로 바운디드 엘라스틱 스케줄러에서 관리하는 별도의 스레드에서 실행
//                .subscribeOn(Schedulers.boundedElastic())
//                .flatMap(content -> {
//                    return Mono
//                            .fromCallable(() -> {
//                                this.template.convertAndSend(
//                                        "hacking-spring-boot","new-items-spring-amqp",content);
//                                return ResponseEntity.created(URI.create("/items")).build();
//                            });
//                });
//    }
//
//}
