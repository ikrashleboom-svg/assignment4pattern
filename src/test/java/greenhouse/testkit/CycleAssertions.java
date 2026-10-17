package greenhouse.testkit;

import greenhouse.chain.CycleRecord;
import greenhouse.chain.CycleStatus;
import greenhouse.finalization.DispenseStatus;
import greenhouse.intake.ScheduleStatus;
import greenhouse.processing.ExecutionStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;
public class CycleAssertions {

    private final CycleRecord record;
    private final GreenhouseTestKit kit;

    CycleAssertions(CycleRecord record, GreenhouseTestKit kit) {
        this.record = record;
        this.kit = kit;
    }

    public CycleAssertions completedSuccessfully() {
        assertEquals(CycleStatus.COMPLETED, record.getStatus(), "cycle should have completed");
        return this;
    }

    public CycleAssertions wasCompensatedAfterBFailure() {
        assertEquals(CycleStatus.COMPENSATED_AFTER_B_FAILURE, record.getStatus(),
                "cycle should have been compensated after a B failure");
        return this;
    }

    public CycleAssertions wasCompensatedAfterCFailure() {
        assertEquals(CycleStatus.COMPENSATED_AFTER_C_FAILURE, record.getStatus(),
                "cycle should have been compensated after a C failure");
        return this;
    }

    public CycleAssertions andScheduleIsCancelled() {
        assertEquals(ScheduleStatus.CANCELLED,
                kit.schedules().findById(record.getScheduleId()).orElseThrow().getStatus(),
                "schedule should have been cancelled");
        return this;
    }

    public CycleAssertions andScheduleIsStillScheduled() {
        assertEquals(ScheduleStatus.SCHEDULED,
                kit.schedules().findById(record.getScheduleId()).orElseThrow().getStatus(),
                "schedule should still be scheduled");
        return this;
    }

    public CycleAssertions andExecutionIsReversed() {
        assertEquals(ExecutionStatus.REVERSED,
                kit.executions().findById(record.getExecutionId()).orElseThrow().getStatus(),
                "execution should have been reversed");
        return this;
    }

    public CycleAssertions andDispenseIsConfirmed() {
        assertEquals(DispenseStatus.CONFIRMED,
                kit.dispenses().findById(record.getDispenseId()).orElseThrow().getStatus(),
                "dispense should have been confirmed");
        return this;
    }
}
