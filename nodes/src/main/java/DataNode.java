import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.redisson.config.Config;

import java.io.IOException;
import java.util.UUID;
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
        options.addOption("n", "name", true, "Name of the server");
        options.addOption("d", "dir", true, "Directory of the file store");
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, args);
        int port = 10088;
        String name = UUID.randomUUID().toString();
        if (cmd.hasOption("t")) {
            port = Integer.valueOf(cmd.getOptionValue("t"));
        } else {
            logger.info("No port specified, using default port 8088.");
        }
        if (cmd.hasOption("n")) {
            name = cmd.getOptionValue("n");
        } else {
            logger.info("Using random server name " + name);
        }
        String directoryPath = "/tmp/" + name;
        if (cmd.hasOption("d")) {
            directoryPath = cmd.getOptionValue("d");
        } else {
            logger.info("Files are stored at " + directoryPath);
        }
        Config redisConfig = new Config();
        redisConfig.useSingleServer().setAddress("localhost:6379");
        DataNodeServer server = new DataNodeServer(port, directoryPath, redisConfig);
        server.startAndBlock();
    }
}
