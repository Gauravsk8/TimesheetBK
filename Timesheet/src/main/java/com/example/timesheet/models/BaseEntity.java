package com.example.timesheet.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.NaturalId;
import org.hibernate.annotations.ParamDef;
import org.hibernate.envers.Audited;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.io.Serial;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Objects;
import java.util.UUID;


@Getter
@Setter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@FilterDef(name = "tenantFilter", parameters = {@ParamDef(name = "tenant_id", type = String.class)})
@Filter(name = "tenantFilter", condition = "tenant_id = :tenantId")
@FilterDef(name = "deletedFilter", parameters = @ParamDef(name = "deleted", type = Boolean.class))
@Filter(name = "deletedFilter", condition = "deleted = :deleted")
@Audited
public class BaseEntity implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "id", updatable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Long id;

    @NaturalId
    @Column(name = "natural_id", nullable = false, updatable = false)
    @JsonIgnore
    private String naturalId = UUID.randomUUID().toString();

    @Column(name = "deleted", nullable = false, columnDefinition = "BOOLEAN")
    private boolean deleted;

    @Column(name = "edited_at")
    @LastModifiedDate
    private Timestamp editedAt;

    @Column(name = "created_at")
    @CreatedDate
    private Timestamp createdAt;

    @Column(name = "edited_by")
    @LastModifiedBy
    @JsonIgnore
    private String editedBy;

    @Column(name = "created_by")
    @CreatedBy
    @JsonIgnore
    private String createdBy;

    @Column(name = "tenant_id", nullable = false)
    @JsonIgnore
    private String tenantId;


    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o instanceof BaseEntity entity) {
            return Objects.equals(getNaturalId(), entity.getNaturalId());
        }
        return false;
    }


    @Override
    public final int hashCode() {
        return Objects.hash(getNaturalId());
    }

}
