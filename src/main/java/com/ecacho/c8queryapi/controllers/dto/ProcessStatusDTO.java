package com.ecacho.c8queryapi.controllers.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProcessStatusDTO {

    String status;
    long createdAt;
}
