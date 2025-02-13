package com.sky.websocket;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import org.springframework.stereotype.Component;

/**
 * WebSocket服务端，负责和客户端进行通信，定义回调方法，和前端一一对应
 */
@Component // 也是最终交给spring容器管理
@ServerEndpoint("/ws/{sid}") // 访问路径，根据路径匹配，类似于Controller
public class WebSocketServer {

    //存放会话对象，建立一个会话就相当于建立一个连接
    private static Map<String, Session> sessionMap = new HashMap();

    /**
     * 连接建立成功调用的方法
     * 建立好链接后，websocket框架会自动调用这个方法
     * @param session //会话对象
     * @param sid //客户端传过来的参数，区分不同的客户端，一般是客户端动态生成的id
     */
    @OnOpen // 加入注解后变为回调方法，和客户端一一对应
    public void onOpen(Session session, @PathParam("sid") String sid) {
        System.out.println("客户端：" + sid + "建立连接");
        //将会话对象存入map中
        sessionMap.put(sid, session);
    }

    /**
     * 收到客户端消息后调用的方法，类似controller的方法，客户端发送请求到服务端，服务端需要执行一个方法，就是这个方法
     *
     * @param message 客户端发送过来的消息
     */
    @OnMessage
    public void onMessage(String message, @PathParam("sid") String sid) {
        System.out.println("收到来自客户端：" + sid + "的信息:" + message);
    }

    /**
     * 连接关闭调用的方法
     *
     * @param sid
     */
    @OnClose
    public void onClose(@PathParam("sid") String sid) {
        System.out.println("连接断开:" + sid);
        //将会话对象从map中移除
        sessionMap.remove(sid);
    }

    /**
     * 群发，broadcast消息
     *
     * @param message
     */
    // 这里没有注解，不是回调方法，需要手动调用
    public void sendToAllClient(String message) {
        Collection<Session> sessions = sessionMap.values();
        for (Session session : sessions) {
            try {
                //服务器向客户端发送消息
                session.getBasicRemote().sendText(message);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
