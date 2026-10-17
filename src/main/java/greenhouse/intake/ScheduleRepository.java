package greenhouse.intake;

import greenhouse.store.CsvFileStore;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
public class ScheduleRepository {

    private final CsvFileStore store;

    public ScheduleRepository(Path dataDir) {
        this.store = new CsvFileStore(dataDir.resolve("schedules.csv"));
    }

    public void save(IrrigationSchedule schedule) {
        List<String[]> rows = store.readAll();
        rows.removeIf(row -> row[0].equals(schedule.getId()));
        rows.add(new String[]{
                schedule.getId(),
                schedule.getZoneId(),
                String.valueOf(schedule.getMoistureReading()),
                schedule.getStatus().name()
        });
        store.writeAll(rows);
    }

    public Optional<IrrigationSchedule> findById(String id) {
        return store.readAll().stream()
                .filter(row -> row[0].equals(id))
                .map(this::toEntity)
                .findFirst();
    }

    public List<IrrigationSchedule> findAll() {
        return store.readAll().stream().map(this::toEntity).toList();
    }

    public void clear() {
        store.clear();
    }

    private IrrigationSchedule toEntity(String[] row) {
        return new IrrigationSchedule(
                row[0],
                row[1],
                Double.parseDouble(row[2]),
                ScheduleStatus.valueOf(row[3])
        );
    }
}
