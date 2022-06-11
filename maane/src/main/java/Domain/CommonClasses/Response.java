package Domain.CommonClasses;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Response<T> {

    private final T result;
    private final boolean isFailure;
    private final String errMsg;

    public Response(T result, boolean isFailure, String errMsg) {
        this.result = result;
        this.isFailure = isFailure;
        this.errMsg = errMsg;
    }

    public T getResult() {
        return result;
    }

    public boolean isFailure() {
        return isFailure;
    }

    public String getErrMsg() {
        return errMsg;
    }

}
