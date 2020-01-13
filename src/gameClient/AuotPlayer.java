package gameClient;

import java.util.Collection;
import java.util.Iterator;

import Server.game_service;
import algorithms.Graph_Algo;
import myDataStructure.edge_data;
import myDataStructure.node_data;

public class AuotPlayer extends Thread {
	private final game_service gameServer;
	private final ServerInfo serverInfo;
	private final Graph_Algo algo;
	private final FruitUpdatingContainer fruits;
	private final RobotsUpdatingContainer robots;
	
	public AuotPlayer(game_service gameServer, ServerInfo serverInfo, Graph_Algo algo, FruitUpdatingContainer fruits,
			RobotsUpdatingContainer robots) {
		this.gameServer = gameServer;
		this.serverInfo = serverInfo;
		this.algo = algo;
		this.fruits = fruits;
		this.robots = robots;
		
		int nodeSize = algo.getGraph().nodeSize();
		for (int i = 0; i < serverInfo.getRobots(); i++) {
			gameServer.addRobot((int)(Math.random()*nodeSize));
		}
	}
	
	@Override
	public void run() {
		while (gameServer.isRunning()) {
			int robotNum = robots.getStuckRobotID();
			node_data robotVer = robots.getStackRobotVertex();
			if(robotVer != null) {
				gameServer.chooseNextEdge(robotNum,
						nextNode(robotVer.getKey()));
				
				gameServer.move();
				robots.resetStackRobot();
			}	
			try {
				Thread.sleep(10);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	private int nextNode(int src) {
		int ans = -1;
		Collection<edge_data> ee = algo.getGraph().getE(src);
		Iterator<edge_data> itr = ee.iterator();
		int s = ee.size();
		int r = (int)(Math.random()*s);
		int i=0;
		while(i<r) {itr.next();i++;}
		ans = itr.next().getDest();
		return ans;
	}

}
