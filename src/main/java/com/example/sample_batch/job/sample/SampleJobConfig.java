package com.example.sample_batch.job.sample;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
public class SampleJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final SampleTasklet sampleTasklet;

    @Bean
    public Job sampleJob(){
        return new JobBuilder("sampleJob", jobRepository)
                .start(sampleStep())
                .build();
    }

    @Bean
    public Step sampleStep(){
        return new StepBuilder("sampleStep", jobRepository)
                .tasklet(sampleTasklet, transactionManager)
                .build();
    }
}
