package com.example.sample_batch.job.sample;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.observability.jfr.events.job.JobLaunchEvent;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.test.JobOperatorTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@SpringBootTest
@SpringBatchTest
@ActiveProfiles("test")
public class SampleJobTest {

    @Autowired
    private JobOperatorTestUtils jobOperatorTestUtils;

    @Test
    @DisplayName("Sample Job executes successfully")
    void sampleJob_completes() throws Exception{
        JobParameters params = new JobParametersBuilder()
                .addLong("run id", System.currentTimeMillis()) // 毎回ユニーク
                .toJobParameters();

        JobExecution execution = jobOperatorTestUtils.startJob(params);

        assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);

    }
}
