package greenhouse.chain;

import greenhouse.store.CsvFileStore;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

public class ChainStateRepository {

    private static final String NULL_MARKER = "-";
    private final CsvFileStore store;

    public ChainStateRepository(Path dataDir) {
        this.store = new CsvFileStore(dataDir.resolve("cycles.csv"));
    }

    public void save(CycleRecord record) {
        List<String[]> rows = store.readAll();
        rows.removeIf(row -> row[0].equals(record.getId()));
        rows.add(new String[]{
                record.getId(),
                record.getZoneId(),
                record.getScheduleId(),
                orMarker(record.getExecutionId()),
                orMarker(record.getDispenseId()),
                record.getStatus().name()
        });
        store.writeAll(rows);
    }

    public Optional<CycleRecord> findById(String id) {
        return store.readAll().stream()
                .filter(row -> row[0].equals(id))
                .map(this::toEntity)
                .findFirst();
    }

    public List<CycleRecord> findAll() {
        return store.readAll().stream().map(this::toEntity).toList();
    }

    public void clear() {
        store.clear();
    }

    private String orMarker(String value) {
        return value == null ? NULL_MARKER : value;
    }

    private CycleRecord toEntity(String[] row) {
        return new CycleRecord(
                row[0],
                row[1],
                row[2],
                NULL_MARKER.equals(row[3]) ? null : row[3],
                NULL_MARKER.equals(row[4]) ? null : row[4],
                CycleStatus.valueOf(row[5])
        );
    }
}
