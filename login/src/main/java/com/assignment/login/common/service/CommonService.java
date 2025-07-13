package com.assignment.login.common.service;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class CommonService {


    private final Map<String, String> codeMap = new ConcurrentHashMap<>();

    public void put(String email, String code) {
        codeMap.put(email, code);
    }

    public String get(String email) {
        return codeMap.get(email);
    }

    public void remove(String email) {
        codeMap.remove(email);
    }
}
