package greenhouse.finalization;

import greenhouse.exceptions.NutrientUnavailableException;

import java.nio.file.Path;
import java.util.UUID;
public class FinalizationService {

    private final NutrientLevelValidator validator;
    private final DispenseRepository repository;

    public FinalizationService(Path dataDir) {
        this.validator = new NutrientLevelValidator();
        this.repository = new DispenseRepository(dataDir);
    }
    public NutrientDispense finalizeCycle(String executionId, double tankLevel) {
        NutrientDispense dispense = new NutrientDispense(UUID.randomUUID().toString(), executionId, tankLevel);
        if (!validator.hasEnoughNutrient(tankLevel)) {
            dispense.cancel();
            repository.save(dispense);
            throw new NutrientUnavailableException(
                    "Nutrient tank too low to dispense: " + tankLevel + "%");
        }
        dispense.confirm();
        repository.save(dispense);
        return dispense;
    }

    public DispenseRepository getRepository() {
        return repository;
    }
}
