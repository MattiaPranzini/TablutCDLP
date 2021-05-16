package it.unibo.ai.didattica.competition.tablut.CDLP.Utility;

import it.unibo.ai.didattica.competition.tablut.CDLP.Model.Coordinate;
import it.unibo.ai.didattica.competition.tablut.domain.Action;
import it.unibo.ai.didattica.competition.tablut.domain.State;

import java.io.IOException;
import java.util.*;

public class Utility {

	public static byte[][] w_map(State state, List<Coordinate> pedineBianche) {

		State.Pawn[][] board = state.getBoard();
		byte[][] bytes = new byte[9][9];
		// 1 se trovo pedina, campo o castello, altrimenti 0
		int r, c;
		for (r = 0; r < 9; r++) {
			for (c = 0; c < 9; c++) {
				if (board[r][c].equals(State.Pawn.EMPTY) && !isCamp(r, c) && !isCastle(r, c))
					bytes[r][c] = 0;

				else {
					bytes[r][c] = 1;

					// salvo le coordinate delle pedine bianche
					if (board[r][c].equals(State.Pawn.WHITE))
						pedineBianche.add(new Coordinate(r, c));

					// e il re
					else if (board[r][c].equals(State.Pawn.KING))
						pedineBianche.add(0, new Coordinate(r, c));
				}
			}
		}

		return bytes;
	}

	public static byte[][][] b_map(State state, List<Coordinate> pedineNere) {
		// 1 se trovo pedina, campo o castello, altrimenti 0
		State.Pawn[][] board = state.getBoard();
		byte[][][] bytes = new byte[2][9][9];

		int r, c;
		for (r = 0; r < 9; r++) {
			for (c = 0; c < 9; c++) {
				if (board[r][c].equals(State.Pawn.EMPTY) && !isCastle(r, c)) {
					bytes[1][r][c] = 0;

					if (!isCamp(r, c))
						bytes[0][r][c] = 0;
					else
						bytes[0][r][c] = 1;
				} else {
					bytes[1][r][c] = 1;
					bytes[0][r][c] = 1;
					// salvo le coordinate delle pedine nere
					if (board[r][c].equals(State.Pawn.BLACK))
						pedineNere.add(new Coordinate(r, c));
				}
			}
		}

		return bytes;
	}

	public static List<Action> getMossePossibili(State state) {

		List<Coordinate> pedineNere = new ArrayList<>();
		List<Coordinate> pedineBianche = new LinkedList<>();
		List<Action> azioniPossibili = new LinkedList<>();

		byte[][] table = null;
		byte[][] table_camp = null;

		if (state.getTurn().equals(State.Turn.WHITE)) {
			table = w_map(state, pedineBianche);

			for (Coordinate c : pedineBianche) {
				try {
					azioniPossibili.addAll(getMosseFromCoordinata(c, table, state.getTurn()));
				} catch (Exception e) {
					System.out.println(e.getStackTrace());
				}
			}
		}

		else {

			byte[][][] bytes = b_map(state, pedineNere);
			table = bytes[0];
			table_camp = bytes[1];

			for (Coordinate c : pedineNere) {
				try {
					if (isCamp(c))
						azioniPossibili.addAll(getMosseFromCoordinata(c, table_camp, state.getTurn()));
					else
						azioniPossibili.addAll(getMosseFromCoordinata(c, table, state.getTurn()));
				} catch (Exception e) {
					System.out.println(e.getStackTrace());
				}
			}
		}

		return azioniPossibili;
	}

	private static String coordToString(int riga, int colonna) {
		char col = (char) (colonna + 97);
		String ret = col + "" + (riga + 1);
		return ret;
	}
	

	// mosse possibili partendo da una coordinata
	private static List<Action> getMosseFromCoordinata(Coordinate c, byte[][] table, State.Turn turn) throws IOException {

		List<Action> res = new LinkedList<>();
		String from = coordToString(c.getRow(), c.getCol());
		int pivot = c.getCol();

		// scorro sinistra
		for (int k = pivot - 1; k >= 0; k--) {
			if (table[c.getRow()][k] == 0) {
				String to = coordToString(c.getRow(), k);
				res.add(new Action(from, to, turn));
			} else {
				break;
			}
		}

		// scorro a destra
		for (int i = pivot + 1; i < 9; i++) {
			if (table[c.getRow()][i] == 0) {
				String to = coordToString(c.getRow(), i);
				res.add(new Action(from, to, turn));
			} else {
				break;
			}
		}

		pivot = c.getRow();

		// scorro in giù,
		for (int i = pivot + 1; i < 9; i++) {
			if (table[i][c.getCol()] == 0) {
				String dest = coordToString(i, c.getCol());
				res.add(new Action(from, dest, turn));
			} else {
				break;
			}
		}

		// scorro in su
		for (int k = pivot - 1; k >= 0; k--) {
			if (table[k][c.getCol()] == 0) {
				String to = coordToString(k, c.getCol());
				res.add(new Action(from, to, turn));
			} else {
				break;
			}
		}

		return res;
	}

