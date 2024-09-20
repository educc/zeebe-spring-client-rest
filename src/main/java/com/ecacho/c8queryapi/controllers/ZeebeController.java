package com.ecacho.c8queryapi.controllers;


import com.ecacho.c8queryapi.config.CamundaClientBuilder;
import com.ecacho.c8queryapi.controllers.dto.ProcessStatusDTO;
import com.ecacho.c8queryapi.controllers.dto.UserTaskDTO;
import com.ecacho.c8queryapi.models.ZeebeRecordProcess;
import com.ecacho.c8queryapi.models.ZeebeRecordUserTask;
import com.ecacho.c8queryapi.repository.ZeebeJobRepository;
import com.ecacho.c8queryapi.repository.ZeebeProcessRepository;
import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;
import lombok.AllArgsConstructor;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@RequestMapping("/zeebe")
@AllArgsConstructor
public class ZeebeController {

    ZeebeProcessRepository zeebeProcessRepo;
    ZeebeJobRepository zeebeRecordJobRepo;

    @GetMapping("/")
    public Iterable<ZeebeRecordProcess> getAll() {
        return zeebeProcessRepo.findAll();
    }

    @GetMapping("/{instanceKey}")
    public Stream<ZeebeRecordProcess> findByInstanceKey(@PathVariable String instanceKey) {
        SearchHits<ZeebeRecordProcess> rs = zeebeProcessRepo.findByValue_ProcessInstanceKey(instanceKey);
        return rs.get().map(SearchHit::getContent);
    }


    @GetMapping("/process-instance/{instanceKey}/status")
    public Optional<ProcessStatusDTO> findStatusOfProcessInstance(@PathVariable String instanceKey) {
        List<ZeebeRecordProcess> rs = zeebeProcessRepo.findByValue_ProcessInstanceKey(instanceKey)
                .get().map(SearchHit::getContent)
                .toList();

        if (rs.isEmpty()) {
            return Optional.empty();
        }

        return rs.stream()
                .filter(it -> it.getValue().getElementId().startsWith("end"))
                .findFirst()
                .map(it -> ProcessStatusDTO.builder()
                        .status(it.getValue().getElementId())
                        .createdAt(it.getTimestamp())
                        .build()
                );
    }

    @GetMapping("/process-instance/{instanceKey}/user-tasks")
    public Stream<UserTaskDTO> findJobByInstanceKey(@PathVariable String instanceKey) {
        SearchHits<ZeebeRecordUserTask> rs = zeebeRecordJobRepo.findByValue_ProcessInstanceKey(instanceKey);

        Map<String, UserTaskDTO> userTaskMap = rs.get().map(SearchHit::getContent)
                .sorted(Comparator.comparingLong(ZeebeRecordUserTask::getTimestamp))
                .map(it -> UserTaskDTO.builder()
                        .userTaskKey(it.getKey())
                        .status(it.getIntent())
                        .assignee(it.getValue().getAssignee())
                        .action(it.getValue().getAction())
                        .createdAt(it.getTimestamp())
                        .build()
                )
                .collect(Collectors.toMap(UserTaskDTO::getUserTaskKey, it -> it, (a, b) -> {
                    if (a.getCreatedAt() > b.getCreatedAt()) {
                        return a;
                    } else {
                        return b;
                    }
                }));

        return userTaskMap.values().stream();
    }

    //post endpoint that requires instanceKey path param and approve boolean query param
    @PostMapping("/user-task/{userTaskKey}/complete")
    public void approveUserTask(
            @PathVariable long userTaskKey,
            @RequestParam boolean approve
    ) {
        try (ZeebeClient client = CamundaClientBuilder.buildPlainText()) {
            String action = approve ? "APPROVED" : "REJECTED";

            //TODO: handle not found
            client.newUserTaskCompleteCommand(userTaskKey)
                    .action(action)
                    .variables(Map.of(
                            "input_is_approved", approve
                    ))
                    .send()
                    .join();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    @PostMapping("/process/{processId}/run")
    public Map<String, Object> runProcess(@PathVariable String processId) {
        try (ZeebeClient client = CamundaClientBuilder.build()) {
            ProcessInstanceEvent processEvent = client.newCreateInstanceCommand()
                    .bpmnProcessId(processId)
                    .latestVersion()
                    .variables("{\"assignee_1\": \"edward\", \"assignee_2\": \"jane\"}")
                    .send()
                    .join();

            return Map.of(
                    "status", "success",
                    "processInstanceKey", processEvent.getProcessInstanceKey()
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
