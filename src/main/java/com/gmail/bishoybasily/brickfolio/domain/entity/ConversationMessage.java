package com.gmail.bishoybasily.brickfolio.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.time.Instant;

@Getter
@Setter
@Accessors(chain = true)
@Embeddable
public class ConversationMessage {

  private String type;

  @Column(columnDefinition = "text")
  private String content;

  private Instant timestamp;

}
