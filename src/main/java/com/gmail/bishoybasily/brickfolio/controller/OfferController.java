package com.gmail.bishoybasily.brickfolio.controller;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.SubscriptionMapping;
import org.springframework.stereotype.Controller;

import com.gmail.bishoybasily.brickfolio.provider.SchedulersProvider;

@Controller
@RequiredArgsConstructor
public class OfferController {

  private final ChatClient chatClient;
  private final SchedulersProvider schedulersProvider;

  // chat memory and the @Tool methods block on JPA, so the stream must not run on the event loop
  @SubscriptionMapping
  public Flux<String> parseOffer(@Argument String conversationId, @Argument String message) {
    return chatClient.prompt()
        .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
        .user(message)
        .stream()
        .content()
        .subscribeOn(schedulersProvider.ioScheduler());
  }

}
