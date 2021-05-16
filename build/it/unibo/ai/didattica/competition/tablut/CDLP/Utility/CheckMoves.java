package it.unibo.ai.didattica.competition.tablut.CDLP.Utility;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import it.unibo.ai.didattica.competition.tablut.domain.Action;
import it.unibo.ai.didattica.competition.tablut.domain.State;
import it.unibo.ai.didattica.competition.tablut.domain.State.Turn;

/**
*
* @author L. Corsaro, D. De Nardi, S. Lia, M. Pranzini
*
*/

public class CheckMoves {

	public final static List<String> citadels = new ArrayList<String>(Arrays.asList("a4", "a5", "a6", "b5", "d1", "e1",
			"f1", "e2", "i4", "i5", "i6", "h5", "d9", "e9", "f9", "e8"));

	private static State movePawn(State state, Action a) {
		State.Pawn pedina = state.getPawn(a.getRowFrom(), a.getColumnFrom());
		State.Pawn[][] scacchiera = state.getBoard();
		// se sto muovendo il re, libero il trono, altrimenti libero la cella normale
		if (Utility.isCastle(a.getRowFrom(), a.getColumnFrom())) {
			scacchiera[a.getRowFrom()][a.getColumnFrom()] = State.Pawn.THRONE;
		} else {
			scacchiera[a.getRowFrom()][a.getColumnFrom()] = State.Pawn.EMPTY;
		}

		// metto la pedina nella nuova posizione e modifico lo stato
		scacchiera[a.getRowTo()][a.getColumnTo()] = pedina;
		state.setBoard(scacchiera);
		// assegno nuovo turno
		if (state.getTurn() == Turn.WHITE) { 
			state.setTurn(State.Turn.BLACK);
		} else {
			state.setTurn(State.Turn.WHITE);
		}

		return state;
	}

	public static State checkMove(State state, Action a) {

		state = CheckMoves.movePawn(state, a);

		if (state.getTurn() == Turn.WHITE) {
			state = CheckMovesBlack.controlloNeroCattura(state, a);
		} else if (state.getTurn() == Turn.BLACK) {
			state = CheckMovesWhite.controlloCatturaBianco(state, a);
		}
		return state;
	}
	

}
