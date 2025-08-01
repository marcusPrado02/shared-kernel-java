package com.marcusprado02.sharedkernel.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface JpaBaseRepository<T, ID> extends JpaRepository<T, ID> {

}
