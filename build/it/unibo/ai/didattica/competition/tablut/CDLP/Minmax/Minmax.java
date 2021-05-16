package it.unibo.ai.didattica.competition.tablut.CDLP.Minmax;

import it.unibo.ai.didattica.competition.tablut.CDLP.Heuristic.Heuristic;
import it.unibo.ai.didattica.competition.tablut.CDLP.Heuristic.HeuristicCDLP;
import it.unibo.ai.didattica.competition.tablut.CDLP.Utility.CheckMoves;
import it.unibo.ai.didattica.competition.tablut.CDLP.Utility.Utility;
import it.unibo.ai.didattica.competition.tablut.domain.Action;
import it.unibo.ai.didattica.competition.tablut.domain.State;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;

/**
 *
 * @author L. Corsaro, D. De Nardi, S. Lia, M. Pranzini
 *
 */

public final class Minmax implements Callable<Action> {

	private final ExecutorService executorService = Executors.newCachedThreadPool();
	
	private final State.Turn giocatore;
	private final Heuristic heuristic;
	protected int depthLimit;

	private static final Random rand = new Random();

	private static Action result;
	private static List<Action> azioniPossibili;

	private State currentState;

	public Minmax(int currDepthLimit, State.Turn player, State state) {
		this.depthLimit = currDepthLimit;
		this.giocatore = player;
		this.heuristic = new HeuristicCDLP(player, state);

		azioniPossibili = new ArrayList<>();
	}

	public Action makeDecision(int timeOut, State stato) throws IOException {
		currentState = stato;

		// eseguiamo l'esecutore
		Future<Action> risultato = executorService.submit(this);

		result = null;
		azioniPossibili.clear();

		try {
			result = risultato.get(timeOut, TimeUnit.SECONDS);
			System.out.println("Selezionato: [" + result.toString() + "]");
		} catch (TimeoutException e) {

			risultato.cancel(true);
			System.out.println("****** TIME OUT ********");

			if (!azioniPossibili.isEmpty())
				result = azioniPossibili.get(rand.nextInt(azioniPossibili.size()));

			return result;

		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	@Override
	public Action call() throws Exception {

		// inizializzo con un valore molto piccolo
		double resultValue = Double.NEGATIVE_INFINITY;

		// recupero le mosse iniziali
		List<Action> azioni = Utility.getMossePossibili(currentState);
		Collections.shuffle(azioni);

		// assegno a result una mossa random
		result = azioni.get(0);
		azioniPossibili.add(azioni.get(0));

		for (Action action : azioni) {

			double value = minValue(CheckMoves.checkMove(currentState.clone(), action), Double.NEGATIVE_INFINITY,
					Double.POSITIVE_INFINITY, 0);

			if (Thread.interrupted()) {
				System.out.println(Thread.currentThread() + "**** Tempo esaurito");
				gestisciTerminazione();
				System.out.println(Thread.currentThread() + "**** Thread interrotto");
				return azioniPossibili.get(rand.nextInt(azioniPossibili.size()));
			}

			if (value > resultValue) {
				result = action;
				azioniPossibili.clear();
				azioniPossibili.add(action);
				resultValue = value;
			}

			// in caso di valori uguali ne scelgo uno a caso
			else if (value == resultValue) {
				azioniPossibili.add(action);
				resultValue = value;
			}

		} // for

		// restituisco una delle migliori azioni possibili
		return azioniPossibili.get(rand.nextInt(azioniPossibili.size()));
	}

	public double maxValue(State state, double alpha, double beta, int depth) throws Exception {

		if (Thread.interrupted()) {
			System.out.println(Thread.currentThread() + "**** Tempo esaurito funzione MAX");
			gestisciTerminazione();
			System.out.println(Thread.currentThread() + "**** Thread interrotto funzione MAX");

			return 0;
		}

		if (state.getTurn() == State.Turn.BLACKWIN || state.getTurn() == State.Turn.WHITEWIN || depth >= depthLimit)
			return evaluate(state, giocatore, depth);

		double value = Double.NEGATIVE_INFINITY;

		for (Action action : Utility.getMossePossibili(state)) {
			value = Math.max(value, minValue(CheckMoves.checkMove(state.clone(), action), alpha, beta, depth + 1));
			if (value >= beta)
				return value;
			alpha = Math.max(alpha, value);
		}
		return value;
	}

	private void gestisciTerminazione() {
		Thread.currentThread().stop();
	}

	public double minValue(State state, double alpha, double beta, int depth) throws Exception {

		if (Thread.interrupted()) {
			System.out.println(Thread.currentThread() + "**** Tempo esaurito funzione MIN");
			gestisciTerminazione();
			System.out.println(Thread.currentThread() + "**** Thread interrotto funzione MIN");
			return 0;
		}

		if (state.getTurn() == State.Turn.BLACKWIN || state.getTurn() == State.Turn.WHITEWIN || depth >= depthLimit)
			return evaluate(state, giocatore, depth);

		double value = Double.POSITIVE_INFINITY;

		for (Action action : Utility.getMossePossibili(state)) {
			value = Math.min(value, maxValue(CheckMoves.checkMove(state.clone(), action), alpha, beta, depth + 1));
			if (value <= alpha)
				return value;
			beta = Math.min(beta, value);
		}
		return value;
	}

	private double evaluate(State state, State.Turn player, int depth) {
		return heuristic.eval(state, player);
	}

}