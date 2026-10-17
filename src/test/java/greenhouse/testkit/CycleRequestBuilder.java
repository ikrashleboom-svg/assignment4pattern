package greenhouse.testkit;

import greenhouse.chain.CycleRecord;
import greenhouse.facade.GreenhouseCycleFacade;
public class CycleRequestBuilder {

    private final GreenhouseCycleFacade facade;
    private String zoneId = "Zone-Test";
    private double soilMoisture = 20.0;
    private double humidity = 40.0;
    private double nutrientTank = 50.0;

    CycleRequestBuilder(GreenhouseCycleFacade facade) {
        this.facade = facade;
    }

    public CycleRequestBuilder forZone(String zoneId) {
        this.zoneId = zoneId;
        return this;
    }

    public CycleRequestBuilder withSoilMoisture(double value) {
        this.soilMoisture = value;
        return this;
    }

    public CycleRequestBuilder withHumidity(double value) {
        this.humidity = value;
        return this;
    }

    public CycleRequestBuilder withNutrientTank(double value) {
        this.nutrientTank = value;
        return this;
    }

    public CycleRecord run() {
        return facade.runCycle(zoneId, soilMoisture, humidity, nutrientTank);
    }
}
