package greenhouse.intake;

import greenhouse.exceptions.InvalidSensorReadingException;
public class MoistureEvaluator {

    private static final double MIN_VALID = 0.0;
    private static final double MAX_VALID = 100.0;
    private static final double DRY_THRESHOLD = 35.0;
    public void validate(double moistureReading) {
        if (moistureReading < MIN_VALID || moistureReading > MAX_VALID) {
            throw new InvalidSensorReadingException(
                    "Soil moisture reading out of range: " + moistureReading + "% (sensor fault)");
        }
    }
    public boolean isIrrigationNeeded(double moistureReading) {
        return moistureReading < DRY_THRESHOLD;
    }
}
