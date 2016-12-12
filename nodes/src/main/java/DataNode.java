import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Shunjie Ding on 12/12/2016.
 */
public class DataNode {
    private static final Logger logger = Logger.getLogger(DataNode.class.getName());

    public static void main(String... args) throws IOException, InterruptedException {
        if (args.length == 0) {
            logger.log(Level.WARNING, "First param should be a port number.");
            System.exit(1);
        }
        int port = Integer.valueOf(args[0]);
        DataNodeServer server = new DataNodeServer(port);
        server.start();
        server.blockUntilShutdown();
    }
}
