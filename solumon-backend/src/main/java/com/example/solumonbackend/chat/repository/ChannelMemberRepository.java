package com.example.solumonbackend.chat.repository;

import com.example.solumonbackend.chat.entity.ChannelMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChannelMemberRepository extends JpaRepository<ChannelMember,Long> {

}
