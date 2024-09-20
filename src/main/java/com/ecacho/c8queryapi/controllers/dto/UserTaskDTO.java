package com.ecacho.c8queryapi.controllers.dto;

import lombok.Builder;
import lombok.Data;


@Data
@Builder
public class UserTaskDTO {
    String userTaskKey;
    String status;
    String assignee;
    String action;
    long createdAt;
}
