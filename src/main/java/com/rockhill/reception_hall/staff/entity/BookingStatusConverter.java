package com.rockhill.reception_hall.staff.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class BookingStatusConverter implements AttributeConverter<BookingStatus, String> {

    @Override
    public String convertToDatabaseColumn(BookingStatus status) {
        return status == null ? null : status.getDbValue();
    }

    @Override
    public BookingStatus convertToEntityAttribute(String value) {
        return value == null ? null : BookingStatus.fromDbValue(value);
    }
}
