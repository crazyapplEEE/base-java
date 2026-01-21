package org.jeecg.modules.content.dto;

public enum SampleType {
    DOCUMENT("DOCUMENT"), IMAGE("IMAGE"), TEXT("TEXT");

    private final String sampleType;

    SampleType(final String sampleType) {
        this.sampleType = sampleType;
    }

    @Override public String toString() {
        return sampleType;
    }
}
