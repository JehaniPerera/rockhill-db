package com.rockhill.reception_hall.staff.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class StaffTypeConverter implements AttributeConverter<StaffType, String> {

    @Override
    public String convertToDatabaseColumn(StaffType type) {
        return type == null ? null : type.getDbValue();
    }

    @Override
    public StaffType convertToEntityAttribute(String value) {
        return value == null ? null : StaffType.fromDbValue(value);
    }
}
