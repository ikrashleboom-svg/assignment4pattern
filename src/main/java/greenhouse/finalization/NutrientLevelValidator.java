package greenhouse.finalization;
public class NutrientLevelValidator {

    private static final double MIN_TANK_LEVEL = 10.0;

    public boolean hasEnoughNutrient(double tankLevel) {
        return tankLevel >= MIN_TANK_LEVEL;
    }
}
