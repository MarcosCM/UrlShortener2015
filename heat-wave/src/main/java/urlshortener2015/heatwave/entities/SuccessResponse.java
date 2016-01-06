package urlshortener2015.heatwave.entities;

/**
 * Represents a JSON Success Response that follows the standard.
 */
public class SuccessResponse<T> extends JsonResponse {

    private T data;

    public SuccessResponse(){
        super("success");
    }

    public SuccessResponse(T data){
        super("success");
        this.data = data;
    }
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
