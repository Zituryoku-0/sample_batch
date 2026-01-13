package com.example.sample_batch.job.user;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.batch.infrastructure.item.ItemReader;
import org.springframework.batch.infrastructure.item.ItemWriter;
import org.springframework.batch.infrastructure.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.infrastructure.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Configuration
@RequiredArgsConstructor
public class DeleteUserInfoJobConfig {

    @Bean
    public Job DeleteUserInfoJob(JobRepository jobRepository, Step deleteUserInfoStep) {
        return new JobBuilder("deleteUserInfoJob", jobRepository)
                .start(deleteUserInfoStep)
                .build();
    }

    @Bean
    public Step deleteUserInfoStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            ItemReader<UserInfo> userInfoItemReader,
            ItemProcessor<UserInfo, UserInfo> userInfoItemProcessor,
            ItemWriter<UserInfo> userInfoItemWriter
            ){
        return new StepBuilder("deleteUserInfoStep", jobRepository)
                .<UserInfo, UserInfo>chunk(100, transactionManager)
                .reader(userInfoItemReader)
                .processor(userInfoItemProcessor)
                .writer(userInfoItemWriter)
                .build();
    }

    // --- Reader: 更新対象をDBから読む ---
    @Bean
    public ItemReader<UserInfo> userInfoItemReader(DataSource dataSource) {
        return new JdbcCursorItemReaderBuilder<UserInfo>()
                .name("userInfoItemReader")
                .dataSource(dataSource)
                .sql("SELECT * FROM userinfo WHERE latest_access_time < DATEADD('YEAR', -1, NOW()) AND delete_flg = '0'")
                .rowMapper((rs, rowNum) -> {
                    UserInfo userInfo = new UserInfo();
                    userInfo.setUserId(rs.getString("userid"));
                    userInfo.setDeleteFlg(rs.getString("delete_flg").charAt(0));
                    return userInfo;
                })
                .build();
    }

    // --- Processor: 更新内容を作る（delete_flgを1にする） ---
    @Bean
    public ItemProcessor<UserInfo, UserInfo> userInfoItemProcessor() {
        return userInfo -> {
            userInfo.setDeleteFlg('1');
            return userInfo;
        };
    }

    // ---Writer: UPDATE文でDBを更新---
    @Bean
    public ItemWriter<UserInfo> userInfoItemWriter(DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<UserInfo>()
                .dataSource(dataSource)
                .sql("UPDATE userinfo SET delete_flg = :deleteFlg WHERE userid = :userId")
                .assertUpdates(true) // 更新件数が0の場合は例外をスロー
                .beanMapped()
                .build();
    }

}
