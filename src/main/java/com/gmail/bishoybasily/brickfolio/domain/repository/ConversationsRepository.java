package com.gmail.bishoybasily.brickfolio.domain.repository;

import static com.gmail.bishoybasily.brickfolio.constants.Constants.Entities.CONVERSATIONS;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import com.gmail.bishoybasily.brickfolio.domain.entity.Conversation;

public interface ConversationsRepository extends JpaRepository<Conversation, String>, JpaSpecificationExecutor<Conversation> {

  @Query("select c.id from " + CONVERSATIONS + " c")
  List<String> findAllIds();

}