	public static boolean isCastle(int riga, int colonna) {
		return (riga == 4 && colonna == 4);
	}

	private static boolean isCamp(Coordinate c) {
		return isCamp(c.getRow(), c.getCol());
	}

	private static boolean isCamp(int riga, int colonna) {
		return (((riga == 0 || riga == 8) && (colonna >= 3 && colonna <= 5))
				|| ((colonna == 0 || colonna == 8) && (riga >= 3 && riga <= 5))
				|| ((riga == 1 || riga == 7) && (colonna == 4)) || ((colonna == 1 || colonna == 7) && (riga == 4)));
	}
	

	public static int[] getPosizioneRe(State state) {
       int[] king= new int[2];
       //recupero la scacchiera e procedo
       State.Pawn[][] scacch = state.getBoard();
       for (int i = 0; i < scacch.length; i++) {
           for (int j = 0; j < scacch.length; j++) {
               if (state.getPawn(i, j).equalsPawn("K")) {
                   king[0] = i;
                   king[1] = j;
               }
           }
       }
       return king;
   }

   public static boolean checkPosizioneRe(State state){
       if(state.getPawn(4,4).equalsPawn("K"))
           return true;
       else
           return false;
   }

   public static int getNumPedineVicine(State state, int[] position, String obiettivo){
       int c=0;
       State.Pawn[][] scacch = state.getBoard();
       if(position[0]>=1 && scacch[position[0]-1][position[1]].equalsPawn(obiettivo))
           c++;
       if(position[0]<8 && scacch[position[0]+1][position[1]].equalsPawn(obiettivo))
           c++;
       if(position[1]>=1 && scacch[position[0]][position[1]-1].equalsPawn(obiettivo))
           c++;
       if(position[1]<8 && scacch[position[0]][position[1]+1].equalsPawn(obiettivo))
           c++;
       return c;
   }


   public static List<int[]> posizioniOccupate(State state, int[] position, String obiettivo){
       List<int[]> posizioniOccupate = new ArrayList<int[]>();
       int[] pos = new int[2];
       State.Pawn[][] scacch = state.getBoard();
       if(scacch[position[0]-1][position[1]].equalsPawn(obiettivo)) {
           pos[0] = position[0] - 1;
           pos[1] = position[1];
           posizioniOccupate.add(pos);
       }
       if(scacch[position[0]+1][position[1]].equalsPawn(obiettivo)) {
           pos[0] = position[0] + 1;
           posizioniOccupate.add(pos);
       }
       if(scacch[position[0]][position[1]-1].equalsPawn(obiettivo)) {
           pos[0] = position[0];
           pos[1] = position[1] - 1;
           posizioniOccupate.add(pos);
       }
       if(scacch[position[0]][position[1]+1].equalsPawn(obiettivo)) {
           pos[0] = position[0];
           pos[1] = position[1] + 1;
           posizioniOccupate.add(pos);
       }

       return posizioniOccupate;
   }


   public static boolean reVicino(State state, int[] position){
       return getNumPedineVicine(state, position, "K") > 0;
   }

   public static int getNumVieBloccate(State state){
       int c = 0;
       int[][] vieBloccate = {{1,1},{1,2},{1,6},{1,7},{2,1},{2,7},{6,1},{6,7},{7,1},{7,2},{7,6},{7,7}};
       for (int[] pos: vieBloccate){
           if (state.getPawn(pos[0],pos[1]).equalsPawn(State.Pawn.BLACK.toString())){
               c++;
           }
       }
       return c;
   }

   public static boolean biancoVince(State state){
       int[] posRe = getPosizioneRe(state);
       boolean ris;
       ris = posRe[0] == 0 || posRe[0] == 8 || posRe[1] == 0 || posRe[1] == 8;
       return ris;
   }


   //quadrato dal quale non si può raggiungere l'uscita in un'unica mossa
   public static boolean quadratoAttornoAlTrono(State state,int[] posizioneRe){
       if(posizioneRe[0] > 2 && posizioneRe[0] < 6) {
           if (posizioneRe[1] > 2 && posizioneRe[1] < 6) {
               return true;
           }
       }
       return false;
   }

