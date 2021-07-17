package com.example.ep.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import reactor.core.publisher.Mono;

@Controller
public class HomeController {

    // Mono는 0 또는 1개의 원소만 담을 수 있는 리액티브 publisher로서 프로젝트 리액터에서 제공해주는 구현체이다.
    // 하나의 원소만 비동기적으로 반환하는 경우가 압도적으로 많아 추가한 객체이다.
    // Mono는 함수형 프로그래밍 무기로 무장한 Future라고 생각해도 된다.
    // 리액티브 스트림은 배압과 지연을 지원한다.
    @GetMapping
    Mono<String> home() {
        return Mono.just("home");
    }

}
