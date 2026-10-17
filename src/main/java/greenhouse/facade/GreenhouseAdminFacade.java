package greenhouse.facade;

import greenhouse.chain.ChainStateRepository;
import greenhouse.chain.CycleRecord;
import greenhouse.chain.CycleStatus;
import greenhouse.exceptions.CycleNotFoundException;
import greenhouse.finalization.FinalizationService;
import greenhouse.intake.IntakeService;
import greenhouse.processing.ProcessingService;

import java.nio.file.Path;
import java.util.List;
public class GreenhouseAdminFacade {

    private final IntakeService intakeService;
    private final ProcessingService processingService;
    private final ChainStateRepository chainStateRepository;

    public GreenhouseAdminFacade(Path dataDir) {
        this.intakeService = new IntakeService(dataDir);
        this.processingService = new ProcessingService(dataDir);
        this.chainStateRepository = new ChainStateRepository(dataDir);
    }

    public List<CycleRecord> listAllCycles() {
        return chainStateRepository.findAll();
    }

    public CycleRecord inspect(String cycleId) {
        return chainStateRepository.findById(cycleId)
                .orElseThrow(() -> new CycleNotFoundException(cycleId));
    }
    public CycleRecord manuallyCompensate(String cycleId) {
        CycleRecord record = inspect(cycleId);

        if (record.getStatus() == CycleStatus.PENDING_C) {
            processingService.reverse(record.getExecutionId());
            intakeService.cancel(record.getScheduleId());
            record.compensateAfterCFailure();
            chainStateRepository.save(record);
            return record;
        }

        if (record.getStatus() == CycleStatus.PENDING_B) {
            intakeService.cancel(record.getScheduleId());
            record.compensateAfterBFailure();
            chainStateRepository.save(record);
            return record;
        }
        return record;
    }
}
