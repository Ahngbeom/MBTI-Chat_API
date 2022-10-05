package com.example.chatapi.Repository;

import com.example.chatapi.Entity.Chat.ChatUserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChatUserRepository extends JpaRepository<ChatUserEntity, Long> {

    List<ChatUserEntity> findAllByChatRoom_RoomName(String roomName);

    List<ChatUserEntity> findAllByUserName_Username(String userName);

    boolean existsByChatRoom_RoomNameAndUserName_Username(String chatRoom_roomName, String userName_username);
}
