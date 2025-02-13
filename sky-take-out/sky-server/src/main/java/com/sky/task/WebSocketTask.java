package com.sky.task;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.sky.websocket.WebSocketServer;

@Component
public class WebSocketTask {
    @Autowired
    private WebSocketServer webSocketServer;

    /**
     * 通过WebSocket每隔5秒向客户端发送消息
     */
    // 为了不影响主程序催单和接单提醒，先注释掉了，不然会一直语音播报
    // @Scheduled(cron = "0/5 * * * * ?")
    // public void sendMessageToClient() {
    //     webSocketServer.sendToAllClient("这是来自服务端的消息：" + DateTimeFormatter.ofPattern("HH:mm:ss").format(LocalDateTime.now()));
    // }
}
