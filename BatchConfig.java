package com.example.Microservice.config;

import com.example.Microservice.model.Employee;
import com.example.Microservice.processor.EmployeeProcessor;
import com.example.Microservice.writer.AckFileWriter;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.ItemPreparedStatementSetter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.beans.factory.annotation.Autowired;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.springframework.batch.item.database.ItemPreparedStatementSetter;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@Configuration
@EnableBatchProcessing
public class BatchConfig {

    private final PlatformTransactionManager transactionManager;
    private final JobRepository jobRepository;

    @Autowired
    public BatchConfig(PlatformTransactionManager transactionManager, JobRepository jobRepository) {
        this.transactionManager = transactionManager;
        this.jobRepository = jobRepository;
    }

    @Bean
    public FlatFileItemReader<Employee> reader() {
        return new FlatFileItemReaderBuilder<Employee>()
                .name("employeeReader")
                .resource(new ClassPathResource("input/employees.csv")) // Ensure the path is correct
                .delimited()
                .names("name", "department", "salary")
                .targetType(Employee.class)
                .linesToSkip(1) // Skip the header
                .lineMapper((line, lineNumber) -> {
                    // Log the line being read
                    System.out.println("Reading line " + lineNumber + ": " + line);
                    String[] fields = line.split(",");
                    Employee employee = new Employee();
                    employee.setName(fields[0]);
                    employee.setDepartment(fields[1]);
                    employee.setSalary(Double.parseDouble(fields[2]));
                    return employee;
                })
                .build();
    }

    @Bean
    public EmployeeProcessor processor() {
        return new EmployeeProcessor();
    }

    @Bean
    public AckFileWriter ackFileWriter() {
        String absolutePath = System.getProperty("user.dir") + "/target/output/employees.ack";
        return new AckFileWriter(absolutePath);
    }

    @Bean
    public Step step(@Autowired DataSource dataSource) {
        return new StepBuilder("step", jobRepository)
                .<Employee, Employee>chunk(10, transactionManager)
                .reader(reader())
                .processor(processor())
                .writer(new JdbcBatchItemWriterBuilder<Employee>()
                        .sql("INSERT INTO employee (name, department, salary) VALUES (?, ?, ?)") // Use positional parameters
                        .dataSource(dataSource)
                        .itemPreparedStatementSetter(new EmployeePreparedStatementSetter())
                        .build())
                        .listener(ackFileWriter())
                .build();
    }
    

    @Bean
    public Job importEmployeeJob(@Autowired DataSource dataSource) {
        return new JobBuilder("importEmployeeJob", jobRepository)
                .start(step(dataSource))
                .build();
    }

    // Custom ItemPreparedStatementSetter for Employee
    public static class EmployeePreparedStatementSetter implements ItemPreparedStatementSetter<Employee> {
        @Override
        public void setValues(Employee item, PreparedStatement ps) throws SQLException {
            ps.setString(1, item.getName());
            ps.setString(2, item.getDepartment());
            ps.setDouble(3, item.getSalary());
        }
    }
}
