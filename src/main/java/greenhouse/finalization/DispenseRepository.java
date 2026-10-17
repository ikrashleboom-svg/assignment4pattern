package greenhouse.finalization;

import greenhouse.store.CsvFileStore;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

public class DispenseRepository {

    private final CsvFileStore store;

    public DispenseRepository(Path dataDir) {
        this.store = new CsvFileStore(dataDir.resolve("dispenses.csv"));
    }

    public void save(NutrientDispense dispense) {
        List<String[]> rows = store.readAll();
        rows.removeIf(row -> row[0].equals(dispense.getId()));
        rows.add(new String[]{
                dispense.getId(),
                dispense.getExecutionId(),
                String.valueOf(dispense.getTankLevelAtDispense()),
                dispense.getStatus().name()
        });
        store.writeAll(rows);
    }

    public Optional<NutrientDispense> findById(String id) {
        return store.readAll().stream()
                .filter(row -> row[0].equals(id))
                .map(this::toEntity)
                .findFirst();
    }

    public List<NutrientDispense> findAll() {
        return store.readAll().stream().map(this::toEntity).toList();
    }

    public void clear() {
        store.clear();
    }

    private NutrientDispense toEntity(String[] row) {
        return new NutrientDispense(
                row[0],
                row[1],
                Double.parseDouble(row[2]),
                DispenseStatus.valueOf(row[3])
        );
    }
}
