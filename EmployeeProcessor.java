package com.example.Microservice.processor;

import com.example.Microservice.model.Employee;
import org.springframework.batch.item.ItemProcessor;

public class EmployeeProcessor implements ItemProcessor<Employee, Employee> {
    @Override
    public Employee process(@SuppressWarnings("null") Employee employee) throws Exception {
        // Perform any processing or transformation if needed
        return employee;
    }
}
