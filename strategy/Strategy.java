package strategy;

import hlt.GameMap;
import hlt.Move;

import java.util.ArrayList;

/**
 * Interfata care descrie utilizata pentru fiecare strategie
 * La fiecare tura se genereaza o lista de mutari
 */
public interface Strategy {

	public abstract void runGame(ArrayList<Move> moves, GameMap gameMap);

	public abstract void updateTurn(int turn);
}
