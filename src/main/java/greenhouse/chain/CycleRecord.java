package greenhouse.chain;
public class CycleRecord {

    private final String id;
    private final String zoneId;
    private String scheduleId;
    private String executionId;
    private String dispenseId;
    private CycleStatus status;

    public CycleRecord(String id, String zoneId, String scheduleId) {
        this.id = id;
        this.zoneId = zoneId;
        this.scheduleId = scheduleId;
        this.executionId = null;
        this.dispenseId = null;
        this.status = CycleStatus.PENDING_B;
    }

    public CycleRecord(String id, String zoneId, String scheduleId, String executionId,
                        String dispenseId, CycleStatus status) {
        this.id = id;
        this.zoneId = zoneId;
        this.scheduleId = scheduleId;
        this.executionId = executionId;
        this.dispenseId = dispenseId;
        this.status = status;
    }

    public void moveToPendingC(String executionId) {
        if (status != CycleStatus.PENDING_B) {
            throw new IllegalStateException("Cannot move to PENDING_C from " + status);
        }
        this.executionId = executionId;
        this.status = CycleStatus.PENDING_C;
    }

    public void complete(String dispenseId) {
        if (status != CycleStatus.PENDING_C) {
            throw new IllegalStateException("Cannot complete from " + status);
        }
        this.dispenseId = dispenseId;
        this.status = CycleStatus.COMPLETED;
    }

    public void compensateAfterBFailure() {
        if (status != CycleStatus.PENDING_B) {
            throw new IllegalStateException("Cannot compensate-after-B from " + status);
        }
        this.status = CycleStatus.COMPENSATED_AFTER_B_FAILURE;
    }

    public void compensateAfterCFailure() {
        if (status != CycleStatus.PENDING_C) {
            throw new IllegalStateException("Cannot compensate-after-C from " + status);
        }
        this.status = CycleStatus.COMPENSATED_AFTER_C_FAILURE;
    }

    public String getId() { return id; }
    public String getZoneId() { return zoneId; }
    public String getScheduleId() { return scheduleId; }
    public String getExecutionId() { return executionId; }
    public String getDispenseId() { return dispenseId; }
    public CycleStatus getStatus() { return status; }

    @Override
    public String toString() {
        return "CycleRecord{id=%s, zone=%s, schedule=%s, execution=%s, dispense=%s, status=%s}"
                .formatted(id, zoneId, scheduleId, executionId, dispenseId, status);
    }
}
