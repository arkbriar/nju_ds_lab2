import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Shunjie Ding on 12/12/2016.
 */
public class DataNode {
    private static final Logger logger = Logger.getLogger(DataNode.class.getName());

    public static void main(String... args)
        throws IOException, InterruptedException, ParseException {
        Options options = new Options();
        options.addOption("p", "port", true, "Port of the service");
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, args);
        int port = 10088;
        if (cmd.hasOption("t")) {
            port = Integer.valueOf(cmd.getOptionValue("t"));
        } else {
            logger.log(Level.WARNING, "No port specified, using default port 8088.");
        }
        DataNodeServer server = new DataNodeServer(port, null);
        server.startAndBlock();
    }
}
