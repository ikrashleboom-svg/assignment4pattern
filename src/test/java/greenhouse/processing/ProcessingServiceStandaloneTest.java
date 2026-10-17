package greenhouse.processing;

import greenhouse.exceptions.VentilationUnsafeException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
class ProcessingServiceStandaloneTest {

    @Test
    void process_safeHumidity_executesAndPersists(@TempDir Path tempDir) {
        ProcessingService service = new ProcessingService(tempDir);

        IrrigationExecution execution = service.process("fake-schedule-id", 40.0);

        assertEquals(ExecutionStatus.EXECUTED, execution.getStatus());
        assertEquals(ExecutionStatus.EXECUTED,
                service.getRepository().findById(execution.getId()).orElseThrow().getStatus());
    }

    @Test
    void process_unsafeHumidity_throwsAndPersistsAsFailed(@TempDir Path tempDir) {
        ProcessingService service = new ProcessingService(tempDir);

        assertThrows(VentilationUnsafeException.class, () -> service.process("fake-schedule-id", 95.0));

        var all = service.getRepository().findAll();
        assertEquals(1, all.size());
        assertEquals(ExecutionStatus.FAILED, all.get(0).getStatus());
    }

    @Test
    void reverse_undoesAnExecutedRun(@TempDir Path tempDir) {
        ProcessingService service = new ProcessingService(tempDir);
        IrrigationExecution execution = service.process("fake-schedule-id", 40.0);

        service.reverse(execution.getId());

        assertEquals(ExecutionStatus.REVERSED,
                service.getRepository().findById(execution.getId()).orElseThrow().getStatus());
    }

    @Test
    void ventilationSafetyChecker_rejectsHighHumidity() {
        VentilationSafetyChecker checker = new VentilationSafetyChecker();

        assertTrue(checker.isSafeToIrrigate(50.0));
        assertFalse(checker.isSafeToIrrigate(90.0));
    }
}
