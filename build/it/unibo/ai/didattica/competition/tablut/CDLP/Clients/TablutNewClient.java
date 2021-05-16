package it.unibo.ai.didattica.competition.tablut.CDLP.Clients;

import it.unibo.ai.didattica.competition.tablut.CDLP.Minmax.Minmax;
import it.unibo.ai.didattica.competition.tablut.client.TablutClient;
import it.unibo.ai.didattica.competition.tablut.domain.Action;
import it.unibo.ai.didattica.competition.tablut.domain.State.Turn;

import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 *
 * @author L. Corsaro, D. De Nardi, S. Lia, M. Pranzini
 *
 */

public class TablutNewClient extends TablutClient {

	private final int timeOut;
	private final int depthLimit;
	private static final String NAME = "CDLP";

	public TablutNewClient(String player, int timeout, String ipAddress) throws UnknownHostException, IOException {
		super(player, NAME, timeout, ipAddress);
		this.timeOut = timeout;
		depthLimit = 4;
	}

	@Override
	public void run() {
		System.out.println("You are player " + this.getPlayer().toString() + "!");

		try {
			this.declareName();
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (this.getPlayer() == Turn.WHITE) {
			playAndWait(Turn.WHITE, Turn.BLACK, "YOU WIN!", "YOU LOSE!");
			return;
		} else {

			playAndWait(Turn.BLACK, Turn.WHITE, "YOU LOSE!", "YOU WIN!");
		}
	}

	private void playAndWait(Turn giocatore, Turn avversario, String messaggio1, String messaggio2) {
		
		Action action;
		while (true) {

			try {
				this.read();
				System.out.println("Stato attuale:");
				System.out.println(this.getCurrentState().toString());
			} catch (SocketException e) {
				System.out.println("Errore durante il recupero del nuovo stato ");
				e.printStackTrace();
				System.exit(1);
			} catch (Exception e) {
				System.out.println("Errore durante il recupero del nuovo stato, riprovo");
				e.printStackTrace();
			}

			try {
				if (this.getCurrentState().getTurn().equals(giocatore)) {

					long start = System.currentTimeMillis();
					
					Minmax minmax = new Minmax(depthLimit, getPlayer(), getCurrentState().clone());
					action = minmax.makeDecision(timeOut, getCurrentState().clone());
					this.write(action);
					
					long end = System.currentTimeMillis();
					System.out.println("Tempo impiegato: " + (end - start) + " ms");

				} else if (this.getCurrentState().getTurn().equals(avversario)) {
					System.out.println("Waiting for your opponent move... ");
				} else if (this.getCurrentState().getTurn().equals(Turn.WHITEWIN)) {
					System.out.println(messaggio1);
					System.exit(0);
				} else if (this.getCurrentState().getTurn().equals(Turn.BLACKWIN)) {
					System.out.println(messaggio2);
					System.exit(0);
				} else if (this.getCurrentState().getTurn().equals(Turn.DRAW)) {
					System.out.println("DRAW!");
					System.exit(0);
				}
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(1);
			}
		}
	}

}
