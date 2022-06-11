package Domain.DataManagement.FaultDetector.Rules;

public enum RuleType {
    AND("AND"),
    IFF("IFF"),
    IMPLY("IMPLY"),
    MULTIPLE_CHOICE("MULTIPLE_CHOICE"),
    NUMERIC("NUMERIC"),
    OR("OR"),
    EMPTY("");

    private final String type;

    RuleType(String type){
        this.type = type;
    }

    public String getType(){
        return this.type;
    }

}
