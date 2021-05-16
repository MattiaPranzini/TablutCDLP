package it.unibo.ai.didattica.competition.tablut.CDLP.Heuristic;

import it.unibo.ai.didattica.competition.tablut.CDLP.Utility.Utility;
import it.unibo.ai.didattica.competition.tablut.domain.State;
import it.unibo.ai.didattica.competition.tablut.domain.State.Turn;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author L. Corsaro, D. De Nardi, S. Lia, M. Pranzini
 *
 */

public class HeuristicCDLP implements Heuristic {

	private State state;
	
	// numero di pedine e caselle
	public final static int NUM_BLACK = 16;
	public final static int NUM_WHITE = 8;
	public final static int NUM_ESCAPES = 16;
	public final static int NUM_CITADELS = 16;

	// BIANCHI
	private final String NERI_MANGIATI = "numeroDiNeriMangiati";
	private final String PROTEZIONE_RE = "protezioneDelRe";
	private final String BIANCHI_VIVI = "numeroBianchiVivi";
	private final String NUM_CELLE_USCITA = "numeroCelleUscita";

	// NERI
	private final String ROMBI = "posizioniRombo";
	private final String BIANCHI_MANGIATI = "numeroBianchiMangiati";
	private final String NERI_VIVI = "numeroNeriVivi";
	private final String NERI_ATTORNO_RE = "neriAttornoAlRe";

	// Threshold configurazione a rombi
	private final int THRESHOLD = 10;
	// Numero di caselle per i rombi
	private final int NUM_CASELLE_ROMBI = 8;

	private final Map<String, Double> pesi;
	private String[] chiavi;

	// Posizioni preferite delle pedine nere per bloccare le uscite
	private final int[][] rhombus = { { 1, 2 }, { 1, 6 }, { 2, 1 }, { 2, 7 }, { 6, 1 }, { 6, 7 }, { 7, 2 }, { 7, 6 } };

	private double numeroNeri;
	private double numeroBianchiMangiati;

	public HeuristicCDLP(State.Turn coloreGiocatore, State state) {
		this.state = state;

		if (coloreGiocatore == Turn.BLACK) {
			pesi = new HashMap<String, Double>();
			pesi.put(NERI_VIVI, 35.0);
			pesi.put(BIANCHI_MANGIATI, 30.0);
			pesi.put(NERI_ATTORNO_RE, 33.0);
			pesi.put(ROMBI, 2.0);

			chiavi = new String[pesi.size()];
			chiavi = pesi.keySet().toArray(new String[0]);
		} else {
			// associazione pesi
			pesi = new HashMap<String, Double>();
			// mosse migliori iniziali
			pesi.put(NERI_MANGIATI, 15.0);
			pesi.put(BIANCHI_VIVI, 34.0);
			pesi.put(NUM_CELLE_USCITA, 25.0);
			pesi.put(NERI_ATTORNO_RE, 7.0);
			pesi.put(PROTEZIONE_RE, 19.0);

			chiavi = new String[pesi.size()];
			chiavi = pesi.keySet().toArray(new String[0]);
		}
	}

	public double eval(State state, State.Turn giocatore) {

		double valore = 0.0;

		if (giocatore == Turn.BLACK) {
			numeroNeri = (double) state.getNumberOf(State.Pawn.BLACK) / NUM_BLACK;
			numeroBianchiMangiati = (double) (NUM_WHITE - state.getNumberOf(State.Pawn.WHITE)) / NUM_WHITE;
			double pedineVicinoRe = (double) Utility.getNumPedineVicine(state, Utility.getPosizioneRe(state), State.Turn.BLACK.toString())
					/ Utility.getNumPedinePerMangiareRe(state);
			double numeroPedineRombo = (double) getNumNeriRombo() / NUM_CASELLE_ROMBI;

			// Weighted sum of functions to get final utility value
			Map<String, Double> atomicUtilities = new HashMap<String, Double>();
			atomicUtilities.put(NERI_VIVI, numeroNeri);
			atomicUtilities.put(BIANCHI_MANGIATI, numeroBianchiMangiati);
			atomicUtilities.put(NERI_ATTORNO_RE, pedineVicinoRe);
			atomicUtilities.put(ROMBI, numeroPedineRombo);

			for (int i = 0; i < pesi.size(); i++) {
				valore += pesi.get(chiavi[i]) * atomicUtilities.get(chiavi[i]);
			}
		}

		// bianchi
		else {
			double numeroBianchiVivi = (double) (state.getNumberOf(State.Pawn.WHITE)) / NUM_WHITE;
			double numeroNeriMangiati = (double) (NUM_BLACK - state.getNumberOf(State.Pawn.BLACK)) / NUM_BLACK;
			double neriAttornoRe = (double) (Utility.getNumPedinePerMangiareRe(state)
					- Utility.getNumPedineVicine(state, Utility.getPosizioneRe(state), State.Turn.BLACK.toString()))
					/ Utility.getNumPedinePerMangiareRe(state);
			double protezioneRe = getValProtezioneRe();

			int numeroModiVincere = Utility.numUsciteRaggiungibili(state);
			double numeroVieFugaRe = numeroModiVincere > 1 ? (double) Utility.numUsciteRaggiungibili(state) / 4 : 0.0;

			Map<String, Double> val = new HashMap<String, Double>();
			val.put(BIANCHI_VIVI, numeroBianchiVivi);
			val.put(NERI_MANGIATI, numeroNeriMangiati);
			val.put(NUM_CELLE_USCITA, numeroVieFugaRe);
			val.put(NERI_ATTORNO_RE, neriAttornoRe);
			val.put(PROTEZIONE_RE, protezioneRe);

			for (int i = 0; i < pesi.size(); i++) {
				valore += pesi.get(chiavi[i]) * val.get(chiavi[i]);
			}

		}

		return valore;
	}

