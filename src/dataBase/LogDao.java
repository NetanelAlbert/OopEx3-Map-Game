package dataBase;

import java.util.Date;

public class LogDao {
	private int userId;
	private int levelId;
	private int moves;
	private Date time;
	private int score;
	
	public LogDao(int userId, int levelId, int moves, Date time, int score) {
		this.userId = userId;
		this.levelId = levelId;
		this.moves = moves;
		this.time = time;
		this.score = score;
	}

	public int getUserId() {
		return userId;
	}
	public int getLevelId() {
		return levelId;
	}
	public int getMoves() {
		return moves;
	}
	public Date getTime() {
		return time;
	}
	public int getScore() {
		return score;
	}

	@Override
	public String toString() {
		return "level: " + levelId + ",\t moves: " + moves + ",\t time: " + time + ",\t score: " + score;
	}
	
	
	
}
