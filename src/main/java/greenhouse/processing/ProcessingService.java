package greenhouse.processing;

import greenhouse.exceptions.VentilationUnsafeException;

import java.nio.file.Path;
import java.util.UUID;
public class ProcessingService {

    private final VentilationSafetyChecker safetyChecker;
    private final ExecutionRepository repository;

    public ProcessingService(Path dataDir) {
        this.safetyChecker = new VentilationSafetyChecker();
        this.repository = new ExecutionRepository(dataDir);
    }
    public IrrigationExecution process(String scheduleId, double humidityLevel) {
        IrrigationExecution execution = new IrrigationExecution(UUID.randomUUID().toString(), scheduleId, humidityLevel);
        if (!safetyChecker.isSafeToIrrigate(humidityLevel)) {
            execution.markFailed();
            repository.save(execution);
            throw new VentilationUnsafeException(
                    "Humidity too high to irrigate safely: " + humidityLevel + "%");
        }
        execution.markExecuted();
        repository.save(execution);
        return execution;
    }
    public void reverse(String executionId) {
        IrrigationExecution execution = repository.findById(executionId)
                .orElseThrow(() -> new IllegalArgumentException("No such execution: " + executionId));
        execution.reverse();
        repository.save(execution);
    }

    public ExecutionRepository getRepository() {
        return repository;
    }
}
