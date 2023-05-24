package com.example.SpringBatch.job.ValidatedParam;

import com.example.SpringBatch.job.ValidatedParam.Validator.FileParamValidator;
import lombok.RequiredArgsConstructor;
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
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * desc: 파일명을 파라미터로 전달/검증
 * run: --spring.batch.job.names=validatedParamJob -fileName=test.csv
 */

@Configuration
@RequiredArgsConstructor
public class ValidatedParamJobConfig {

    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Bean
    public Job validatedParamJob (Step validatedParamStep) { // 빈으로 등록된 Step 주입받기
        return jobBuilderFactory.get("validatedParamJob")
                .incrementer(new RunIdIncrementer())
                // 만약 여러개의 validator가 필요할 경우, CompositeJobParameterValidator
                .validator(new FileParamValidator())
                .start(validatedParamStep) // 함수가 아닌 빈 호출
                .build();
    }

    @JobScope
    @Bean
    public Step validatedParamStep(Tasklet validatedParamTasklet) {
        return stepBuilderFactory.get("validatedParamStep")
                .tasklet(validatedParamTasklet)
                .build();
    }

    @StepScope  // Step 하위
    @Bean
    public Tasklet validatedParamTasklet(@Value("#{jobParameters['fileName']}") String fileName) {
        return new Tasklet() {
            @Override
            public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
                System.out.println(fileName);
                System.out.println("validatedParam 호출하기");
                return RepeatStatus.FINISHED;
            }
        };
    }
}