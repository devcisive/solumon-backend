package com.example.solumonbackend.member.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QMember is a Querydsl query type for Member
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QMember extends EntityPathBase<Member> {

    private static final long serialVersionUID = -255045119L;

    public static final QMember member = new QMember("member1");

    public final DateTimePath<java.time.LocalDateTime> bannedAt = createDateTime("bannedAt", java.time.LocalDateTime.class);

    public final StringPath email = createString("email");

    public final BooleanPath isFirstLogIn = createBoolean("isFirstLogIn");

    public final NumberPath<Long> kakaoId = createNumber("kakaoId", Long.class);

    public final NumberPath<Long> memberId = createNumber("memberId", Long.class);

    public final DateTimePath<java.time.LocalDateTime> modifiedAt = createDateTime("modifiedAt", java.time.LocalDateTime.class);

    public final StringPath nickname = createString("nickname");

    public final StringPath password = createString("password");

    public final DateTimePath<java.time.LocalDateTime> registeredAt = createDateTime("registeredAt", java.time.LocalDateTime.class);

    public final EnumPath<com.example.solumonbackend.member.type.MemberRole> role = createEnum("role", com.example.solumonbackend.member.type.MemberRole.class);

    public final DateTimePath<java.time.LocalDateTime> unregisteredAt = createDateTime("unregisteredAt", java.time.LocalDateTime.class);

    public QMember(String variable) {
        super(Member.class, forVariable(variable));
    }

    public QMember(Path<? extends Member> path) {
        super(path.getType(), path.getMetadata());
    }

    public QMember(PathMetadata metadata) {
        super(Member.class, metadata);
    }

}

