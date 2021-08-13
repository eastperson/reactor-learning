//package com.chapter1_2.ep.service;
//
//import com.chapter1_2.ep.model.Item;
//import com.chapter1_2.ep.repository.ItemRepository;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.amqp.rabbit.annotation.Exchange;
//import org.springframework.amqp.rabbit.annotation.Queue;
//import org.springframework.amqp.rabbit.annotation.QueueBinding;
//import org.springframework.amqp.rabbit.annotation.RabbitListener;
//import org.springframework.stereotype.Service;
//import reactor.core.publisher.Mono;
//
//@Service
//public class SpringAmqpItemService {
//
//    private static final Logger log = LoggerFactory.getLogger(SpringAmqpItemService.class);
//
//    private final ItemRepository itemRepository;
//
//    public SpringAmqpItemService(ItemRepository itemRepository) {
//        this.itemRepository = itemRepository;
//    }
//
//    // 스프링 AMQP 메시지 리스너로 등록되어 메시지를 소비 가능
//    @RabbitListener(ackMode = "MANUAL",
//            // 큐를 익스체인지에 바인딩하는 방법을 지정
//            bindings = @QueueBinding(
//                // 임의의 지속성 없는 익명 큐를 생성.
//                // @Queue의 인자로 큐의 이름을 지정. durable, exlusive, autoDelete 속성 지정 가능
//               value = @Queue,
//                // @Exchange는 이 큐와 연결될 익스체인지를 지정.
//                // 예제에서는 hacking-spring-boot 익스체인지를 큐와 연결
//                // 익스체인지의 다른 속성값을 설정
//                exchange = @Exchange("hacking-spring-boot"),
//                // 라우팅 키를 지정
//                key = "new-items-spring-amqp"))
//    public Mono<Void> processNewItemsViaSpringAmqp(Item item) {
//        log.debug("Consuming => " + item);
//        return this.itemRepository.save(item).then();
//    }
//
//}
