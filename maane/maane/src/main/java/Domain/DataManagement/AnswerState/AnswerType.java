package Domain.DataManagement.AnswerState;

public enum AnswerType {
    OPEN_ANSWER("OPEN_ANSWER"),
    NUMERIC_ANSWER("NUMERIC_ANSWER"),
    MULTIPLE_CHOICE("MULTIPLE_CHOICE"),
    NONE("NONE");

    private final String type;

    AnswerType(String type){
        this.type = type;
    }

    public String getType(){
        return this.type;
    }

}
