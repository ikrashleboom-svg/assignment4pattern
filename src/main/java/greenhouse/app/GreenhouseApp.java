package greenhouse.app;

import greenhouse.chain.CycleRecord;
import greenhouse.exceptions.GreenhouseException;
import greenhouse.facade.GreenhouseAdminFacade;
import greenhouse.facade.GreenhouseCycleFacade;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Stream;
public class GreenhouseApp {

    private static final Path DATA_DIR = Path.of("data");

    public static void main(String[] args) {
        GreenhouseCycleFacade cycleFacade = new GreenhouseCycleFacade(DATA_DIR);
        GreenhouseAdminFacade adminFacade = new GreenhouseAdminFacade(DATA_DIR);
        Scanner scanner = new Scanner(System.in);

        System.out.println("=== Smart Greenhouse -- Facade Demo ===");
        boolean running = true;
        while (running) {
            printMenu();
            String choice = scanner.nextLine().trim();
            try {
                switch (choice) {
                    case "1" -> runCustomCycle(cycleFacade, scanner);
                    case "2" -> {
                        System.out.println("Running demo: B fails (humidity too high) after A commits...");
                        CycleRecord r = cycleFacade.runCycle("Zone-Demo-B", 20.0, 95.0, 50.0);
                        System.out.println("Result: " + r);
                    }
                    case "3" -> {
                        System.out.println("Running demo: C fails (nutrient tank empty) after A and B commit...");
                        CycleRecord r = cycleFacade.runCycle("Zone-Demo-C", 20.0, 40.0, 2.0);
                        System.out.println("Result: " + r);
                    }
                    case "4" -> inspectState(adminFacade);
                    case "5" -> manualCompensation(adminFacade, scanner);
                    case "6" -> resetStore();
                    case "0" -> running = false;
                    default -> System.out.println("Unknown option.");
                }
            } catch (GreenhouseException e) {
                System.out.println("Rejected: " + e.getMessage());
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
            System.out.println();
        }
        System.out.println("Bye.");
    }

    private static void printMenu() {
        System.out.println("""
                1) Run irrigation cycle (custom input)
                2) Demo: force B failure (ventilation unsafe)
                3) Demo: force C failure (nutrient tank empty)
                4) Inspect persisted state
                5) Manually compensate a stuck cycle (admin)
                6) Reset store (wipe all persisted data)
                0) Exit
                Choose:\
                """);
    }

    private static void runCustomCycle(GreenhouseCycleFacade facade, Scanner scanner) {
        System.out.print("Zone id: ");
        String zoneId = scanner.nextLine().trim();
        double moisture = readDouble(scanner, "Soil moisture % (0-100): ");
        double humidity = readDouble(scanner, "Humidity % (0-100, >=85 fails B): ");
        double nutrient = readDouble(scanner, "Nutrient tank % (0-100, <10 fails C): ");

        CycleRecord record = facade.runCycle(zoneId, moisture, humidity, nutrient);
        System.out.println("Result: " + record);
    }

    private static double readDouble(Scanner scanner, String prompt) {
        System.out.print(prompt);
        return Double.parseDouble(scanner.nextLine().trim());
    }

    private static void inspectState(GreenhouseAdminFacade adminFacade) {
        List<CycleRecord> cycles = adminFacade.listAllCycles();
        if (cycles.isEmpty()) {
            System.out.println("No cycles persisted yet.");
            return;
        }
        System.out.println("Persisted cycles:");
        cycles.forEach(c -> System.out.println("  " + c));
    }

    private static void manualCompensation(GreenhouseAdminFacade adminFacade, Scanner scanner) {
        System.out.print("Cycle id to inspect/compensate: ");
        String cycleId = scanner.nextLine().trim();
        CycleRecord before = adminFacade.inspect(cycleId);
        System.out.println("Before: " + before);
        CycleRecord after = adminFacade.manuallyCompensate(cycleId);
        System.out.println("After:  " + after);
    }

    private static void resetStore() {
        try (Stream<Path> files = Files.exists(DATA_DIR) ? Files.list(DATA_DIR) : Stream.empty()) {
            files.forEach(f -> {
                try {
                    Files.deleteIfExists(f);
                } catch (IOException e) {
                    System.out.println("Could not delete " + f + ": " + e.getMessage());
                }
            });
            System.out.println("Store reset -- all data files cleared.");
        } catch (IOException e) {
            System.out.println("Could not reset store: " + e.getMessage());
        }
    }
}
