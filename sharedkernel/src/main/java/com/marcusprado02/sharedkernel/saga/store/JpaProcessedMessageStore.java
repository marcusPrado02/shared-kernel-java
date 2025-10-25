package com.marcusprado02.sharedkernel.saga.store;

import java.time.OffsetDateTime;

import com.marcusprado02.sharedkernel.saga.ProcessedMessageStore;
import com.marcusprado02.sharedkernel.saga.model.ProcessedMessageEntity;

import jakarta.persistence.EntityManager;

public class JpaProcessedMessageStore implements ProcessedMessageStore {
  private final EntityManager em;

  public JpaProcessedMessageStore(EntityManager em) {
    this.em = em;
  }

  public boolean seen(String messageId, String consumer){
    var q = em.createQuery("select count(p) from ProcessedMessageEntity p where p.messageId=:m and p.consumer=:c", Long.class);
    var n = q.setParameter("m", messageId).setParameter("c", consumer).getSingleResult();
    return n > 0;
  }
  public void mark(String messageId, String consumer){
    var p = new ProcessedMessageEntity(); 
    p.messageId=messageId; 
    p.consumer=consumer; 
    p.processedAt=OffsetDateTime.now(); 
    em.persist(p);
  }
}
