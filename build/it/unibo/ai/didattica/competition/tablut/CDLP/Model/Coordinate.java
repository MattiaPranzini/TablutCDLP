package it.unibo.ai.didattica.competition.tablut.CDLP.Model;

import java.util.Objects;

/**
 *
 * @author L. Corsaro, D. De Nardi, S. Lia, M. Pranzini
 *
 */

public class Coordinate {

	private int row;
	private int col;

	public Coordinate(int row, int col) {
		this.row = row;
		this.col = col;
	}

	public int getRow() {
		return row;
	}

	public void setRow(int row) {
		this.row = row;
	}

	public int getCol() {
		return col;
	}

	public void setCol(int col) {
		this.col = col;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		Coordinate coord = (Coordinate) o;
		return getRow() == coord.getRow() && getCol() == coord.getCol();
	}

	@Override
	public int hashCode() {
		return Objects.hash(getRow(), getCol());
	}

	public int distanceFrom(Coordinate other) {
		return Math.abs(this.getRow() - other.getRow()) + Math.abs(this.getCol() - other.getCol());
	}

	public boolean closeTo(Coordinate other) {
		if (other.getCol() < 0 || other.getRow() < 0)
			return false;

		int res = this.getCol() - other.getCol() + this.getRow() - other.getRow();
		return (res == 1 || res == -1);
	}

	@Override
	public String toString() {
		return "Coordinate (" + "riga:" + row + ", colonna:" + col + ')';
	}
}
