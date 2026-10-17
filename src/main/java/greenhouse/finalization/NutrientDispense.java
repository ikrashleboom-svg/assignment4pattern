package greenhouse.finalization;
public class NutrientDispense {

    private final String id;
    private final String executionId;
    private final double tankLevelAtDispense;
    private DispenseStatus status;

    public NutrientDispense(String id, String executionId, double tankLevelAtDispense) {
        this.id = id;
        this.executionId = executionId;
        this.tankLevelAtDispense = tankLevelAtDispense;
        this.status = DispenseStatus.PENDING;
    }

    public NutrientDispense(String id, String executionId, double tankLevelAtDispense, DispenseStatus status) {
        this.id = id;
        this.executionId = executionId;
        this.tankLevelAtDispense = tankLevelAtDispense;
        this.status = status;
    }

    public void confirm() {
        if (status != DispenseStatus.PENDING) {
            throw new IllegalStateException("Cannot confirm from status " + status);
        }
        status = DispenseStatus.CONFIRMED;
    }

    public void cancel() {
        if (status != DispenseStatus.PENDING) {
            throw new IllegalStateException("Cannot cancel from status " + status);
        }
        status = DispenseStatus.CANCELLED;
    }

    public String getId() { return id; }
    public String getExecutionId() { return executionId; }
    public double getTankLevelAtDispense() { return tankLevelAtDispense; }
    public DispenseStatus getStatus() { return status; }

    @Override
    public String toString() {
        return "NutrientDispense{id=%s, executionId=%s, tankLevel=%.1f, status=%s}"
                .formatted(id, executionId, tankLevelAtDispense, status);
    }
}
