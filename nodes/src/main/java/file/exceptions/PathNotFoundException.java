package file.exceptions;

/**
 * Created by Shunjie Ding on 13/12/2016.
 */
public class PathNotFoundException extends FileSystemException {
    public PathNotFoundException(String message) {
        super(message);
    }
}
