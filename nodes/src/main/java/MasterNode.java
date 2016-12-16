import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.redisson.config.Config;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Shunjie Ding on 12/12/2016.
 */
public class MasterNode {
    private static final Logger logger = Logger.getLogger(MasterNode.class.getName());

    public static void main(String... args)
        throws IOException, InterruptedException, ParseException {
        Options options = new Options();
        options.addOption("p", "port", true, "Port of the service");
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, args);
        int port = 8088;
        if (cmd.hasOption("t")) {
            port = Integer.valueOf(cmd.getOptionValue("t"));
        } else {
            logger.log(Level.WARNING, "No port specified, using default port 8088.");
        }
        Config redisConfig = new Config();
        redisConfig.useSingleServer().setAddress("localhost:6379");
        MasterNodeServer server = new MasterNodeServer(port, redisConfig);
        server.startAndBlock();
    }
}
