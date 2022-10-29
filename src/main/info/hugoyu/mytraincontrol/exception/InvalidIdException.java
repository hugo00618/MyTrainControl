package info.hugoyu.mytraincontrol.exception;

public class InvalidIdException extends RuntimeException {

    public enum Type {
        NOT_FOUND("Id not found: "),
        DUPLICATE("Duplicate id: ");

        final String message;

        Type(String message) {
            this.message = message;
        }
    }

    public InvalidIdException(String id, Type type) {
        super(type.message + id);
    }

    public InvalidIdException(Number id, Type type) {
        this(String.valueOf(id), type);
    }

}
