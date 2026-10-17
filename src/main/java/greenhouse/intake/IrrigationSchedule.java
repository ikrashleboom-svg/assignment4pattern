package greenhouse.intake;
public class IrrigationSchedule {

    private final String id;
    private final String zoneId;
    private final double moistureReading;
    private ScheduleStatus status;

    public IrrigationSchedule(String id, String zoneId, double moistureReading) {
        this.id = id;
        this.zoneId = zoneId;
        this.moistureReading = moistureReading;
        this.status = ScheduleStatus.NEW;
    }
    public IrrigationSchedule(String id, String zoneId, double moistureReading, ScheduleStatus status) {
        this.id = id;
        this.zoneId = zoneId;
        this.moistureReading = moistureReading;
        this.status = status;
    }

    public void markScheduled() {
        if (status != ScheduleStatus.NEW) {
            throw new IllegalStateException("Cannot schedule from status " + status);
        }
        status = ScheduleStatus.SCHEDULED;
    }

    public void cancel() {
        if (status != ScheduleStatus.SCHEDULED) {
            throw new IllegalStateException("Cannot cancel from status " + status);
        }
        status = ScheduleStatus.CANCELLED;
    }

    public String getId() { return id; }
    public String getZoneId() { return zoneId; }
    public double getMoistureReading() { return moistureReading; }
    public ScheduleStatus getStatus() { return status; }

    @Override
    public String toString() {
        return "IrrigationSchedule{id=%s, zone=%s, moisture=%.1f, status=%s}"
                .formatted(id, zoneId, moistureReading, status);
    }
}
