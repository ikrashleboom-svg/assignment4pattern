package greenhouse.processing;
public class VentilationSafetyChecker {

    private static final double HUMIDITY_SAFE_LIMIT = 85.0;

    public boolean isSafeToIrrigate(double humidityLevel) {
        return humidityLevel < HUMIDITY_SAFE_LIMIT;
    }
}
