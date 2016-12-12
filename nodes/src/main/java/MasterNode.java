import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Shunjie Ding on 12/12/2016.
 */
public class MasterNode {
    private static final Logger logger = Logger.getLogger(MasterNode.class.getName());

    public static void main(String... args) throws IOException, InterruptedException {
        int port = 8088;
        if (args.length == 0) {
            logger.log(Level.WARNING, "No port specified, using default port 8088.");
        } else {
            port = Integer.valueOf(args[0]);
        }
        MasterNodeServer server = new MasterNodeServer(port);
        server.startAndBlock();
    }
}
