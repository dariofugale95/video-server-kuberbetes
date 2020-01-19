package com.castagnolofugale.videomanagementservice.repository;

import com.castagnolofugale.videomanagementservice.model.VideoInformation;
import org.bson.types.ObjectId;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface ReactiveVideoInformationRepository extends ReactiveCrudRepository<VideoInformation, ObjectId> {
}
