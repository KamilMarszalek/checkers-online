package pw.checkers.utils;


import org.springframework.web.socket.WebSocketSession;
import pw.checkers.message.User;


public record WaitingPlayer(WebSocketSession session, User user) {}
