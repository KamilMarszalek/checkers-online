package pw.checkers.sockets.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.InvalidTypeIdException;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import pw.checkers.message.Message;
import pw.checkers.message.MessageAccept;
import pw.checkers.message.PromptMessage;
import pw.checkers.sockets.MessageSender;

import java.io.IOException;

import static pw.checkers.data.enums.MessageType.ERROR;

@Service
public class MessageMapper {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final MessageSender messageSender;

    public MessageMapper(MessageSender messageSender) {
        this.messageSender = messageSender;
    }

    public MessageAccept toMessageAccept(WebSocketSession session, TextMessage message) throws IOException {
        MessageAccept rawMessage = null;
        try {
            rawMessage = (MessageAccept) objectMapper.readValue(message.getPayload(), Message.class);
        } catch (InvalidTypeIdException e) {
            Message defaultMessage = new PromptMessage(ERROR.getValue(), "Unknown message type");
            messageSender.sendMessage(session, defaultMessage);
        }
        return rawMessage;
    }
}
