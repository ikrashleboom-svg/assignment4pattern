package greenhouse.testkit;

import greenhouse.chain.ChainStateRepository;
import greenhouse.chain.CycleRecord;
import greenhouse.facade.GreenhouseAdminFacade;
import greenhouse.facade.GreenhouseCycleFacade;
import greenhouse.finalization.DispenseRepository;
import greenhouse.intake.IntakeService;
import greenhouse.intake.IrrigationSchedule;
import greenhouse.intake.ScheduleRepository;
import greenhouse.processing.ExecutionRepository;
import greenhouse.processing.IrrigationExecution;
import greenhouse.processing.ProcessingService;

import java.nio.file.Path;
import java.util.UUID;
public class GreenhouseTestKit {

    private final Path dataDir;
    private final GreenhouseCycleFacade cycleFacade;
    private final GreenhouseAdminFacade adminFacade;
    private final ScheduleRepository schedules;
    private final ExecutionRepository executions;
    private final DispenseRepository dispenses;
    private final ChainStateRepository cycles;

    public GreenhouseTestKit(Path dataDir) {
        this.dataDir = dataDir;
        this.cycleFacade = new GreenhouseCycleFacade(dataDir);
        this.adminFacade = new GreenhouseAdminFacade(dataDir);
        this.schedules = new ScheduleRepository(dataDir);
        this.executions = new ExecutionRepository(dataDir);
        this.dispenses = new DispenseRepository(dataDir);
        this.cycles = new ChainStateRepository(dataDir);
    }

    public CycleRequestBuilder aCycleRequest() {
        return new CycleRequestBuilder(cycleFacade);
    }

    public CycleAssertions assertThat(CycleRecord record) {
        return new CycleAssertions(record, this);
    }

    public GreenhouseCycleFacade cycleFacade() { return cycleFacade; }
    public GreenhouseAdminFacade adminFacade() { return adminFacade; }
    public ScheduleRepository schedules() { return schedules; }
    public ExecutionRepository executions() { return executions; }
    public DispenseRepository dispenses() { return dispenses; }
    public ChainStateRepository cycles() { return cycles; }
    public GreenhouseTestKit restarted() {
        return new GreenhouseTestKit(dataDir);
    }
    public CycleRecord simulateCrashAfterStepB(String zoneId, double moisture, double humidity) {
        IntakeService intake = new IntakeService(dataDir);
        ProcessingService processing = new ProcessingService(dataDir);

        IrrigationSchedule schedule = intake.intake(zoneId, moisture);
        IrrigationExecution execution = processing.process(schedule.getId(), humidity);

        CycleRecord record = new CycleRecord(UUID.randomUUID().toString(), zoneId, schedule.getId());
        record.moveToPendingC(execution.getId());
        cycles.save(record);
        return record;
    }
}
