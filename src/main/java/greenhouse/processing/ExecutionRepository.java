package greenhouse.processing;

import greenhouse.store.CsvFileStore;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
public class ExecutionRepository {

    private final CsvFileStore store;

    public ExecutionRepository(Path dataDir) {
        this.store = new CsvFileStore(dataDir.resolve("executions.csv"));
    }

    public void save(IrrigationExecution execution) {
        List<String[]> rows = store.readAll();
        rows.removeIf(row -> row[0].equals(execution.getId()));
        rows.add(new String[]{
                execution.getId(),
                execution.getScheduleId(),
                String.valueOf(execution.getHumidityLevel()),
                execution.getStatus().name()
        });
        store.writeAll(rows);
    }

    public Optional<IrrigationExecution> findById(String id) {
        return store.readAll().stream()
                .filter(row -> row[0].equals(id))
                .map(this::toEntity)
                .findFirst();
    }

    public List<IrrigationExecution> findAll() {
        return store.readAll().stream().map(this::toEntity).toList();
    }

    public void clear() {
        store.clear();
    }

    private IrrigationExecution toEntity(String[] row) {
        return new IrrigationExecution(
                row[0],
                row[1],
                Double.parseDouble(row[2]),
                ExecutionStatus.valueOf(row[3])
        );
    }
}
