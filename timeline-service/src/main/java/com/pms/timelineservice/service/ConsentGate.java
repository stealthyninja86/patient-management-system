package com.pms.timelineservice.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class ConsentGate {
    private final StringRedisTemplate stringRedisTemplate;


    public ConsentGate(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    public boolean hasConsent(String patientId, String hospitalId) {
        String key = "consent" + patientId + ":" + hospitalId;
        return stringRedisTemplate.hasKey(key);
    }
}
