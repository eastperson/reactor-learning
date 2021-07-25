package com.chapter1_2.ep.repository;

import com.chapter1_2.ep.config.HttpTraceWrapper;
import org.springframework.data.repository.Repository;

import java.util.stream.Stream;

public interface HttpTraceWrapperRepository extends Repository<HttpTraceWrapper,String> {

    Stream<HttpTraceWrapper> findAll();

    void save(HttpTraceWrapper trace);

}
