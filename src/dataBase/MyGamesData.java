package dataBase;

import java.util.TreeMap;

/**
 * 
 * Holding data of 1 user, mainly reeding from Database
 * 
 * @author Netanel Albert
 *
 */
public class MyGamesData {
	private int playedGames = 0;
	private int currentLevel = 0;
	private TreeMap<Integer, LogDao> topScors = new TreeMap<Integer, LogDao>();
	
	/**
	 * Add 1 to playedGames, to count the games.
	 */
	public void increedGames() {
		playedGames++;
	}
	
	/**
	 * Replace currentLevel if the new is higher.
	 */
	public void CheckSetCurrentLevel(int currentLevel) {
		if(currentLevel > this.currentLevel)
			this.currentLevel = currentLevel;
	}
	
	/**
	 * put log in topScors if is better the the current log in same level
	 */
	public void insertIfBigger(LogDao log) {
		LogDao prev = topScors.get(log.getLevelId());
		
		if(prev == null || (isMovesLegal(log) && log.getScore() > prev.getScore()) )
			topScors.put(log.getLevelId(), log);
		
	}
	
	/**
	 * 
	 * @param log - the game info
	 * @return true iff there isn't more moves then allowed
	 */
	private boolean isMovesLegal(LogDao log) {
		return log.getMoves() <= DBHelper.maxMovesAllaowd(log.getLevelId());
	}
	
	public int getCurrentLevel() {
		return currentLevel;
	}
	public int getPlayedGames() {
		return playedGames;
	}
	public TreeMap<Integer, LogDao> getTopScors() {
		return topScors;
	}

}
