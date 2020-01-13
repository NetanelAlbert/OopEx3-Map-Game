package gameClient;

import org.json.JSONException;
import org.json.JSONObject;

public class ServerInfo {
	private int fruits;
	private int moves;
	private int grade;
	private int robots;
	private String graph;
	
	public ServerInfo(JSONObject gameServer) throws JSONException {
		updateServer(gameServer);
	}
	
	public void updateServer(JSONObject gameServer) throws JSONException {
		setFruits(gameServer.getInt("fruits"));
		setMoves(gameServer.getInt("moves"));
		setGrade(gameServer.getInt("grade"));
		setRobots(gameServer.getInt("robots"));
		setGraph(gameServer.getString("graph"));
	}
	
	public int getFruits() {
		return fruits;
	}
	private void setFruits(int fruits) {
		this.fruits = fruits;
	}
	public int getMoves() {
		return moves;
	}
	private void setMoves(int moves) {
		this.moves = moves;
	}
	public int getGrade() {
		return grade;
	}
	private void setGrade(int grade) {
		this.grade = grade;
	}
	public int getRobots() {
		return robots;
	}
	private void setRobots(int robots) {
		this.robots = robots;
	}
	public String getGraph() {
		return graph;
	}
	private void setGraph(String grapg) {
		this.graph = grapg;
	}
}
