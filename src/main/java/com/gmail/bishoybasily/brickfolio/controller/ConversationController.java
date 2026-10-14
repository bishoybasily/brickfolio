package com.gmail.bishoybasily.brickfolio.controller;

import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import com.gmail.bishoybasily.brickfolio.domain.entity.Conversation;
import com.gmail.bishoybasily.brickfolio.domain.parmas.ConversationParams;
import com.gmail.bishoybasily.brickfolio.service.jpa.ConversationsService;

@Controller
@RequiredArgsConstructor
public class ConversationController {

  private final ConversationsService conversationsService;

  @QueryMapping
  public List<Conversation> conversations() {
    return conversationsService.all(ConversationParams.builder().build());
  }

  @QueryMapping
  public Conversation conversation(@Argument String id) {
    return conversationsService.find(id).orElse(null);
  }

}
