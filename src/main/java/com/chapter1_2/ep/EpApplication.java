package com.chapter1_2.ep;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.thymeleaf.TemplateEngine;
import reactor.blockhound.BlockHound;

@SpringBootApplication
public class EpApplication {

    public static void main(String[] args) {
        // 블록하운드 등록
        // 네이티브 메소드를 호출까지 가로채서 블로킹 코드를 검출하고 익셉션을 발생시킨다.
        // 에러코드를 추적해서 블로킹 코드를 제거할 수 있다.
        // 허용목록을 만들어서 특정 메소드의 사용을 허용해줄 수 있다.
        BlockHound.builder()
                .allowBlockingCallsInside(
                        TemplateEngine.class.getCanonicalName(),"process"
                )
                .install();
        SpringApplication.run(EpApplication.class, args);
    }

}
