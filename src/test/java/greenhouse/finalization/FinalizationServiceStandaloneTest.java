package greenhouse.finalization;

import greenhouse.exceptions.NutrientUnavailableException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
class FinalizationServiceStandaloneTest {

    @Test
    void finalizeCycle_enoughNutrient_confirmsAndPersists(@TempDir Path tempDir) {
        FinalizationService service = new FinalizationService(tempDir);

        NutrientDispense dispense = service.finalizeCycle("fake-execution-id", 50.0);

        assertEquals(DispenseStatus.CONFIRMED, dispense.getStatus());
        assertEquals(DispenseStatus.CONFIRMED,
                service.getRepository().findById(dispense.getId()).orElseThrow().getStatus());
    }

    @Test
    void finalizeCycle_tankTooLow_throwsAndPersistsAsCancelled(@TempDir Path tempDir) {
        FinalizationService service = new FinalizationService(tempDir);

        assertThrows(NutrientUnavailableException.class, () -> service.finalizeCycle("fake-execution-id", 2.0));

        var all = service.getRepository().findAll();
        assertEquals(1, all.size());
        assertEquals(DispenseStatus.CANCELLED, all.get(0).getStatus());
    }

    @Test
    void nutrientLevelValidator_rejectsLowTank() {
        NutrientLevelValidator validator = new NutrientLevelValidator();

        assertTrue(validator.hasEnoughNutrient(50.0));
        assertFalse(validator.hasEnoughNutrient(5.0));
    }

    @Test
    void dispense_cannotBeConfirmedTwice() {
        NutrientDispense dispense = new NutrientDispense("id-1", "exec-1", 50.0);
        dispense.confirm();

        assertThrows(IllegalStateException.class, dispense::confirm);
    }
}
