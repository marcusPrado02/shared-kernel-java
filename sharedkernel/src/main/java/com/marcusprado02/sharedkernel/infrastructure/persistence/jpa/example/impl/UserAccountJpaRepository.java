package com.marcusprado02.sharedkernel.infrastructure.persistence.jpa.example.impl;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.marcusprado02.sharedkernel.infrastructure.persistence.criteria.Criteria;
import com.marcusprado02.sharedkernel.infrastructure.persistence.criteria.Filter;
import com.marcusprado02.sharedkernel.infrastructure.persistence.criteria.Op;
import com.marcusprado02.sharedkernel.infrastructure.persistence.jpa.BaseJpaRepository;
import com.marcusprado02.sharedkernel.infrastructure.persistence.sql.example.impl.UserAccount;
import com.marcusprado02.sharedkernel.infrastructure.tenancy.TenantProvider;

import jakarta.transaction.Transactional;

@Repository
@Transactional
public class UserAccountJpaRepository
    extends BaseJpaRepository<UserAccount, String> {

  public UserAccountJpaRepository(TenantProvider tenantProvider) {
    super(UserAccount.class, tenantProvider);
  }

  public Optional<UserAccount> findByEmail(String email){
    var c = Criteria.of(new Filter("email", Op.EQ, email));
    return findAll(c).stream().findFirst();
  }
}
