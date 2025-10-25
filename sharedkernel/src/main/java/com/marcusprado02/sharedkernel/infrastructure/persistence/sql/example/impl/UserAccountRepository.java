package com.marcusprado02.sharedkernel.infrastructure.persistence.sql.example.impl;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;

import com.marcusprado02.sharedkernel.infrastructure.persistence.criteria.Criteria;
import com.marcusprado02.sharedkernel.infrastructure.persistence.criteria.Filter;
import com.marcusprado02.sharedkernel.infrastructure.persistence.criteria.Op;
import com.marcusprado02.sharedkernel.infrastructure.persistence.sql.BaseSqlRepository;
import com.marcusprado02.sharedkernel.infrastructure.persistence.sql.dialect.PostgresDialect;

import java.sql.ResultSet;
import java.util.*;

public class UserAccountRepository extends BaseSqlRepository<UserAccount, String> {

  public UserAccountRepository(NamedParameterJdbcTemplate jdbc, TransactionTemplate tx) {
    super(jdbc, tx,
      new PostgresDialect(),
      "user_account", "id", "version",
      true, Optional.of("deleted_at"), Optional.of("tenant_id"),
      UserAccountRepository::map, UserAccountRepository::unmap,
      Set.of("email","full_name","active","tenant_id") // updatable
    );
  }

  private static UserAccount map(ResultSet rs) throws Exception {
    return new UserAccount(
      rs.getString("id"),
      rs.getString("tenant_id"),
      rs.getString("email"),
      rs.getString("full_name"),
      rs.getBoolean("active"),
      rs.getLong("version")
    );
  }

  private static Map<String,Object> unmap(UserAccount u){
    var m = new LinkedHashMap<String,Object>();
    m.put("id", u.id());
    m.put("tenant_id", u.tenantId());
    m.put("email", u.email());
    m.put("full_name", u.fullName());
    m.put("active", u.active());
    m.put("version", u.version());
    return m;
  }

  @Override
  protected Object extractSortValue(UserAccount entity, String field) {
    return switch (field) {
      case "email" -> entity.email();
      case "id"    -> entity.id();
      default -> throw new IllegalArgumentException("Unsupported sort field: "+field);
    };
  }

  // queries de alto nível (exemplos)
  public Optional<UserAccount> findByEmail(String email){
    var c = Criteria.of(new Filter("email", Op.EQ, email));
    return findAll(c).stream().findFirst();
  }
}
