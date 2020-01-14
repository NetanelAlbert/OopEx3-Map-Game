package gameDataStructure;

import org.json.JSONArray;
import org.json.JSONObject;

import Server.game_service;
import algorithms.Graph_Algo;
import grapgDataStructure.node_data;
import utils.Point3D;

public class RobotsUpdatingContainer extends Thread {
	private final game_service gameServer;
	private final ServerInfo serverInfo;
	private final Graph_Algo algo;
	private final double EPS;

	private Robot[] robots;
	private int stuckRobotID = -1;
	private node_data stackRobotVertex;

	public RobotsUpdatingContainer(game_service gameServer, ServerInfo serverInfo, Graph_Algo algo, double EPS) {
		this.gameServer = gameServer;
		this.serverInfo = serverInfo;
		this.algo = algo;
		this.EPS = EPS;
		updateRobots();
	}

	@Override
	public void run() {
		while (gameServer.isRunning()) {
			updateRobots();
			findStackRobot();

			try {
				Thread.sleep(40);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void findStackRobot() {
		if (robots == null || stuckRobotID != -1)
			return;

		for (Robot robot : robots) {
			if (robot.getDest() == -1) {
				stuckRobotID = robot.getId();
				stackRobotVertex = getNodeByLocation(robot.getPos());
				return;
			}
		}
	}

	public synchronized void updateRobots() {
		if (gameServer == null || serverInfo == null) {
			return;
		}
		if (robots == null)
			robots = new Robot[serverInfo.getRobots()];
		try {
			JSONArray robotsJSON = new JSONArray(gameServer.getRobots());
			for (int i = 0; i < robotsJSON.length(); i++) {
				String roboString = robotsJSON.getString(i);
				JSONObject roboJSON = new JSONObject(roboString).getJSONObject("Robot");
				if(robots[i] == null) 
					robots[i] = new Robot(roboJSON);
				else 
					robots[i].updateRobot(roboJSON);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private synchronized node_data getNodeByLocation(Point3D point) {
		for (node_data n : algo.getGraph().getV()) {
			Point3D p = n.getLocation();
			if (Math.abs(p.x() - point.x()) < EPS && Math.abs(p.y() - point.y()) < EPS)
				return n;
		}
		return null;
	}

	public int getStuckRobotID() {
		return stuckRobotID;
	}

	public node_data getStackRobotVertex() {
		return stackRobotVertex;
	}

	public void resetStackRobot() {
		this.stuckRobotID = -1;
		this.stackRobotVertex = null;
	}

	public synchronized Robot getRobot(int i) {
		if (robots == null || i >= robots.length)
			return null;

		return robots[i];
	}

	public synchronized Robot getRobotByID(int id) {
		for (Robot robot : robots) {
			if(robot.getId() == id)
				return robot;
		}
		return null;
	}
	
	public synchronized Robot[] getRobots() {
		return robots;
	}

}
