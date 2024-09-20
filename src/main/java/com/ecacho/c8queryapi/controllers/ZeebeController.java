package com.ecacho.c8queryapi.controllers;


import com.ecacho.c8queryapi.config.CamundaClientBuilder;
import com.ecacho.c8queryapi.models.ZeebeRecordUserTask;
import com.ecacho.c8queryapi.models.ZeebeRecordProcess;
import com.ecacho.c8queryapi.repository.ZeebeJobRepository;
import com.ecacho.c8queryapi.repository.ZeebeProcessRepository;
import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;
import lombok.AllArgsConstructor;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
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

    @GetMapping("/user-tasks/{instanceKey}")
    public Stream<ZeebeRecordUserTask> findJobByInstanceKey(@PathVariable String instanceKey) {
        SearchHits<ZeebeRecordUserTask> rs = zeebeRecordJobRepo.findByValue_ProcessInstanceKey(instanceKey);
        return rs.get().map(SearchHit::getContent);
    }

    //run proess
    @PostMapping("/run-process/{processId}")
    public Map<String, Object> runProcess(@PathVariable String processId) {
        try (ZeebeClient client = CamundaClientBuilder.build()) {
            ProcessInstanceEvent processEvent = client.newCreateInstanceCommand()
                    .bpmnProcessId(processId)
                    .latestVersion()
                    .variables("{\"assignee_1\": \"edward\"}")
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
