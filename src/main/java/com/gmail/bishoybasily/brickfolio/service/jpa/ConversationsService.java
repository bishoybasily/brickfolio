package com.gmail.bishoybasily.brickfolio.service.jpa;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import com.gmail.bishoybasily.brickfolio.domain.entity.Conversation;
import com.gmail.bishoybasily.brickfolio.domain.entity.ConversationMessage;
import com.gmail.bishoybasily.brickfolio.domain.parmas.ConversationParams;
import com.gmail.bishoybasily.brickfolio.domain.repository.ConversationsRepository;

@Service
@RequiredArgsConstructor
public class ConversationsService implements _JpaService<Conversation, String, ConversationParams> {

  private final ConversationsRepository repository;

  @Getter
  private final TransactionTemplate writeTransactionTemplate;

  public List<String> ids() {
    return repository.findAllIds();
  }

  public Conversation replaceMessages(String id, List<ConversationMessage> messages) {
    return writeTransactionTemplate.execute(status -> {
      final var conversation = repository.findById(id).orElseGet(() -> new Conversation().setId(id));
      // mutate the managed collection: callers may pass an immutable list, which Hibernate can't merge into
      conversation.getMessages().clear();
      conversation.getMessages().addAll(messages);
      return repository.save(conversation);
    });
  }

  @Override
  public JpaRepository<Conversation, String> getJpaRepository() {
    return repository;
  }

  @Override
  public JpaSpecificationExecutor<Conversation> getJpaSpecificationExecutor() {
    return repository;
  }

}
