package com.mycompany.user.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycompany.user.dto.request.GeneralPageRequest;
import com.mycompany.user.dto.response.AuthorPageResponse;
import com.mycompany.user.service.AuthorRedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthorRedisServiceImpl implements AuthorRedisService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    private String generateCacheKey(GeneralPageRequest request) {
        return String.format("author_list_page:%d:size:%d", request.getPageNumber(), request.getPageSize());
    }

    @Override
    public AuthorPageResponse getAllAuthors(GeneralPageRequest request) {
        String key = generateCacheKey(request);
        String json = (String) redisTemplate.opsForValue().get(key);
        try {
            return json != null ? objectMapper.readValue(json, AuthorPageResponse.class) : null;
        } catch (JsonProcessingException e) {
            // Handle exception if needed
            return null;
        }
    }

    @Override
    public void saveAllAuthors(AuthorPageResponse authorPage, GeneralPageRequest request) {
        String key = generateCacheKey(request);
        try {
            String json = objectMapper.writeValueAsString(authorPage);
            redisTemplate.opsForValue().set(key, json);
        } catch (JsonProcessingException e) {
            // Handle exception if needed
        }
    }
}

