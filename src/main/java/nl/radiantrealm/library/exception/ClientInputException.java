package nl.radiantrealm.library.exception;

public class ClientInputException extends ClientException {
    public ClientInputException(String messsage) {
        super(messsage);
    }

    public ClientInputException(String messsage, Throwable cause) {
        super(messsage, cause);
    }
}
