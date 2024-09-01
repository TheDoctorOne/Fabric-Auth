package com.example;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class JsonAuthHandler implements IAuthHandler {

    private static final Gson GSON = new Gson();
    private static final Type GSON_TYPE = new TypeToken<Map<String, String>>(){}.getType();

    public static final Logger LOGGER = LoggerFactory.getLogger("JsonAuthHandler");

    private Map<String, String> userPassMap = new ConcurrentHashMap<>();

    public JsonAuthHandler() {
        reloadUsers();
    }

    @Override
    public boolean userExists(String username) {
        return userPassMap.containsKey(username);
    }

    @Override
    public boolean registerUpdate(String username, String password, boolean isUpdate) {
        String hashedPw = Utils.hash(password);
        userPassMap.put(username, hashedPw);
        if(isUpdate) {
            LOGGER.info("Updated: " + username);
        } else {
            LOGGER.info("Registered: " + username);
        }
        save();
        return true;
    }

    @Override
    public boolean login(String username, String password) {
        String hashedPw = Utils.hash(password);
        String pw = userPassMap.get(username);
        if(!Objects.equals(pw, hashedPw)) {
            return false;
        }

        LOGGER.info("Logged in: " + username);
        return true;
    }

    @Override
    public boolean changePassword(String username, String oldPassword, String newPassword) {
        String hashedPw = Utils.hash(oldPassword);
        String pw = userPassMap.get(username);
        if(!Objects.equals(pw, hashedPw)) {
            return false;
        }
        return registerUpdate(username, newPassword, true);
    }

    @Override
    public synchronized boolean reloadUsers() {
        try {
            String read = Files.readString(Path.of("users.json"), StandardCharsets.UTF_8);
            Map<String, String> parsed = GSON.fromJson(read, GSON_TYPE);
            userPassMap.putAll(parsed);
            return true;
        } catch (IOException e) {
            LOGGER.error("Exception while reading user data!", e);
        }
        return false;
    }

    @Override
    public boolean save() {
        String json = GSON.toJson(userPassMap);
        try {
            Files.writeString(Path.of("users.json"), json);
            return true;
        } catch (IOException e) {
            LOGGER.error("Exception while saving!", e);
        }
        return false;
    }
}