	private int getNumNeriRombo() {

		if (state.getNumberOf(State.Pawn.BLACK) >= THRESHOLD) {
			int c = 0;
			for (int[] position : rhombus) {
				if (state.getPawn(position[0], position[1]).equalsPawn(State.Pawn.BLACK.toString())) {
					c++;
				}
			}
			return c;
		} else {
			return 0;
		}
	}
	
	private double getValProtezioneRe() {

		// se c'è un bianco vicino al re
		double VAL_NEAR = 0.6;
		double VAL_TOT = 1.0;

		double result = 0.0;

		int[] kingPos = Utility.getPosizioneRe(state);
		// pedine vicino al re
		ArrayList<int[]> posizioniPedine = (ArrayList<int[]>) Utility.posizioniOccupate(state, kingPos,
				State.Pawn.BLACK.toString());

		// se c'è un nero che minaccia il re e sono necessarie due pedine per mangiarlo
		if (posizioniPedine.size() == 1 && Utility.getNumPedinePerMangiareRe(state) == 2) {
			int[] posNeri = posizioniPedine.get(0);
			// posizioni in cui il re può essere mangiato
			int[] posizioniObiettivo = new int[2];
			// nero a dx del Re
			if (posNeri[0] == kingPos[0] && posNeri[1] == kingPos[1] + 1) {
				// se il re ha un bianco a sx
				posizioniObiettivo[0] = kingPos[0];
				posizioniObiettivo[1] = kingPos[1] - 1;
				if (state.getPawn(posizioniObiettivo[0], posizioniObiettivo[1])
						.equalsPawn(State.Pawn.WHITE.toString())) {
					result += VAL_NEAR;
				}
				// nero a sx del Re
			} else if (posNeri[0] == kingPos[0] && posNeri[1] == kingPos[1] - 1) {
				// se il re ha un bianco a dx
				posizioniObiettivo[0] = kingPos[0];
				posizioniObiettivo[1] = kingPos[1] + 1;
				if (state.getPawn(posizioniObiettivo[0], posizioniObiettivo[1])
						.equalsPawn(State.Pawn.WHITE.toString())) {
					result += VAL_NEAR;
				}
				// nero sopra al Re
			} else if (posNeri[1] == kingPos[1] && posNeri[0] == kingPos[0] - 1) {
				// se il re ha un bianco sotto
				posizioniObiettivo[0] = kingPos[0] + 1;
				posizioniObiettivo[1] = kingPos[1];
				if (state.getPawn(posizioniObiettivo[0], posizioniObiettivo[1])
						.equalsPawn(State.Pawn.WHITE.toString())) {
					result += VAL_NEAR;
				}
				// nero sotto al Re
			} else {
				// se il re ha un bianco a sopra
				posizioniObiettivo[0] = kingPos[0] - 1;
				posizioniObiettivo[1] = kingPos[1];
				if (state.getPawn(posizioniObiettivo[0], posizioniObiettivo[1])
						.equalsPawn(State.Pawn.WHITE.toString())) {
					result += VAL_NEAR;
				}
			}

			// Fare barriera con i bianchi sulle posizioniObiettivo
			double points = VAL_TOT - VAL_NEAR;
			double valore = 0.0;

			if (posizioniObiettivo[0] == 0 || posizioniObiettivo[0] == 8 || posizioniObiettivo[1] == 0
					|| posizioniObiettivo[1] == 8) {
				if (state.getPawn(posizioniObiettivo[0], posizioniObiettivo[1])
						.equalsPawn(State.Pawn.EMPTY.toString())) {
					result = 1.0;
				} else {
					result = 0.0;
				}
			} else {
				// ridurre il numero di pedine vicine se le posizioniObiettivo sono affiancate
				// al trono / accampamenti
				if (posizioniObiettivo[0] == 4 && posizioniObiettivo[1] == 2
						|| posizioniObiettivo[0] == 4 && posizioniObiettivo[1] == 6
						|| posizioniObiettivo[0] == 2 && posizioniObiettivo[1] == 4
						|| posizioniObiettivo[0] == 6 && posizioniObiettivo[1] == 4
						|| posizioniObiettivo[0] == 3 && posizioniObiettivo[1] == 4
						|| posizioniObiettivo[0] == 5 && posizioniObiettivo[1] == 4
						|| posizioniObiettivo[0] == 4 && posizioniObiettivo[1] == 3
						|| posizioniObiettivo[0] == 4 && posizioniObiettivo[1] == 5) {
					valore = points / 2;
				} else {
					valore = points / 3;
				}

				result += valore * Utility.getNumPedineVicine(state, posizioniObiettivo, State.Pawn.WHITE.toString());
			}

		}
		return result;
	}

}
