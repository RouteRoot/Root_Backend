package com.root.root.util;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;

@Converter
public class WeeklyScheduleConverter implements AttributeConverter<Map<String, Integer>, String> {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(Map<String, Integer> attribute) {
        if (attribute == null || attribute.isEmpty()) {
            return "{}"; // 기본 빈 JSON 객체 반환
        }
        try {
            return objectMapper.writeValueAsString(attribute);
        } catch (Exception e) {
            throw new RuntimeException("요일별 스케줄 데이터를 JSON으로 변환하는 데 실패했습니다.", e);
        }
    }

    @Override
    public Map<String, Integer> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.trim().isEmpty()) {
            return Map.of();
        }
        try {
            return objectMapper.readValue(dbData, new TypeReference<Map<String, Integer>>() {});
        } catch (Exception e) {
            throw new RuntimeException("DB의 JSON 데이터를 요일별 스케줄 Map으로 변환하는 데 실패했습니다.", e);
        }
    }
}
