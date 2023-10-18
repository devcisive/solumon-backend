package com.example.solumonbackend.notify.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QNotify is a Querydsl query type for Notify
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QNotify extends EntityPathBase<Notify> {

    private static final long serialVersionUID = -829485793L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QNotify notify = new QNotify("notify");

    public final BooleanPath isRead = createBoolean("isRead");

    public final com.example.solumonbackend.member.entity.QMember member;

    public final NumberPath<Long> notiId = createNumber("notiId", Long.class);

    public final NumberPath<Long> postId = createNumber("postId", Long.class);

    public final StringPath postTitle = createString("postTitle");

    public final DateTimePath<java.time.LocalDateTime> sentAt = createDateTime("sentAt", java.time.LocalDateTime.class);

    public final EnumPath<com.example.solumonbackend.notify.type.NotifyType> type = createEnum("type", com.example.solumonbackend.notify.type.NotifyType.class);

    public QNotify(String variable) {
        this(Notify.class, forVariable(variable), INITS);
    }

    public QNotify(Path<? extends Notify> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QNotify(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QNotify(PathMetadata metadata, PathInits inits) {
        this(Notify.class, metadata, inits);
    }

    public QNotify(Class<? extends Notify> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.member = inits.isInitialized("member") ? new com.example.solumonbackend.member.entity.QMember(forProperty("member")) : null;
    }

}

