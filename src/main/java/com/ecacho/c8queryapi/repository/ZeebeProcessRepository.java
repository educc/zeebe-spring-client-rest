package com.ecacho.c8queryapi.repository;

import com.ecacho.c8queryapi.models.ZeebeRecordProcess;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface ZeebeProcessRepository extends ElasticsearchRepository<ZeebeRecordProcess, String> {


    SearchHits<ZeebeRecordProcess> findByValue_ProcessInstanceKey(String processInstanceKey);
}
