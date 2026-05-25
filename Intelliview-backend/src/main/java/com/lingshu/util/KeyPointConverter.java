// util/KeyPointConverter.java
package com.lingshu.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lingshu.dto.KeyPoint;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@MappedTypes(List.class)
@MappedJdbcTypes(JdbcType.VARCHAR)
public class KeyPointConverter extends BaseTypeHandler<List<KeyPoint>> {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, List<KeyPoint> parameter, JdbcType jdbcType) throws SQLException {
        if (parameter == null || parameter.isEmpty()) {
            ps.setString(i, "[]");
        } else {
            try {
                ps.setString(i, objectMapper.writeValueAsString(parameter));
            } catch (JsonProcessingException e) {
                throw new SQLException("Failed to convert keyPoints to JSON", e);
            }
        }
    }

    @Override
    public List<KeyPoint> getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String dbData = rs.getString(columnName);
        return convertToEntityAttribute(dbData);
    }

    @Override
    public List<KeyPoint> getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String dbData = rs.getString(columnIndex);
        return convertToEntityAttribute(dbData);
    }

    @Override
    public List<KeyPoint> getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String dbData = cs.getString(columnIndex);
        return convertToEntityAttribute(dbData);
    }

    private List<KeyPoint> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.trim().isEmpty() || dbData.equals("[]")) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(dbData, new TypeReference<List<KeyPoint>>() {});
        } catch (IOException e) {
            throw new RuntimeException("Failed to convert JSON to keyPoints", e);
        }
    }
}
