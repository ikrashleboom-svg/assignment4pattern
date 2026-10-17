# Assignment 4 — Facade Over a Three-Stage Chain

Domain: Smart Greenhouse — an irrigation cycle that goes through three independent subsystems.

## What this app does

One operation — "run an irrigation cycle for a zone" — actually touches three separate subsystems:

- **A (intake):** a soil sensor reading comes in, gets validated, and an irrigation schedule is committed.
- **B (processing):** the irrigation actually runs, but only if the ventilation system says the humidity is safe.
- **C (finalization):** nutrients get dispensed and the cycle is confirmed, but only if the nutrient tank has enough left.

Each step can fail, and by the time it fails, earlier steps have already been written to disk. The whole point of this assignment is handling that correctly: noticing a mid-chain failure from what's actually persisted, and rolling back exactly the right part of the chain.

## Project structure

```
assignment4-facade-pattern/
  ├── src/main/java/greenhouse/
  │    ├── intake/         (Subsystem A)
  │    ├── processing/      (Subsystem B)
  │    ├── finalization/    (Subsystem C)
  │    ├── chain/           (chain progress — not a subsystem, belongs to the facade layer)
  │    ├── store/           (shared file-persistence helper)
  │    ├── exceptions/
  │    ├── facade/          (the two facades)
  │    └── app/             (CLI demo)
  ├── src/test/java/greenhouse/
  │    ├── facade/          (chain tests, compensation tests, persistence-reload test)
  │    ├── intake/ processing/ finalization/  (standalone subsystem tests)
  │    └── testkit/         (small test DSL)
  ├── diagrams/              (UML class diagram)
  ├── README.md
  └── pom.xml
```

## How to run

```bash
mvn package
java -jar target/assignment4.jar
```

This opens a console menu. Everything gets persisted as plain CSV files under `./data/` — stop the app (Ctrl+C) and start it again, and whatever was there is still there.

To run the tests (JUnit 5) and generate a coverage report:

```bash
mvn test
```

JaCoCo is wired into the build (see `pom.xml`) and writes its report to `target/site/jacoco/index.html` after `mvn test`.

### Resetting the store

Option `6` in the app's menu wipes everything in `./data/`. You can also just delete the `data/` folder by hand and it gets recreated empty on the next run.

## The three subsystems

