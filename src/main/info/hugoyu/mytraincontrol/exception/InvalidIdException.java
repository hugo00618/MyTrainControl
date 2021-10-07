package info.hugoyu.mytraincontrol.exception;

public class InvalidIdException extends RuntimeException {

    public InvalidIdException(String id) {
        super("Invalid id: " + id);
    }

    public InvalidIdException(long id) {
        this(String.valueOf(id));
    }

}
