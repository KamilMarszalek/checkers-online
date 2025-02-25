package pw.checkers.sockets;

import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;
import pw.checkers.data.enums.Color;
import pw.checkers.message.User;
import pw.checkers.utils.WaitingPlayer;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

@Service
public class SessionManager {
    private final Map<String, Set<WebSocketSession>> sessionsByGame = new ConcurrentHashMap<>();
    private final Map<String, Map<WebSocketSession, String>> colorAssignmentsByGame = new ConcurrentHashMap<>();
    private final Map<WebSocketSession, User> usersBySessions = new ConcurrentHashMap<>();
    private final Queue<WaitingPlayer> waitingQueue = new ConcurrentLinkedQueue<>();

    public void removeWaitingPlayerFromQueue(WebSocketSession session, User user) {
        waitingQueue.removeIf(waitingPlayer -> waitingPlayer.session().equals(session) && waitingPlayer.user().equals(user));
    }

    public Optional<WebSocketSession> getOpponent(String gameId, WebSocketSession session) throws IOException {
        Set<WebSocketSession> sessions = sessionsByGame.get(gameId);
        if (sessions == null || sessions.isEmpty()) {
            return Optional.empty();
        }
        return sessions.stream()
                .filter(s -> !s.equals(session))
                .findFirst();
    }

    public void removeGameFromMaps(String gameId) {
        colorAssignmentsByGame.remove(gameId);
        sessionsByGame.remove(gameId);
    }

    public Set<WebSocketSession> getSessionsByGameId(String gameId){
        return sessionsByGame.get(gameId);
    }

    public void addToSessionsByGame (String newGameId, WebSocketSession session1, WebSocketSession session2) {
        sessionsByGame.putIfAbsent(newGameId, ConcurrentHashMap.newKeySet());
        sessionsByGame.get(newGameId).add(session1);
        sessionsByGame.get(newGameId).add(session2);
    }

    public void addToColorAssignments (String newGameId, WebSocketSession white, WebSocketSession black) {
        colorAssignmentsByGame.putIfAbsent(newGameId, new ConcurrentHashMap<>());
        colorAssignmentsByGame.get(newGameId).put(white, Color.WHITE.getValue());
        colorAssignmentsByGame.get(newGameId).put(black, Color.BLACK.getValue());
    }

    public void addToUserBySessions(WebSocketSession session1, User user1, WebSocketSession session2, User user2) {
        usersBySessions.put(session1, user1);
        usersBySessions.put(session2, user2);
    }

    public Map<WebSocketSession, String> getColorAssignments(String gameId) {
        return colorAssignmentsByGame.get(gameId);
    }

    public Map<String, WebSocketSession> getSessionByColorMap(String gameId) {
        return getColorAssignments(gameId).entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
    }

    public User getUserBySession(WebSocketSession session) {
        return usersBySessions.get(session);
    }

    public WaitingPlayer pollFromPlayerQueue() {
        return waitingQueue.poll();
    }

    public void addPlayerToQueue(WebSocketSession session, User user) {
        waitingQueue.add(new WaitingPlayer(session, user));
    }

    public boolean isGameIdInvalid(String gameId) {
        return !sessionsByGame.containsKey(gameId);
    }

    public String getAssignedColorByGameIdAndSession(String gameId, WebSocketSession session) {
        return colorAssignmentsByGame.get(gameId).get(session);
    }

    public void handleSessionClose(WebSocketSession session) {
        waitingQueue.removeIf(waitingPlayer -> waitingPlayer.session().equals(session));
        sessionsByGame.entrySet().removeIf(entry -> entry.getValue().contains(session));
        colorAssignmentsByGame.entrySet().removeIf(entry -> entry.getValue().containsKey(session));
    }

    public void removeUsersBySessionEntry(WebSocketSession session) {
        usersBySessions.remove(session);
    }
}
