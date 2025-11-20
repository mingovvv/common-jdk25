package mingovvv.common.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class JsonUtil {

    private static final ObjectMapper objectMapper = new ObjectMapper()
        .registerModules(new ParameterNamesModule(), new Jdk8Module(), new JavaTimeModule())
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
        .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

    /**
     * Object -> JSON String
     */
    public static String toJson(Object object) {
        if (object == null) return null;
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            log.error("ToJson Error: {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Object -> Pretty JSON String
     */
    public static String toPrettyJson(Object object) {
        if (object == null) return null;
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(object);
        } catch (JsonProcessingException e) {
            log.error("ToPrettyJson Error: {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    /**
     * JSON String -> Object (단일 객체)
     */
    public static <T> T toObject(String json, Class<T> clazz) {
        if (json == null || json.isBlank()) return null;
        try {
            return objectMapper.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            log.error("ToObject Error: {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    /**
     * JSON String -> Object (복잡한 제네릭: List, Map 등)
     * 사용법: JsonUtil.toObject(json, new TypeReference<List<MemberDto>>(){});
     */
    public static <T> T toObject(String json, TypeReference<T> typeReference) {
        if (json == null || json.isBlank()) return null;
        try {
            return objectMapper.readValue(json, typeReference);
        } catch (JsonProcessingException e) {
            log.error("ToObject(TypeRef) Error: {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    /**
     * JSON String -> JsonNode (트리 구조 탐색용)
     * 밍고가 원했던 기능 부활!
     */
    public static JsonNode readTree(String json) {
        if (json == null || json.isBlank()) return null;
        try {
            return objectMapper.readTree(json);
        } catch (JsonProcessingException e) {
            log.error("ReadTree Error: {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    /**
     * ObjectNode 생성 (JSON 조립용)
     * 밍고가 원했던 기능 부활!
     */
    public static ObjectNode createNode() {
        return objectMapper.createObjectNode();
    }

    /**
     * List 변환 편의 메서드
     */
    public static <T> List<T> toList(String json, Class<T> elementClass) {
        if (json == null || json.isBlank()) return null;
        try {
            return objectMapper.readValue(json, objectMapper.getTypeFactory().constructCollectionType(List.class, elementClass));
        } catch (JsonProcessingException e) {
            log.error("ToList Error: {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 원본 Mapper가 필요할 때
     */
    public static ObjectMapper getMapper() {
        return objectMapper;
    }

}
