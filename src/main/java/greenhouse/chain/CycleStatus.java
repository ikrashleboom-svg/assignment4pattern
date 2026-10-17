package greenhouse.chain;
public enum CycleStatus {
    PENDING_B,
    PENDING_C,
    COMPLETED,
    COMPENSATED_AFTER_B_FAILURE,
    COMPENSATED_AFTER_C_FAILURE
}
