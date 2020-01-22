package dataBase;

import java.util.TreeMap;

public class MyGamesData {
	private int playedGames;
	private int currentLevel;
	private TreeMap<Integer, Log> topScors = new TreeMap<Integer, Log>();
	
	public void increedGames() {
		playedGames++;
	}
	
	public void CheckSetCurrentLevel(int currentLevel) {
		if(currentLevel > this.currentLevel)
			this.currentLevel = currentLevel;
	}
	
	public void insertIfBigger(Log log) {
		Log prev = topScors.get(log.getLevelId());
		
		if(prev == null || (isMovesLegal(log) && log.getScore() > prev.getScore()) )
			topScors.put(log.getLevelId(), log);
		
			
	}
	
	private boolean isMovesLegal(Log log) {
		return log.getMoves() <= DBHelper.maxMovesAllaowd(log.getLevelId());
	}
	
	public int getCurrentLevel() {
		return currentLevel;
	}
	public void setCurrentLevel(int currentLevel) {
		this.currentLevel = currentLevel;
	}
	public int getPlayedGames() {
		return playedGames;
	}
	public TreeMap<Integer, Log> getTopScors() {
		return topScors;
	}

}
