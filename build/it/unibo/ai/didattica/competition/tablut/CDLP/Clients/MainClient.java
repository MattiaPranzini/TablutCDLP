package it.unibo.ai.didattica.competition.tablut.CDLP.Clients;

import java.io.IOException;
import java.net.UnknownHostException;

import it.unibo.ai.didattica.competition.tablut.client.TablutClient;

/**
 *
 * @author L. Corsaro, D. De Nardi, S. Lia, M. Pranzini
 *
 */

public class MainClient {
	public static void main(String[] args) throws UnknownHostException, IOException, ClassNotFoundException {

		if (args.length != 3) {
			System.out.println("Usage: role timeout(in seconds) serverIP");
			System.exit(-1);
		} else {
			TablutClient client = new TablutNewClient(args[0].toUpperCase(), Math.round(Float.parseFloat(args[1])) - 2,
					args[2]);
			client.run();
		}

	}
}
