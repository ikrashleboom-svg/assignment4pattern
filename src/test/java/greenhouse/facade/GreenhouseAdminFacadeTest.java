package greenhouse.facade;

import greenhouse.exceptions.CycleNotFoundException;
import greenhouse.testkit.GreenhouseTestKit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GreenhouseAdminFacadeTest {

    @Test
    void listAllCycles_returnsEveryPersistedCycle(@TempDir Path tempDir) {
        GreenhouseTestKit kit = new GreenhouseTestKit(tempDir);

        kit.aCycleRequest().forZone("Zone-1").withSoilMoisture(20).withHumidity(40).withNutrientTank(50).run();
        kit.aCycleRequest().forZone("Zone-2").withSoilMoisture(20).withHumidity(95).withNutrientTank(50).run();

        List<?> all = kit.adminFacade().listAllCycles();
        assertEquals(2, all.size());
    }

    @Test
    void inspect_unknownCycleId_throwsCycleNotFound(@TempDir Path tempDir) {
        GreenhouseTestKit kit = new GreenhouseTestKit(tempDir);

        assertThrows(CycleNotFoundException.class, () -> kit.adminFacade().inspect("does-not-exist"));
    }

    @Test
    void listAllCycles_emptyStoreReturnsEmptyList(@TempDir Path tempDir) {
        GreenhouseTestKit kit = new GreenhouseTestKit(tempDir);

        assertTrue(kit.adminFacade().listAllCycles().isEmpty());
    }
}
