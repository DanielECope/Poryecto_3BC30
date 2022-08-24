package pe.com.nttdata.Contrato.service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface ICRUD<T, V> {
    Mono<T> update(T obj) throws Exception;
    Flux<T> findAll() throws Exception;
    Mono<T> findById(V id) throws Exception;
    Mono<Void> delete(V id) ;
}
