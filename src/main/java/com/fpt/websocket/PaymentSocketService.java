package com.fpt.websocket;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class PaymentSocketService {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    public void notifyOrderStatus(Integer orderId, String status) {
        // Gửi WebSocket đến client đang lắng nghe kênh này
        messagingTemplate.convertAndSend("/topic/payment/" + orderId, status);
    }
}
