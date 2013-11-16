package ar.edu.itba.pod.tp.referee;

import ar.edu.itba.pod.tp.interfaces.Master;
import ar.edu.itba.pod.tp.interfaces.Referee;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Scanner;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 * Hello world!
 *
 */
public class RefereeApp 
{
	
    public static void main( String[] args ) throws ParseException
    {
		final CommandLine cmdLine = parseArguments(args);
		final String host = cmdLine.getOptionValue(HOST_L, HOST_D);
		final int port = Integer.valueOf( cmdLine.getOptionValue(PORT_L, PORT_D));
		final String name = cmdLine.getOptionValue(NAME_L);
		
		if (cmdLine.hasOption("help")) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("server", options);
			System.exit(0);
		}
		try {
			System.out.println("Registering Referee " + name + " on master: " + host + ":" + port);
			final Registry registry = LocateRegistry.getRegistry(host, port);

			System.out.println("Loading Master");
			final Master master = (Master) registry.lookup("master");
			final int requestsTotal = master.getRequestsTotal();
			
			final RefereeServer server = new RefereeServer(name, requestsTotal, registry);
			System.out.println("Binding my referee");
			final Referee stub = (Referee) UnicastRemoteObject.exportObject(server, 0);
			registry.bind("referees/" + name, stub);

			master.registerReferee(stub);

			System.out.println("Press any key to start");
			Scanner scan = new Scanner(System.in);
			
			synchronized (server) {
				scan.nextLine();
				server.playing = true;
			}
			System.out.println("EMPEZAMOS!");
			String line;
			do {
				line = scan.next();
				System.out.println(server.showResults());
				
			} while(!"x".equals(line));
			System.exit(0);
		}
		catch (Exception e) {
			System.err.println("Server exception: " + e.toString());
			System.exit(-1);
		}    
	}

	private static CommandLine parseArguments(String[] args) throws ParseException
	{
		CommandLineParser parser = new BasicParser();
		try {
			// parse the command line arguments
			return parser.parse(options, args, false);
		}
		catch (ParseException exp) {
			// oops, something went wrong
			System.err.println("Parsing failed.  Reason: " + exp.getMessage());
			throw exp;
		}	
	}
	
	private static Options createOptions()
	{
		final Options result = new Options();
		final Option name = OptionBuilder.withArgName(NAME_S).withLongOpt(NAME_L).hasArg().withDescription("Player name").isRequired(true).create(NAME_S);
		result.addOption(name);
		result.addOption(HOST_S, HOST_L, true, "Master hHost");
		result.addOption(PORT_S, PORT_L, true, "Master Port");
		result.addOption("help", false, "Help");
		return result;
	}

	private static final String HOST_L = "host";
	private static final String HOST_S = "h";
	private static final String HOST_D = "localhost";
	private static final String PORT_L = "port";
	private static final String PORT_S = "p";
	private static final String PORT_D = "7242";
	private static final String NAME_L = "name";
	private static final String NAME_S = "n";
	private static Options options = createOptions();
}
