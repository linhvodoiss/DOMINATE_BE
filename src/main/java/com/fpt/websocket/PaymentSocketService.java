package com.fpt.websocket;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class PaymentSocketService {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    public void notifyOrderStatus(Integer orderId, String status) {
        messagingTemplate.convertAndSend("/topic/payment/" + orderId, status);
        messagingTemplate.convertAndSend("/topic/payment/global", Map.of(
                "orderId", orderId,
                "newStatus", status
        ));
    }
    public void notifyAdminStatus(Integer orderId, String status) {
        messagingTemplate.convertAndSend("/topic/payment/" + orderId, status);
    }
    public void notifyNewOrder(Integer orderId, String userName, String createdAt, String packageName, Float price, String paymentMethod) {
        messagingTemplate.convertAndSend(
                "/topic/order/global",
                Map.of(
                        "orderId", orderId,
                        "userName", userName,
                        "createdAt", createdAt,
                        "packageName", packageName,
                        "price", price,
                        "paymentMethod", paymentMethod
                )
        );
    }
    public void notifyOrderReport(Integer orderId, String content) {
        messagingTemplate.convertAndSend(
                "/topic/order/report",
                Map.of(
                        "orderId", orderId,
                        "content", content
                )
        );
    }

    public void notifySyncBill(Integer orderId, String content) {
        messagingTemplate.convertAndSend(
                "/topic/sync/"+ orderId, content
        );
    }

}
