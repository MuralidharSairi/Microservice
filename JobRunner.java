package com.example.Microservice; // Adjust package name if needed

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired; // Import this
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class JobRunner implements CommandLineRunner {

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    private Job importEmployeeJob;

    @Override
    public void run(String... args) throws Exception {
        // Trigger your batch job
        JobParameters jobParameters = new JobParameters();
        jobLauncher.run(importEmployeeJob, jobParameters);
    }
}
