package com.marcusprado02.sharedkernel.infrastructure.persistence.document.example.impl;

import java.util.Optional;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Repository;

import com.marcusprado02.sharedkernel.infrastructure.persistence.criteria.Criteria;
import com.marcusprado02.sharedkernel.infrastructure.persistence.criteria.Filter;
import com.marcusprado02.sharedkernel.infrastructure.persistence.criteria.Op;
import com.marcusprado02.sharedkernel.infrastructure.persistence.document.BaseDocumentRepository;
import com.marcusprado02.sharedkernel.infrastructure.persistence.sql.example.impl.UserAccount;
import com.marcusprado02.sharedkernel.infrastructure.tenancy.TenantProvider;

@Repository
public class UserAccountDocumentRepository
    extends BaseDocumentRepository<UserAccount, String> {

  public UserAccountDocumentRepository(MongoTemplate mongo, TenantProvider tenantProvider) {
    super(mongo, UserAccount.class, "user_account", tenantProvider, true);
  }

  public Optional<UserAccount> findByEmail(String email){
    var c = Criteria.of(new Filter("email", Op.EQ, email));
    return findAll(c).stream().findFirst();
  }
}
