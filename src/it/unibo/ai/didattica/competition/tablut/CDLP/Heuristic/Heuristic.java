package it.unibo.ai.didattica.competition.tablut.CDLP.Heuristic;

import it.unibo.ai.didattica.competition.tablut.domain.State;

/**
 *
 * @author L. Corsaro, D. De Nardi, S. Lia, M. Pranzini
 *
 */

public interface Heuristic {
    double eval(State state, State.Turn player);
}
