package com.undina.messenger.repository;


import com.undina.messenger.model.Message;
import com.undina.messenger.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, String> {
    List<Message> findAllByAuthorInAndRecipientInOrderByCreatedDesc(List<User> authors, List<User> recipients);
}
