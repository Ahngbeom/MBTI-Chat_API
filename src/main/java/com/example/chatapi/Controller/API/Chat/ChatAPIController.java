package com.example.chatapi.Controller.API.Chat;

import com.example.chatapi.DTO.ChatRoomDTO;
import com.example.chatapi.Service.ChatService;
import com.example.chatapi.Service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat")
@PreAuthorize("isAuthenticated()")
public class ChatAPIController {

    private final UserService userService;
    private final ChatService chatService;

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/listOfAll")
    public ResponseEntity<List<ChatRoomDTO>> getListOfAllChatRooms() {
        return ResponseEntity.ok(chatService.getListOfAllChatRooms());
    }

    @GetMapping("/list")
    public ResponseEntity<List<ChatRoomDTO>> getListOfAllChatRoomsUserHasJoined(Principal principal) {
        return ResponseEntity.ok(chatService.getListOfAllChatRoomsUserHasJoined(userService.getUserInfo(principal.getName()).getUsername()));
    }

    @PostMapping("/create")
    public ResponseEntity<Boolean> createChatRoom(Principal principal, @RequestBody ChatRoomDTO chatRoom) {
        log.warn(chatRoom.toString());
        return ResponseEntity.ok(chatService.createChatRoom(principal.getName(), chatRoom));
    }

    @GetMapping("/info")
    public ResponseEntity<ChatRoomDTO> getInfoChatRoom(@RequestParam String roomName, Principal principal) {
        return ResponseEntity.ok(chatService.getInfoChatRoom(roomName));
    }

    @PostMapping("/update")
    public ResponseEntity<Boolean> updateChatRoom(Principal principal, @RequestBody ChatRoomDTO chatRoomDTO) {
        log.info(chatRoomDTO.toString());
        if (!principal.getName().equals(chatService.getInfoChatRoom(chatRoomDTO.getOrigRoomName()).getFounder()))
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(false);
        chatRoomDTO.setFounder(principal.getName());
        chatService.updateChatRoom(chatRoomDTO);
        return ResponseEntity.ok(true);
    }

    @GetMapping("/remove")
    public ResponseEntity<Boolean> removeChatRoom(Principal principal, @RequestParam(value = "roomName") String roomName) {
        log.info(roomName);
        if (principal.getName().equals(chatService.getInfoChatRoom(roomName).getFounder()))
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(false);
        chatService.removeChatRoom(roomName);
        return ResponseEntity.ok(true);
    }

    @GetMapping("/join-availability/{roomName}")
    public ResponseEntity<String> joinChatRoomAvailability(@PathVariable String roomName, Principal principal) {
        if (!chatService.checkAlreadyJoined(roomName, principal.getName())) {
            if (!chatService.joinChatRoomAvailability(roomName, principal.getName())) {
                return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body("Your MBTI code does not allow access to this chat room.");
            }
        }
        return ResponseEntity.ok("Success");
    }

    @GetMapping("/leave-chatroom/{roomName}")
    public ResponseEntity<String> leaveChatRoom(@PathVariable String roomName, Principal principal) {
        if (chatService.leaveChatRoom(roomName, principal.getName())) {
            return ResponseEntity.ok(null);
        }
        return ResponseEntity.unprocessableEntity().body("You need to transfer the founder privileges to other users.");
    }

}
