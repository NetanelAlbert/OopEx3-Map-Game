package gameDataStructure;

import org.json.JSONException;
import org.json.JSONObject;
import Server.game_service;

/**
 * This Class holding information about the game server.
 * 
 * @author Netanel Albert
 */
public class ServerInfo {
	private int fruits;
	private int moves;
	private int grade;
	private int robots;
	private String graph;
	private game_service gameServer;
	
	/**
	 * Set the field and update the information.
	 * @throws JSONException - if the server JSON format isn't correct.
	 */
	public ServerInfo(game_service gameServer) throws JSONException {
		this.gameServer = gameServer;
		updateServer();
	}
	/**
	 * Update the information about the server from the server.toString()
	 */
	public void updateServer() throws JSONException {
		JSONObject serverJSON = new JSONObject(gameServer.toString()).getJSONObject("GameServer");
		setFruits(serverJSON.getInt("fruits"));
		setMoves(serverJSON.getInt("moves"));
		setGrade(serverJSON.getInt("grade"));
		setRobots(serverJSON.getInt("robots"));
		setGraph(serverJSON.getString("graph"));
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
