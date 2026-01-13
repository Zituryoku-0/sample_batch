# Copilot Instructions for Spring Batch Project

## プロジェクト概要

このリポジトリはSpring Batchを使用したバッチ処理専用のプロジェクトです。

- **フレームワーク**: Spring Boot 4.x + Spring Batch
- **言語**: Java 17
- **ビルドツール**: Gradle
- **データベース**: H2（開発/テスト）、PostgreSQL互換モード

## ディレクトリ構成

```
src/main/java/com/example/sample_batch/
├── config/          # バッチ共通設定クラス
├── job/             # ジョブ定義（ジョブ単位でサブパッケージを作成）
│   └── {jobname}/   # 各ジョブのConfig, Entity, Reader, Processor, Writer
├── listener/        # ジョブ/ステップリスナー
└── util/            # ユーティリティクラス
```

## コーディング規約

### 言語設定

- **Copilotの応答・指摘・コメントはすべて日本語で記述すること**

### ジョブ作成時のルール

1. **パッケージ構成**: 新しいジョブは `job/{ジョブ名}/` パッケージ配下に作成する
2. **命名規則**:
   - Configクラス: `{JobName}JobConfig.java`
   - Entityクラス: `{EntityName}.java`
   - メソッド名: キャメルケース（小文字始まり）
3. **Beanメソッド名**: `@Bean`メソッドは小文字始まりのキャメルケースで命名する

### Spring Batch設計パターン

```java
@Configuration
public class XxxJobConfig {

    @Bean
    public Job xxxJob(JobRepository jobRepository, Step xxxStep) {
        return new JobBuilder("xxxJob", jobRepository)
                .start(xxxStep)
                .build();
    }

    @Bean
    public Step xxxStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            ItemReader<Entity> reader,
            ItemProcessor<Entity, Entity> processor,
            ItemWriter<Entity> writer) {
        return new StepBuilder("xxxStep", jobRepository)
                .<Entity, Entity>chunk(100, transactionManager)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .build();
    }
}
```

### ItemReader/Writer実装時の注意

1. **JdbcCursorItemReader**: 大量データの読み込みに使用
2. **JdbcBatchItemWriter**: バッチ更新に使用、`.beanMapped()`使用時はSQLパラメータ名をJavaプロパティ名と一致させる

```java
// 正しい例: Javaプロパティ名 userId に合わせる
.sql("UPDATE table SET col = :value WHERE id = :userId")

// 誤った例: 大文字小文字が一致しない
.sql("UPDATE table SET col = :value WHERE id = :userid")
```

### RowMapperの実装

```java
.rowMapper((rs, rowNum) -> {
    Entity entity = new Entity();
    entity.setId(rs.getString("column_name"));
    return entity;
})
```

## データベース互換性

### H2データベース使用時の注意

このプロジェクトはH2データベース（PostgreSQL互換モード）を使用しています。
以下の構文はH2で動作しないため、代替構文を使用してください：

| PostgreSQL構文 | H2互換構文 |
|---------------|-----------|
| `NOW() - INTERVAL '1 year'` | `DATEADD('YEAR', -1, NOW())` |
| `NOW() - INTERVAL '1 month'` | `DATEADD('MONTH', -1, NOW())` |
| `NOW() - INTERVAL '1 day'` | `DATEADD('DAY', -1, NOW())` |

## テスト

### バッチジョブのテスト

```java
@SpringBatchTest
@SpringBootTest
class XxxJobTest {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Test
    void testJob() throws Exception {
        JobExecution jobExecution = jobLauncherTestUtils.launchJob();
        assertEquals(BatchStatus.COMPLETED, jobExecution.getStatus());
    }
}
```

## 依存ライブラリ

- `spring-boot-starter-batch`: Spring Batch本体
- `spring-boot-starter-data-jpa`: JPA/データアクセス
- `spring-boot-starter-validation`: バリデーション
- `lombok`: ボイラープレートコード削減
- `h2`: 開発/テスト用インメモリDB

## よくある問題と解決策

### 1. INTERVAL構文エラー
- **症状**: `INTERVAL 定数を解析できません`
- **原因**: H2がPostgreSQLのINTERVAL構文をサポートしていない
- **解決**: `DATEADD()`関数を使用

### 2. Invalid property エラー
- **症状**: `Invalid property 'xxx' of bean class`
- **原因**: SQLパラメータ名とJavaプロパティ名の不一致
- **解決**: パラメータ名を正確にプロパティ名と一致させる（大文字小文字含む）

### 3. Job already exists エラー
- **症状**: 同名のジョブが既に存在する
- **原因**: ジョブ名の重複
- **解決**: ユニークなジョブ名を設定

