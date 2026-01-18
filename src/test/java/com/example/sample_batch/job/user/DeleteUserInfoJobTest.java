package com.example.sample_batch.job.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.JobRepositoryTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@SpringBootTest(properties = {
        "spring.batch.job.enabled=false" // テストの自動起動を止める
})
@SpringBatchTest
class DeleteUserInfoJobTest {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private JobRepositoryTestUtils jobRepositoryTestUtils;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        // バッチメタデータの掃除（同一job/paramsの衝突防止）
        jobRepositoryTestUtils.removeJobExecutions();

        // テストデータの作成（userinfoテーブル）
        jdbcTemplate.execute("DELETE FROM userinfo");
        jdbcTemplate.execute("INSERT INTO userinfo (userId, userName, userPassword) " +
                "VALUES('sampleUserId1', 'sample UserName1', 'abcdefgh')");
        jdbcTemplate.execute("INSERT INTO userinfo (userId, userName, userPassword) " +
                "VALUES('sampleUserId2', 'sample UserName2', 'abcdefgh')");
        jdbcTemplate.execute("update userInfo set latest_access_time = '2000-01-01 00:00:00' where userId = 'sampleUserId2'");
    }

    @Test
    void complete_delete_job() throws Exception {
        JobParameters params = new JobParametersBuilder()
                .addLong("runId", System.currentTimeMillis()) // 毎回ユニークにする
                .toJobParameters();

        JobExecution execution = jobLauncherTestUtils.launchJob(params);

        assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
    }
}
