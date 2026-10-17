package greenhouse.facade;

import greenhouse.chain.CycleRecord;
import greenhouse.chain.CycleStatus;
import greenhouse.testkit.GreenhouseTestKit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
class PersistenceReloadTest {

    @Test
    void afterSimulatedRestart_adminFacadeResumesFromPersistedPendingC(@TempDir Path tempDir) {
        GreenhouseTestKit kit = new GreenhouseTestKit(tempDir);

        CycleRecord stuckRecord = kit.simulateCrashAfterStepB("Zone-Restart", 20.0, 40.0);
        assertEquals(CycleStatus.PENDING_C, stuckRecord.getStatus());

        GreenhouseTestKit afterRestart = kit.restarted();

        CycleRecord readBack = afterRestart.adminFacade().inspect(stuckRecord.getId());
        assertEquals(CycleStatus.PENDING_C, readBack.getStatus(),
                "the restarted facade must read the same status straight off disk");

        CycleRecord compensated = afterRestart.adminFacade().manuallyCompensate(stuckRecord.getId());

        afterRestart.assertThat(compensated)
                .wasCompensatedAfterCFailure()
                .andScheduleIsCancelled()
                .andExecutionIsReversed();
    }

    @Test
    void afterSimulatedRestart_completedCycleIsLeftUntouched(@TempDir Path tempDir) {
        GreenhouseTestKit kit = new GreenhouseTestKit(tempDir);

        CycleRecord completed = kit.aCycleRequest()
                .forZone("Zone-Already-Done")
                .withSoilMoisture(20)
                .withHumidity(40)
                .withNutrientTank(50)
                .run();
        kit.assertThat(completed).completedSuccessfully();

        GreenhouseTestKit afterRestart = kit.restarted();
        CycleRecord result = afterRestart.adminFacade().manuallyCompensate(completed.getId());

        assertEquals(CycleStatus.COMPLETED, result.getStatus(),
                "an already-completed cycle must not be touched by manual compensation");
    }
}
