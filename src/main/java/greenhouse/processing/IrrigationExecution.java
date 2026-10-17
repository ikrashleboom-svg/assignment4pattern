package greenhouse.processing;
public class IrrigationExecution {

    private final String id;
    private final String scheduleId;
    private final double humidityLevel;
    private ExecutionStatus status;

    public IrrigationExecution(String id, String scheduleId, double humidityLevel) {
        this.id = id;
        this.scheduleId = scheduleId;
        this.humidityLevel = humidityLevel;
        this.status = ExecutionStatus.PENDING;
    }

    public IrrigationExecution(String id, String scheduleId, double humidityLevel, ExecutionStatus status) {
        this.id = id;
        this.scheduleId = scheduleId;
        this.humidityLevel = humidityLevel;
        this.status = status;
    }

    public void markExecuted() {
        if (status != ExecutionStatus.PENDING) {
            throw new IllegalStateException("Cannot execute from status " + status);
        }
        status = ExecutionStatus.EXECUTED;
    }

    public void markFailed() {
        if (status != ExecutionStatus.PENDING) {
            throw new IllegalStateException("Cannot fail from status " + status);
        }
        status = ExecutionStatus.FAILED;
    }

    public void reverse() {
        if (status != ExecutionStatus.EXECUTED) {
            throw new IllegalStateException("Cannot reverse from status " + status);
        }
        status = ExecutionStatus.REVERSED;
    }

    public String getId() { return id; }
    public String getScheduleId() { return scheduleId; }
    public double getHumidityLevel() { return humidityLevel; }
    public ExecutionStatus getStatus() { return status; }

    @Override
    public String toString() {
        return "IrrigationExecution{id=%s, scheduleId=%s, humidity=%.1f, status=%s}"
                .formatted(id, scheduleId, humidityLevel, status);
    }
}
