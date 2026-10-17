package greenhouse.store;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
public class CsvFileStore {

    private final Path file;

    public CsvFileStore(Path file) {
        this.file = file;
        try {
            if (file.getParent() != null) {
                Files.createDirectories(file.getParent());
            }
            if (!Files.exists(file)) {
                Files.createFile(file);
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Could not initialize store file " + file, e);
        }
    }

    public List<String[]> readAll() {
        try {
            List<String> lines = Files.readAllLines(file);
            List<String[]> rows = new ArrayList<>();
            for (String line : lines) {
                if (line.isBlank()) continue;
                rows.add(line.split("\\|", -1));
            }
            return rows;
        } catch (IOException e) {
            throw new UncheckedIOException("Could not read store file " + file, e);
        }
    }

    public void writeAll(List<String[]> rows) {
        try {
            List<String> lines = rows.stream()
                    .map(row -> String.join("|", row))
                    .collect(Collectors.toList());
            Files.write(file, lines);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not write store file " + file, e);
        }
    }
    public void clear() {
        writeAll(new ArrayList<>());
    }
}
