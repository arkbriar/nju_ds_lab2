package file.exceptions;

/**
 * Created by Shunjie Ding on 13/12/2016.
 */
public class FileAlreadyExistsException extends Exception {
    public FileAlreadyExistsException(String message) {
        super(message);
    }
}
