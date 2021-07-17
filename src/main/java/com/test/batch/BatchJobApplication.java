package com.test.batch;

import java.time.LocalDateTime;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@SpringBootApplication
@EnableBatchProcessing
@EnableScheduling
public class BatchJobApplication {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	public JobBuilderFactory jobBuilderFactory;
	
	@Autowired
	public StepBuilderFactory stepBuilderFactory;
	
	@Autowired
	public MyHandler handler;
	
	@Bean
	public Step printMQ() {
		
		Tasklet tasklet1 = new Tasklet() {
			
			
			@Override
			public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
				 
				
				System.out.println("inside execute 1");
				 logger.info("inside execute 1");
				handler.readMQ();
				//Thread.sleep(1000);
				return RepeatStatus.FINISHED;
			}
		}; 
		return this.stepBuilderFactory.get("printMQ")
				.tasklet(tasklet1)
				.build();
	}
	
	@Bean
	public Job readMQ() {
		
		return this.jobBuilderFactory.get("readMQ").start(printMQ()).build();
		
	}
	
	@Bean
	public Job c() {
		
		return this.jobBuilderFactory.get("writeMQ").start(printMQ()).build();
		
	}
	

	@Autowired
	public JobLauncher jobLauncher;

	@Scheduled(cron = "0/30 * * * * *")
	public void runJob() throws JobExecutionAlreadyRunningException, JobRestartException, JobInstanceAlreadyCompleteException, JobParametersInvalidException, Exception {
		JobParametersBuilder paramBuilder = new JobParametersBuilder();
		paramBuilder.addDate("date", new Date());
		logger.info("Starting cron scheduled Job");
		this.jobLauncher.run(scheduledJob(), paramBuilder.toJobParameters());
	}

	@Bean
	public Step scheduledStep() throws Exception {
		logger.info("Starting cron scheduled Step");
		return this.stepBuilderFactory.get("scheduledStep").tasklet(new Tasklet() {
			
			@Override
			public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
				System.out.println("The run time is: " + LocalDateTime.now());
				return RepeatStatus.FINISHED;
			}
		}).build();

	}

	@Bean
	public Job scheduledJob() throws Exception {
		return this.jobBuilderFactory.get("scheduledJob").incrementer(new RunIdIncrementer()).start(scheduledStep()).build();
	}
	
	public static void main(String[] args) {
		SpringApplication.run(BatchJobApplication.class, args);
	}

}
