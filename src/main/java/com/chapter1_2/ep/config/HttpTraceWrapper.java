package com.chapter1_2.ep.config;

import org.springframework.boot.actuate.trace.http.HttpTrace;
import org.springframework.data.annotation.Id;

public class HttpTraceWrapper {

    // 인스턴스 식별자
    private @Id String id;

    // 트레이스 정보를 가지고 있는 HttpTrace
    private HttpTrace httpTrace;
    public HttpTraceWrapper(HttpTrace httpTrace) {
        this.httpTrace = httpTrace;
    }

    public HttpTrace getHttpTrace() {
        return httpTrace;
    }

}