**Subsystem A — intake** (`greenhouse.intake`): `IrrigationSchedule` is the entity (status `NEW` → `SCHEDULED` → `CANCELLED`, and it enforces those transitions itself — you can't cancel something that was never scheduled). `MoistureEvaluator` is the pure business-logic class, separate from the entity, that decides if a reading is physically valid and whether the soil is dry enough to need water. `ScheduleRepository` is the persistence gateway — nothing outside this package touches the schedules file directly. `IntakeService` wires those two together and is what everything else calls.

**Subsystem B — processing** (`greenhouse.processing`): same shape. `IrrigationExecution` is the entity (`PENDING` → `EXECUTED`/`FAILED`, and `EXECUTED` → `REVERSED` for compensation). `VentilationSafetyChecker` decides if humidity is safe enough to irrigate. `ExecutionRepository` is the gateway. `ProcessingService` ties it together, plus a `reverse()` method used for compensation.

**Subsystem C — finalization** (`greenhouse.finalization`): `NutrientDispense` is the entity (`PENDING` → `CONFIRMED`/`CANCELLED`). `NutrientLevelValidator` checks the tank has enough left. `DispenseRepository` is the gateway. `FinalizationService` ties it together.

None of these three packages import or call each other. They only meet inside the facade. I checked each one works completely on its own with a standalone test (`IntakeServiceStandaloneTest`, `ProcessingServiceStandaloneTest`, `FinalizationServiceStandaloneTest`) that never touches a facade class.

## The two facades

**`GreenhouseCycleFacade`** — the normal client-facing one. One method, `runCycle(zoneId, moisture, humidity, nutrientTank)`, drives A → B → C and persists the chain's own progress (`CycleRecord`, with its own status: `PENDING_B`, `PENDING_C`, `COMPLETED`, or one of the two compensated states). This is where the real sequencing and compensation logic actually lives, not a one-line delegate.

**`GreenhouseAdminFacade`** — a narrower one for inspection and manual recovery. `listAllCycles()` and `inspect()` read what's on disk; `manuallyCompensate()` looks at a cycle's persisted status and, if it's stuck mid-chain (the app crashed between steps), unwinds whatever already committed. It makes that decision purely from `CycleRecord.getStatus()` read off disk — never from anything held in memory, which is what makes it work correctly after a restart.

## The two compensation depths

- **B fails after A committed** (humidity too high): the facade cancels the schedule from A. Nothing from B was ever marked `EXECUTED`, so there's nothing there to undo.
- **C fails after A and B committed** (nutrient tank too low): the facade reverses B's execution first, then cancels A's schedule — in that order, a real two-step rollback, not a single undo.

Both are reachable from the CLI (menu options 2 and 3 run them directly with preset values; option 1 lets you trigger them yourself with any input).

## Principle of Least Knowledge — before and after

**Before (what it would look like without the facade):** the client (the CLI) would have to know about `IntakeService`, `ProcessingService`, `FinalizationService`, `ChainStateRepository`, *and* the three entity classes (`IrrigationSchedule`, `IrrigationExecution`, `NutrientDispense`) just to run one cycle, plus write its own try/catch around each step to decide what to compensate and in what order:

```java
IrrigationSchedule schedule = intakeService.intake(zoneId, moisture);
CycleRecord record = new CycleRecord(id, zoneId, schedule.getId());
chainStateRepository.save(record);
try {
    IrrigationExecution execution = processingService.process(schedule.getId(), humidity);
    try {
        NutrientDispense dispense = finalizationService.finalizeCycle(execution.getId(), nutrient);
        record.complete(dispense.getId());
    } catch (NutrientUnavailableException e) {
        processingService.reverse(execution.getId());
        intakeService.cancel(schedule.getId());
        record.compensateAfterCFailure();
    }
} catch (VentilationUnsafeException e) {
    intakeService.cancel(schedule.getId());
    record.compensateAfterBFailure();
}
chainStateRepository.save(record);
```

That's six classes the client has to know by name, and the compensation order is the client's problem.

**After (with the facade):** the client talks to exactly one object:

```java
CycleRecord result = cycleFacade.runCycle(zoneId, moisture, humidity, nutrientTank);
```

One friend (`GreenhouseCycleFacade`), one call, and the compensation ordering is the facade's responsibility, not the caller's. `GreenhouseApp` (the CLI) never imports `IntakeService`, `ProcessingService`, `FinalizationService`, or any entity class — only the two facades and `CycleRecord` as the return type.

## Facade vs. Adapter

An Adapter exists because two interfaces don't match — it translates calls from the shape a client expects into the shape some existing class actually has, usually wrapping one thing. That's not what's happening here. `IntakeService`, `ProcessingService`, and `FinalizationService` aren't incompatible with each other — there's no interface mismatch to translate. They're three independent subsystems that each do their own job correctly, and the problem is that *running them together, in order, with the right rollback on failure* is logic that doesn't belong to any one of them.

That's a Facade's job by intent: not translating an interface, but giving a client a single simplified entry point over several subsystems it shouldn't have to know about individually, while the facade itself owns the orchestration (sequencing, compensation order, persistence of chain progress). If I used an Adapter here, I'd need three of them (one per subsystem) and the orchestration logic would still have nowhere to live — an Adapter doesn't add new coordinating behavior, it only reshapes an existing interface. A Facade is the pattern that's actually about reducing how many things a client has to know and coordinating multiple collaborators behind one call, which is exactly the problem this assignment is about.

## Testing

Tests use JUnit 5. Every test method takes a JUnit `@TempDir`, so each one starts from its own empty, isolated data directory — no test can see another test's files, and running them in any order gives the same result (Independent, Repeatable). Every test is a plain assertion, no manual inspection needed (Self-Validating), and they run in milliseconds since it's just local file I/O (Fast).

`greenhouse.testkit` has a small DSL (`GreenhouseTestKit`, `CycleRequestBuilder`, `CycleAssertions`) so a test reads like a sentence, e.g.:

```java
CycleRecord result = kit.aCycleRequest()
        .forZone("Zone-B-Fails")
        .withSoilMoisture(20)
        .withHumidity(95)
        .withNutrientTank(50)
        .run();

kit.assertThat(result).wasCompensatedAfterBFailure().andScheduleIsCancelled();
```

Both compensation depths have their own dedicated test (`bFails_compensatesAAlone_depthOne`, `cFails_compensatesBThenA_depthTwo`), plus a persistence-reload test (`PersistenceReloadTest`) that simulates a crash mid-chain, opens a brand-new set of facades over the same data directory, and checks the admin facade resumes correctly purely from what's on disk. Coverage is measured with JaCoCo (`mvn test`, report at `target/site/jacoco/index.html`) and is targeted at 80%+ across all three subsystems and both facades.
