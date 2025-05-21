package com.example.common.audit;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QAudit is a Querydsl query type for Audit
 */
@Generated("com.querydsl.codegen.DefaultSupertypeSerializer")
public class QAudit extends EntityPathBase<Audit> {

    private static final long serialVersionUID = 1132148726L;

    public static final QAudit audit = new QAudit("audit");

    public final StringPath createdBy = createString("createdBy");

    public final DateTimePath<java.time.LocalDateTime> creationOn = createDateTime("creationOn", java.time.LocalDateTime.class);

    public final StringPath updatedBy = createString("updatedBy");

    public final DateTimePath<java.time.LocalDateTime> updatedOn = createDateTime("updatedOn", java.time.LocalDateTime.class);

    public QAudit(String variable) {
        super(Audit.class, forVariable(variable));
    }

    public QAudit(Path<? extends Audit> path) {
        super(path.getType(), path.getMetadata());
    }

    public QAudit(PathMetadata metadata) {
        super(Audit.class, metadata);
    }

}

