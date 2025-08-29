package com.exam;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class StartupRunner implements CommandLineRunner {

    private final WorkflowService workflowService;

    public StartupRunner(WorkflowService workflowService) {
        this.workflowService = workflowService;
    }

    @Override
    public void run(String... args) {
        workflowService.execute();
    }
}
