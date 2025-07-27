package com.marcusprado02.sharedkernel.domain.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface MongoBaseRepository<T, ID> extends MongoRepository<T, ID> {

}
