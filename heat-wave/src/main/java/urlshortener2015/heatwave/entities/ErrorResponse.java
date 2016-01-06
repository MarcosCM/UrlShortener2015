package urlshortener2015.heatwave.entities;

/**
 * Represents a JSON ERROR Response that follows the standard.
 */
public class ErrorResponse  extends JsonResponse{

    private String message;

    public ErrorResponse(){
        super("error");
    }

    public ErrorResponse(String message){
        super("error");
        this.message = message;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}