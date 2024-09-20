package com.ecacho.c8queryapi.repository;

import com.ecacho.c8queryapi.models.ZeebeRecordUserTask;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface ZeebeJobRepository extends ElasticsearchRepository<ZeebeRecordUserTask, String> {


    SearchHits<ZeebeRecordUserTask> findByValue_ProcessInstanceKey(String processInstanceKey);
}
