package com.gmail.bishoybasily.brickfolio.domain.entity;

import static com.gmail.bishoybasily.brickfolio.constants.Constants.Columns.CONVERSATION_ID;
import static com.gmail.bishoybasily.brickfolio.constants.Constants.Entities.CONVERSATIONS;
import static com.gmail.bishoybasily.brickfolio.constants.Constants.Tables.CONVERSATION_MESSAGES;
import static jakarta.persistence.FetchType.EAGER;

import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.OrderColumn;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Accessors(chain = true)
@Entity(name = CONVERSATIONS)
public class Conversation implements _Entity<Conversation> {

  @Id
  private String id;

  @ElementCollection(fetch = EAGER)
  @JoinTable(name = CONVERSATION_MESSAGES, joinColumns = @JoinColumn(name = CONVERSATION_ID))
  @OrderColumn(name = "message_order")
  private List<ConversationMessage> messages = new ArrayList<>();

  @Override
  public Conversation update(Conversation conversation) {
    this.setMessages(conversation.getMessages());
    return this;
  }

}
