package greenhouse.intake;

import greenhouse.exceptions.InvalidSensorReadingException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
class IntakeServiceStandaloneTest {

    @Test
    void intake_persistsAScheduledSchedule(@TempDir Path tempDir) {
        IntakeService service = new IntakeService(tempDir);

        IrrigationSchedule schedule = service.intake("Zone-Standalone", 20.0);

        assertEquals(ScheduleStatus.SCHEDULED, schedule.getStatus());
        assertEquals(ScheduleStatus.SCHEDULED,
                service.getRepository().findById(schedule.getId()).orElseThrow().getStatus());
    }

    @Test
    void intake_outOfRangeReading_throwsAndPersistsNothing(@TempDir Path tempDir) {
        IntakeService service = new IntakeService(tempDir);

        assertThrows(InvalidSensorReadingException.class, () -> service.intake("Zone-Bad", -5.0));
        assertTrue(service.getRepository().findAll().isEmpty());
    }

    @Test
    void cancel_undoesAScheduledSchedule(@TempDir Path tempDir) {
        IntakeService service = new IntakeService(tempDir);
        IrrigationSchedule schedule = service.intake("Zone-Cancel", 20.0);

        service.cancel(schedule.getId());

        assertEquals(ScheduleStatus.CANCELLED,
                service.getRepository().findById(schedule.getId()).orElseThrow().getStatus());
    }

    @Test
    void schedule_cannotBeCancelledTwice() {
        IrrigationSchedule schedule = new IrrigationSchedule("id-1", "Zone-X", 20.0);
        schedule.markScheduled();
        schedule.cancel();

        assertThrows(IllegalStateException.class, schedule::cancel);
    }

    @Test
    void moistureEvaluator_decidesIrrigationNeedBasedOnDryness() {
        MoistureEvaluator evaluator = new MoistureEvaluator();

        assertTrue(evaluator.isIrrigationNeeded(10.0));
        assertFalse(evaluator.isIrrigationNeeded(80.0));
    }
}
