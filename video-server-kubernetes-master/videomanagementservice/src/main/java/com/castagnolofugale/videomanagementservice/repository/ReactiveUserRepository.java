package com.castagnolofugale.videomanagementservice.repository;

import com.castagnolofugale.videomanagementservice.model.User;
import org.bson.types.ObjectId;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface ReactiveUserRepository extends ReactiveCrudRepository<User, ObjectId> {
    Mono<User> findByUsername(String username);
}
