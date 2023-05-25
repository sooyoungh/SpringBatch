package com.example.SpringBatch.job.DBDataReadWrite;

import com.example.SpringBatch.domain.accounts.Accounts;
import com.example.SpringBatch.domain.accounts.AccountsRepository;
import com.example.SpringBatch.domain.orders.Orders;
import com.example.SpringBatch.domain.orders.OrdersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.batch.item.data.builder.RepositoryItemWriterBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;

import java.util.Arrays;
import java.util.Collections;

/**
 * desc: DB 데이터 마이그레이션
 * run: --job.name=trMigrationJob
 */

@Configuration
@RequiredArgsConstructor
public class TrMigrationConfig {

    @Autowired
    private OrdersRepository ordersRepository;

    @Autowired
    private AccountsRepository accountsRepository;

    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Bean
    public Job trMigrationJob (Step trMigrationStep) {
        return jobBuilderFactory.get("trMigrationJob") // 이름 정해주기
                .incrementer(new RunIdIncrementer())  // 순서대로 -> id 부여
                .start(trMigrationStep)
                .build();
    }

    @JobScope   // Job 하위
    @Bean
    public Step trMigrationStep(
            ItemReader trOrdersReader,
            ItemProcessor trOrderProcessor,
            ItemWriter trOrdersWriter) {
        return stepBuilderFactory.get("trMigrationStep")
                .<Orders, Accounts>chunk(5)
                .reader(trOrdersReader)
                .processor(trOrderProcessor)
                .writer(trOrdersWriter)
                .build();
    }

    @StepScope
    @Bean
    public RepositoryItemWriter<Accounts> trOrdersWriter() {
        return new RepositoryItemWriterBuilder<Accounts>()
                .repository(accountsRepository)
                .methodName("save")
                .build();
    }

    @StepScope
    @Bean
    public ItemProcessor<Orders, Accounts> trOrderProcessor () {
        return new ItemProcessor<Orders, Accounts>() {
            @Override
            public Accounts process(Orders item) throws Exception {
                return new Accounts(item);
            }
        };
    }

    @StepScope
    @Bean
    public RepositoryItemReader<Orders> trOrdersReader() {
        return new RepositoryItemReaderBuilder<Orders>()
                .name("trOrdersReader")
                .repository(ordersRepository)
                .methodName("findAll")
                .pageSize(5) // 보통 chunk 단위와 같게
                .arguments(Arrays.asList()) // 메소드의 인자가 필요할 경우
                .sorts(Collections.singletonMap("id", Sort.Direction.ASC))
                .build();
    }
}
