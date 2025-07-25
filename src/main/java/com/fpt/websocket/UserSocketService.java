package com.fpt.websocket;

import com.fpt.entity.User;
import com.fpt.entity.UserStatus;
import com.fpt.payload.UserStatusChangePayload;
import com.fpt.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;

@Service
public class UserSocketService {

    private final SimpMessagingTemplate messagingTemplate;
    private final UserRepository userRepository;

    @Autowired
    public UserSocketService(SimpMessagingTemplate messagingTemplate, UserRepository userRepository) {
        this.messagingTemplate = messagingTemplate;
        this.userRepository = userRepository;
    }

    public void sendUserStatusUpdate(Long userId) {
        User user = userRepository.findById(userId)
                .orElse(null);


        if (user != null) {
            UserStatusChangePayload payload = new UserStatusChangePayload(
                    user.getId(),
                    user.getIsActive(),
                    user.getStatus()
            );
            messagingTemplate.convertAndSend("/topic/user-status/" + userId, payload);
        } else {

            UserStatusChangePayload payload = new UserStatusChangePayload(
                    userId, false, UserStatus.NOT_ACTIVE
            );
            messagingTemplate.convertAndSend("/topic/user-status/" + userId, payload);
        }
    }


}
