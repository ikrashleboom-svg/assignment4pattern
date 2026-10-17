package greenhouse.exceptions;
public class CycleNotFoundException extends GreenhouseException {
    public CycleNotFoundException(String cycleId) {
        super("No cycle found with id " + cycleId);
    }
}
