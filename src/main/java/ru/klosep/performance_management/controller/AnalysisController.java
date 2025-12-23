package ru.klosep.performance_management.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;
import ru.klosep.performance_management.port.AnalysisResultPublisher;
import ru.klosep.performance_management.port.TaskDataSource;
import ru.klosep.performance_management.service.TaskAnalysisService;

@RestController
@RequestMapping("/api/analysis")
public class AnalysisController {

    @Autowired
    @Qualifier("restTaskAdapter")
    private TaskDataSource restTaskAdapter;

    @Autowired
    @Qualifier("fileTaskAdapter")
    private TaskDataSource fileTaskAdapter;

    @Autowired
    @Qualifier("consoleResultAdapter")
    private AnalysisResultPublisher consolePublisher;

    @Autowired
    @Qualifier("databaseResultAdapter")
    private AnalysisResultPublisher dbPublisher;

    @GetMapping("/run-rest-console")
    public String analyzeFromRestToConsole() {
        TaskAnalysisService service = new TaskAnalysisService(
                restTaskAdapter, consolePublisher
        );
        service.analyzeAndPublish();
        return "Analysis completed: REST → Console";
    }

    @GetMapping("/run-file-db")
    public String analyzeFromFileToDb() {
        TaskAnalysisService service = new TaskAnalysisService(
                fileTaskAdapter, dbPublisher
        );
        service.analyzeAndPublish();
        return "Analysis completed: File → Database";
    }

    @GetMapping("/run-rest-db")
    public String analyzeFromRestToDb() {
        TaskAnalysisService service = new TaskAnalysisService(
                restTaskAdapter, dbPublisher
        );
        service.analyzeAndPublish();
        return "Analysis completed: REST → Database";
    }
}