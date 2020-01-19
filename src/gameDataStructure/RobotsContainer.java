package gameDataStructure;

import java.util.Collection;
import java.util.TreeMap;

import org.json.JSONArray;
import org.json.JSONObject;

import Server.game_service;

/**
 * This Class is holding a collection of Robots, 
 * and should be update all the time by updateRobots(),
 * that take a new information from server
 * 
 * @author Netanel Albert
 */
public class RobotsContainer {
	private final game_service gameServer;
	private final ServerInfo serverInfo;

	private TreeMap<Integer, Robot> robots;

	/**
	 * 
	 * @param gameServer, @param serverInfo - needed for update the information
	 */
	public RobotsContainer(game_service gameServer, ServerInfo serverInfo) {
		this.gameServer = gameServer;
		this.serverInfo = serverInfo;
		updateRobots();
	}

	/**
	 * Update the information about the robots from the server.
	 */
	public synchronized void updateRobots() {
		if (gameServer == null || serverInfo == null) {
			return;
		}
		if (robots == null)
			robots = new TreeMap<Integer, Robot>();
		try {
			JSONArray robotsJSON = new JSONArray(gameServer.getRobots());
			for (int i = 0; i < robotsJSON.length(); i++) {
				String roboString = robotsJSON.getString(i);
				JSONObject roboJSON = new JSONObject(roboString).getJSONObject("Robot");
				int id = roboJSON.getInt("id");
				if(robots.get(id) == null) 
					robots.put(id, new Robot(roboJSON));
				else 
					robots.get(id).updateRobot(roboJSON);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public synchronized Robot getRobotByID(int id) {
		return robots.get(id);
	}
	
	public synchronized Collection<Robot> getRobots() {
		return robots.values();
	}

}