   public static boolean rePuoVincere(State state){
       int[] posizioneRe=Utility.getPosizioneRe(state);
       int col = 0;
       int row = 0;
       if(!quadratoAttornoAlTrono(state,posizioneRe)){
    	   
           if((!(posizioneRe[1] > 2 && posizioneRe[1] < 6)) && (!(posizioneRe[0] > 2 && posizioneRe[0] < 6))){
               col = getTotVieLibereSuColonna(state, posizioneRe);
               row = getTotVieLibereSuRiga(state,posizioneRe);
           }
           if((posizioneRe[1] > 2 && posizioneRe[1] < 6)){
               row = getTotVieLibereSuRiga(state, posizioneRe);
           }
           if((posizioneRe[0] > 2 && posizioneRe[0] < 6)) {
               col = getTotVieLibereSuColonna(state, posizioneRe);
           }
           return (col + row > 0);
       }
       return (col + row > 0);
   }

   public static int numUsciteRaggiungibili(State state){
       int[] posizioneRe=Utility.getPosizioneRe(state);
       int col = 0;
       int row = 0;
       if(!quadratoAttornoAlTrono(state,posizioneRe)){
           if((!(posizioneRe[1] > 2 && posizioneRe[1] < 6)) && (!(posizioneRe[0] > 2 && posizioneRe[0] < 6))){
               col = getTotVieLibereSuColonna(state, posizioneRe);
               row = getTotVieLibereSuRiga(state,posizioneRe);
           }
           if((posizioneRe[1] > 2 && posizioneRe[1] < 6)){
               row = getTotVieLibereSuRiga(state, posizioneRe);
           }
           if((posizioneRe[0] > 2 && posizioneRe[0] < 6)) {
               col = getTotVieLibereSuColonna(state, posizioneRe);
           }
           return (col + row);
       }

       return (col + row);

   }

   // restituisce 1 se ha un solo lato libero (dx e sx), 2 entrambi, 0 nessuno 
   public static int getTotVieLibereSuRiga(State state,int[] position){
       int row=position[0];
       int col=position[1];
       int[] posizioneCorrente = new int[2];
       int vieLibere=0;
       int destra=0;
       int sinistra=0;
       //scorro a dx
       for(int i = col+1; i<=8; i++) {
           posizioneCorrente[0]=row;
           posizioneCorrente[1]=i;
           if (cellaOccupata(state,posizioneCorrente)) {
               destra++;
           }
       }
       if(destra==0)
           vieLibere++;
     //scorro a sx
       for(int i=col-1;i>=0;i--) {
           posizioneCorrente[0]=row;
           posizioneCorrente[1]=i;
           if (cellaOccupata(state,posizioneCorrente)){
               sinistra++;
           }
       }
       if(sinistra==0)
           vieLibere++;

       return vieLibere;
   }

   // restituisce 1 se ha un solo lato libero (sopra e sotto), 2 entrambi, 0 nessuno 
   public static int getTotVieLibereSuColonna(State state,int[] position){
       int row=position[0];
       int column=position[1];
       int[] posizioneCorrente = new int[2];
       int vieLibere=0;
       int su=0;
       int giu=0;
       //scorro in giù
       for(int i=row+1;i<=8;i++) {
           posizioneCorrente[0]=i;
           posizioneCorrente[1]=column;
           if (cellaOccupata(state,posizioneCorrente)) {
               giu++;
           }
       }
       if(giu==0)
           vieLibere++;
       //scorro in su
       for(int i=row-1;i>=0;i--) {
           posizioneCorrente[0]=i;
           posizioneCorrente[1]=column;
           if (cellaOccupata(state,posizioneCorrente)){
               su++;
           }
       }
       if(su==0)
           vieLibere++;

       return vieLibere;
   }

   
   public static boolean cellaOccupata(State state,int[] position){
       return !state.getPawn(position[0], position[1]).equals(State.Pawn.EMPTY);
   }

   public static int getNumPedinePerMangiareRe(State state){

       int[] posizioneRe = getPosizioneRe(state);

       if (posizioneRe[0] == 4 && posizioneRe[1] == 4){
           return 4;
       } else if ((posizioneRe[0] == 3 && posizioneRe[1] == 4) || (posizioneRe[0] == 4 && posizioneRe[1] == 3)
                  || (posizioneRe[0] == 5 && posizioneRe[1] == 4) || (posizioneRe[0] == 4 && posizioneRe[1] == 5)){
           return 3;
       } else{
           return 2;
       }

   }
}
