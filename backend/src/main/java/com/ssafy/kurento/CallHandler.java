package com.ssafy.kurento;

import com.google.gson.*;
import org.kurento.client.IceCandidate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;

public class CallHandler extends TextWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(CallHandler.class);

    private static final Gson gson = new GsonBuilder().create();

    @Autowired
    private RoomManager roomManager;

    @Autowired
    private UserRegistry registry;

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        final JsonObject jsonMessage = gson.fromJson(message.getPayload(), JsonObject.class);

        final UserSession user = registry.getBySession(session);

        if (user != null) {
            log.debug("Incoming message from user '{}': {}", user.getName(), jsonMessage);
        } else {
            log.debug("Incoming message from new user: {}", jsonMessage);
        }

        switch (jsonMessage.get("id").getAsString()) {
            case "joinRoom":
                joinRoom(jsonMessage, session);
                break;
            case "receiveVideoFrom":
                final String senderName = jsonMessage.get("sender").getAsString();
                final UserSession sender = registry.getByName(senderName);
                final String sdpOffer = jsonMessage.get("sdpOffer").getAsString();
                user.receiveVideoFrom(sender, sdpOffer);
                break;
            case "leaveRoom":
                leaveRoom(user);
                break;
            case "sendChat":
                sendChat(jsonMessage);
                break;
            case "onIceCandidate":
                JsonObject candidate = jsonMessage.get("candidate").getAsJsonObject();

                if (user != null) {
                    IceCandidate cand = new IceCandidate(candidate.get("candidate").getAsString(),
                            candidate.get("sdpMid").getAsString(), candidate.get("sdpMLineIndex").getAsInt());
                    user.addCandidate(cand, jsonMessage.get("name").getAsString());
                }
                break;
            default:
                break;
        }
    }

    private void sendChat(JsonObject params) throws IOException {
        final String roomName = params.get("room").getAsString();
        final String name = params.get("name").getAsString();
        final String chatting = params.get("chat").getAsString();;
        final String userID = params.get("userId").getAsString();;

        Room room = roomManager.getRoom(roomName);
        for (final UserSession participant : room.getParticipants()) {

            final JsonElement chat = new JsonPrimitive(chatting);
            final JsonElement sendName = new JsonPrimitive(name);
            final JsonElement sendUserId = new JsonPrimitive(userID);

            final JsonObject existingParticipantsMsg = new JsonObject();
            existingParticipantsMsg.addProperty("id", "receiveChat");
            existingParticipantsMsg.add("chat", chat);
            existingParticipantsMsg.add("name", sendName);
            existingParticipantsMsg.add("userId", sendUserId);
            participant.sendMessage(existingParticipantsMsg);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        log.info("afterConnectionClosed : {}",  session);
        UserSession user = registry.removeBySession(session);
        roomManager.getRoom(user.getRoomName()).leave(user);
    }

    private void joinRoom(JsonObject params, WebSocketSession session) throws IOException {
        log.info("joinRoom : {session}",  session);
        log.info("joinRoom : {params}",  params);
        final String roomName = params.get("room").getAsString();
        final String name = params.get("name").getAsString();
        final String image = params.get("image").getAsString();;
        log.info("PARTICIPANT {}: trying to join room {}", name, roomName);

        Room room = roomManager.getRoom(roomName);
        final UserSession user = room.join(image, name, session);
        registry.register(user);
    }

    private void leaveRoom(UserSession user) throws IOException {
        log.info("leaveRoom : {params}",  user);
        final Room room = roomManager.getRoom(user.getRoomName());
        room.leave(user);
        if (room.getParticipants().isEmpty()) {
            roomManager.removeRoom(room);
        }
    }
}
