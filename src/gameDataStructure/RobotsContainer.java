package gameDataStructure;

import java.util.Collection;
import java.util.TreeMap;

import org.json.JSONArray;
import org.json.JSONObject;

import Server.game_service;

public class RobotsContainer {
	private final game_service gameServer;
	private final ServerInfo serverInfo;

	private TreeMap<Integer, Robot> robots;

	public RobotsContainer(game_service gameServer, ServerInfo serverInfo) {
		this.gameServer = gameServer;
		this.serverInfo = serverInfo;
		updateRobots();
	}

	public void updateRobots() {
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
		//System.out.println("finish updateRobots");
	}

	public Robot getRobotByID(int id) {
		return robots.get(id);
	}
	
	public Collection<Robot> getRobots() {
		return robots.values();
	}

}
