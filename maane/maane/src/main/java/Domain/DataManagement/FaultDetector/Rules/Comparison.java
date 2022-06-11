package Domain.DataManagement.FaultDetector.Rules;

public enum Comparison {
    GREATER_THAN("GREATER_THAN"),
    LESS_THAN("LESS_THAN"),
    EQUAL("EQUAL"),
    NONE("NONE");

    private final String compare;

    Comparison(String compare){
        this.compare = compare;
    }

    public String getComparison(){
        return this.compare;
    }
}
