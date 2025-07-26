package com.marcusprado02.sharedkernel.domain.entity;

import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.Field;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public abstract class DocumentEntity extends BaseEntity {

    @Id
    private String id;

    @Field("identifier")
    private UUID identifier;

    @Version
    private Long version;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    @Override
    public Long getId() {
        return id == null ? null : id.hashCode() * 1L;
    }

    @Override
    public void setId(Long id) {
        this.id = id == null ? null : id.toString();
    }
}
