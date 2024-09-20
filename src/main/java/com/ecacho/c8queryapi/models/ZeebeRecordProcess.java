package com.ecacho.c8queryapi.models;


import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

@Data
@Builder
@Document(indexName = "zeebe-record-process-instance")
public class ZeebeRecordProcess {

    private @Id String id;
    private String intent;
    private String valueType;
    private long timestamp;

    private Value value;

    @Data
    public static class Value {
        String elementId, type, processInstanceKey;
    }
}
