package ru.mail.park.services;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by devil on 02.10.16.
 */
@Service
public class SessionService {
    private Map<String, String> sessionToLogin = new HashMap<>();

    public void addSession(String sessionId, String login) {
        sessionToLogin.put(sessionId, login);
    }

    public void deleteSession(String sessionId) {
        if (checkExists(sessionId)) sessionToLogin.remove(sessionId);
    }

    public String returnLogin(String sessionId) {
        return sessionToLogin.get(sessionId);
    }

    public boolean checkExists(String sessionId) {
        return sessionToLogin.containsKey(sessionId);
    }

}
