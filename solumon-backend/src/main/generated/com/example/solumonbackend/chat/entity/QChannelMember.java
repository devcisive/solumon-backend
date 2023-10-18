package com.example.solumonbackend.chat.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QChannelMember is a Querydsl query type for ChannelMember
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QChannelMember extends EntityPathBase<ChannelMember> {

    private static final long serialVersionUID = -843892168L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QChannelMember channelMember = new QChannelMember("channelMember");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final com.example.solumonbackend.member.entity.QMember member;

    public final com.example.solumonbackend.post.entity.QPost post;

    public QChannelMember(String variable) {
        this(ChannelMember.class, forVariable(variable), INITS);
    }

    public QChannelMember(Path<? extends ChannelMember> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QChannelMember(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QChannelMember(PathMetadata metadata, PathInits inits) {
        this(ChannelMember.class, metadata, inits);
    }

    public QChannelMember(Class<? extends ChannelMember> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.member = inits.isInitialized("member") ? new com.example.solumonbackend.member.entity.QMember(forProperty("member")) : null;
        this.post = inits.isInitialized("post") ? new com.example.solumonbackend.post.entity.QPost(forProperty("post"), inits.get("post")) : null;
    }

}

