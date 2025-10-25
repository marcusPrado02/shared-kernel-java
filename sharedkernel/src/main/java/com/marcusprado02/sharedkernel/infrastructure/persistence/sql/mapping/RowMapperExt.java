package com.marcusprado02.sharedkernel.infrastructure.persistence.sql.mapping;

import java.sql.ResultSet;
public interface RowMapperExt<E> { 
    E map(ResultSet rs) throws Exception; 
}
