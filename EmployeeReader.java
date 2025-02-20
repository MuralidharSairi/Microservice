package com.example.Microservice.reader;

import com.example.Microservice.model.Employee;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

@Component
public class EmployeeReader extends FlatFileItemReader<Employee> {

    public EmployeeReader() {
        setName("employeeItemReader");
        // Path to the CSV file in the resources/input directory
        setResource(new ClassPathResource("input/employees.csv"));

        // Skip the header line
        setLinesToSkip(1);

        // Set up the line mapper
        DefaultLineMapper<Employee> lineMapper = new DefaultLineMapper<>();

        // Tokenize each CSV line, split by commas
        DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer();
        tokenizer.setNames("name", "department", "salary"); // The column names in the CSV

        // Map the tokens to the Employee object
        BeanWrapperFieldSetMapper<Employee> fieldSetMapper = new BeanWrapperFieldSetMapper<>();
        fieldSetMapper.setTargetType(Employee.class);

        // Set up the line mapper components
        lineMapper.setLineTokenizer(tokenizer);
        lineMapper.setFieldSetMapper(fieldSetMapper);

        // Apply the line mapper to the reader
        setLineMapper(lineMapper);
    }
}


