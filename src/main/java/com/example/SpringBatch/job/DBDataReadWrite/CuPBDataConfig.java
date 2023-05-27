package com.example.SpringBatch.job.DBDataReadWrite;

import com.example.SpringBatch.crawler.CU.CuEventCrawl;
import com.example.SpringBatch.crawler.CU.CuPBCrawl;
import com.example.SpringBatch.domain.pbproducts.PbProducts;
import com.example.SpringBatch.domain.pbproducts.PbProductsRepository;
import lombok.RequiredArgsConstructor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.*;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.data.builder.RepositoryItemWriterBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * desc: DB 데이터 마이그레이션
 * run: --job.name=cuPBCrawlingJob
 */

@Configuration
@RequiredArgsConstructor
public class CuPBDataConfig {

    @Autowired
    private CuEventCrawl cuEventCrawl; // 여기!!

    @Autowired
    private PbProductsRepository pBProductsRepository;

    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Bean
    public Job cuPBCrawlingJob (Step createConnectionStep, Step cuPBCrawlingStep) {
        return jobBuilderFactory.get("cuPBCrawlingJob") // 이름 정해주기
                .incrementer(new RunIdIncrementer())  // 순서대로 -> id 부여
                .start(createConnectionStep)
                .next(cuPBCrawlingStep)
                .build();
    }

    // chunk
//    @JobScope   // Job 하위
//    @Bean
//    public Step cuPBCrawlingStep(
//            ItemReader pbReader,
//            ItemProcessor pbProcessor,
//            ItemWriter pbWriter) {
//        return stepBuilderFactory.get("cuPBCrawlingStep")
//                .<PbProducts, PbProducts>chunk(5)
//                .reader(pbReader)
//                .processor(pbProcessor)
//                .writer(pbWriter)
//                .build();
//    }
//
//    @JobScope
//    @Bean
//    public void createConnectionStep() {
//
//    }
//
//    @StepScope
//    @Bean
//    public RepositoryItemWriter<PbProducts> pbWriter() {
//        return new RepositoryItemWriterBuilder<PbProducts>()
//                .repository(pBProductsRepository)
//                .methodName("save")
//                .build();
//    }
//
//    @StepScope
//    @Bean
//    public ItemProcessor<PbProducts, PbProducts> pbProcessor() {
//        return new ItemProcessor<PbProducts, PbProducts>() {
//            @Override
//            public PbProducts process(PbProducts item) throws Exception {
//                System.out.println("CU PB Crawling Batch Start");
//                PbProducts pbProducts = cuPBCrawl.cuPBCrawling(driver);
//                return pbProducts;
//            }
//        };
//    }
//
//    @StepScope
//    @Bean
//    public ItemReader<PbProducts> pbReader() {
//        return new ItemReader<PbProducts>() {
//            @Override
//            public PbProducts read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
//                return null;
//            }
//        };
//    }

    //  tasklet
    @JobScope   // Job 하위
    @Bean
    public Step cuPBCrawlingStep(Tasklet cuPBCrawlingTasklet) {
        return stepBuilderFactory.get("cuPBCrawlingStep")
                .tasklet(cuPBCrawlingTasklet)
                .build();
    }

    @StepScope  // Step 하위
    @Bean
    public Tasklet cuPBCrawlingTasklet() {
        return new Tasklet() {
            @Override
            public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
                System.out.println("CU PB Crawling Batch Start");
                List<PbProducts> pbProducts = cuEventCrawl.cuPBCrawling();
                pBProductsRepository.saveAll(pbProducts);
                return RepeatStatus.FINISHED;
            }
        };
    }
}
