package net.danshin.danshinnoticeandroidphone.models.genealogydate;

public enum DateType {
    BIRTH(1), DEATH(2);

    private final int value;

    DateType(int value) {
        this.value = value;
    }

    public static DateType fromValue(int value) {
        for (DateType type : DateType.values()) {
            if (type.value == value) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown value: " + value);
    }

    public static DateType fromString(String value) {
        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException("DateType cannot be null or empty");
        }
        try {
            return DateType.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown DateType: " + value);
        }
    }

    public int getValue() {
        return value;
    }
}
