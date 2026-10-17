package greenhouse.facade;

import greenhouse.chain.ChainStateRepository;
import greenhouse.chain.CycleRecord;
import greenhouse.exceptions.NutrientUnavailableException;
import greenhouse.exceptions.VentilationUnsafeException;
import greenhouse.finalization.FinalizationService;
import greenhouse.intake.IntakeService;
import greenhouse.intake.IrrigationSchedule;
import greenhouse.processing.IrrigationExecution;
import greenhouse.processing.ProcessingService;

import java.nio.file.Path;
import java.util.UUID;
public class GreenhouseCycleFacade {

    private final IntakeService intakeService;
    private final ProcessingService processingService;
    private final FinalizationService finalizationService;
    private final ChainStateRepository chainStateRepository;

    public GreenhouseCycleFacade(Path dataDir) {
        this.intakeService = new IntakeService(dataDir);
        this.processingService = new ProcessingService(dataDir);
        this.finalizationService = new FinalizationService(dataDir);
        this.chainStateRepository = new ChainStateRepository(dataDir);
    }
    public CycleRecord runCycle(String zoneId, double moistureReading, double humidityLevel, double nutrientTankLevel) {
        IrrigationSchedule schedule = intakeService.intake(zoneId, moistureReading);

        CycleRecord record = new CycleRecord(UUID.randomUUID().toString(), zoneId, schedule.getId());
        chainStateRepository.save(record);
        IrrigationExecution execution;
        try {
            execution = processingService.process(schedule.getId(), humidityLevel);
        } catch (VentilationUnsafeException e) {
            intakeService.cancel(schedule.getId());
            record.compensateAfterBFailure();
            chainStateRepository.save(record);
            return record;
        }

        record.moveToPendingC(execution.getId());
        chainStateRepository.save(record);
        try {
            var dispense = finalizationService.finalizeCycle(execution.getId(), nutrientTankLevel);
            record.complete(dispense.getId());
            chainStateRepository.save(record);
            return record;
        } catch (NutrientUnavailableException e) {
            processingService.reverse(execution.getId());
            intakeService.cancel(schedule.getId());
            record.compensateAfterCFailure();
            chainStateRepository.save(record);
            return record;
        }
    }

    public ChainStateRepository getChainStateRepository() {
        return chainStateRepository;
    }
}
