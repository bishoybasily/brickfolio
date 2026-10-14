package com.gmail.bishoybasily.brickfolio.ai;

import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.util.List;

import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Component;

import com.gmail.bishoybasily.brickfolio.domain.entity.ConversationMessage;
import com.gmail.bishoybasily.brickfolio.service.jpa.ConversationsService;

@Component
@RequiredArgsConstructor
public class ConversationChatMemory implements ChatMemoryRepository {

  private final ConversationsService conversationsService;

  @Override
  public List<String> findConversationIds() {
    return conversationsService.ids();
  }

  @Override
  public List<Message> findByConversationId(String conversationId) {
    return conversationsService.find(conversationId)
        .map(conversation -> conversation.getMessages().stream()
            .map(this::toAiMessage)
            .toList())
        .orElse(List.of());
  }

  @Override
  public void saveAll(String conversationId, List<Message> messages) {
    conversationsService.replaceMessages(conversationId, messages.stream()
        .map(this::toConversationMessage)
        .toList());
  }

  @Override
  public void deleteByConversationId(String conversationId) {
    conversationsService.delete(conversationId);
  }

  private Message toAiMessage(ConversationMessage cm) {
    return switch (cm.getType()) {
      case "user" -> new UserMessage(cm.getContent());
      case "assistant" -> new AssistantMessage(cm.getContent());
      case "system" -> new SystemMessage(cm.getContent());
      default -> new UserMessage(cm.getContent());
    };
  }

  private ConversationMessage toConversationMessage(Message message) {
    return new ConversationMessage()
        .setType(message.getMessageType().getValue())
        .setContent(message.getText() != null ? message.getText() : "")
        .setTimestamp(Instant.now());
  }

}
