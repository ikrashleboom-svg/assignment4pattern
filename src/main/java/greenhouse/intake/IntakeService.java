package greenhouse.intake;

import java.nio.file.Path;
import java.util.UUID;
public class IntakeService {

    private final MoistureEvaluator evaluator;
    private final ScheduleRepository repository;

    public IntakeService(Path dataDir) {
        this.evaluator = new MoistureEvaluator();
        this.repository = new ScheduleRepository(dataDir);
    }
    public IrrigationSchedule intake(String zoneId, double moistureReading) {
        evaluator.validate(moistureReading);
        IrrigationSchedule schedule = new IrrigationSchedule(UUID.randomUUID().toString(), zoneId, moistureReading);
        schedule.markScheduled();
        repository.save(schedule);
        return schedule;
    }
    public void cancel(String scheduleId) {
        IrrigationSchedule schedule = repository.findById(scheduleId)
                .orElseThrow(() -> new IllegalArgumentException("No such schedule: " + scheduleId));
        schedule.cancel();
        repository.save(schedule);
    }

    public boolean isIrrigationNeeded(double moistureReading) {
        return evaluator.isIrrigationNeeded(moistureReading);
    }

    public ScheduleRepository getRepository() {
        return repository;
    }
}
