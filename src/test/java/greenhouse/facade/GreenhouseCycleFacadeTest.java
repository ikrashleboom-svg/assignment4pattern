package greenhouse.facade;

import greenhouse.chain.CycleRecord;
import greenhouse.exceptions.InvalidSensorReadingException;
import greenhouse.testkit.GreenhouseTestKit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
class GreenhouseCycleFacadeTest {

    @Test
    void happyPath_runsFullChainAndConfirmsDispense(@TempDir Path tempDir) {
        GreenhouseTestKit kit = new GreenhouseTestKit(tempDir);

        CycleRecord result = kit.aCycleRequest()
                .forZone("Zone-Happy")
                .withSoilMoisture(20)
                .withHumidity(40)
                .withNutrientTank(50)
                .run();

        kit.assertThat(result).completedSuccessfully().andDispenseIsConfirmed();
    }

    @Test
    void bFails_compensatesAAlone_depthOne(@TempDir Path tempDir) {
        GreenhouseTestKit kit = new GreenhouseTestKit(tempDir);

        CycleRecord result = kit.aCycleRequest()
                .forZone("Zone-B-Fails")
                .withSoilMoisture(20)
                .withHumidity(95)
                .withNutrientTank(50)
                .run();

        kit.assertThat(result)
                .wasCompensatedAfterBFailure()
                .andScheduleIsCancelled();
        assertTrue(result.getExecutionId() == null);
    }

    @Test
    void cFails_compensatesBThenA_depthTwo(@TempDir Path tempDir) {
        GreenhouseTestKit kit = new GreenhouseTestKit(tempDir);

        CycleRecord result = kit.aCycleRequest()
                .forZone("Zone-C-Fails")
                .withSoilMoisture(20)
                .withHumidity(40)       // safe -- B commits
                .withNutrientTank(2)    // too low -- C rejects
                .run();

        kit.assertThat(result)
                .wasCompensatedAfterCFailure()
                .andScheduleIsCancelled()
                .andExecutionIsReversed();
    }

    @Test
    void invalidMoistureReading_rejectedBeforeAnythingIsPersisted(@TempDir Path tempDir) {
        GreenhouseTestKit kit = new GreenhouseTestKit(tempDir);

        assertThrows(InvalidSensorReadingException.class, () ->
                kit.aCycleRequest()
                        .forZone("Zone-Bad-Sensor")
                        .withSoilMoisture(150) // impossible reading -- sensor fault
                        .run()
        );
        assertTrue(kit.schedules().findAll().isEmpty());
        assertTrue(kit.cycles().findAll().isEmpty());
    }
}
