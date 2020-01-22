package gameClient;

import java.util.Arrays;
import java.util.TreeMap;

import Server.game_service;
import algorithms.Graph_Algo;
import algorithms.RobAlgo;
import gameDataStructure.Fruit;
import gameDataStructure.FruitsContainer;
import gameDataStructure.Robot;
import gameDataStructure.RobotsContainer;
import gameDataStructure.ServerInfo;
import grapgDataStructure.edge_data;
import grapgDataStructure.node_data;
import utils.Point3D;

/*
 * new strategy: for each robot keep 1 fruit as dest,
 * and if foud fruit that its (value / time to reach) is greater so swetch dest.
 * 
 * optional: chack wich robot will get the moste of taking it.
 */

public class AutoPlayer2 extends Thread {
	private final game_service gameServer;
	private final ServerInfo serverInfo;
	private final Graph_Algo algo;
	private final FruitsContainer fruits;
	private final RobotsContainer robots;
	private final double EPS;
	private double[][] dist;
	private TreeMap<Integer, RobAlgo> androids = new TreeMap<Integer, RobAlgo>();
	
	public AutoPlayer2(game_service gameServer, ServerInfo serverInfo, Graph_Algo algo, FruitsContainer fruits,
			RobotsContainer robots, double EPS) {
		this.gameServer = gameServer;
		this.serverInfo = serverInfo;
		this.algo = algo;
		this.fruits = fruits;
		this.robots = robots;
		this.EPS = EPS;

		setDist();
		putRobots();

		robots.updateRobots();
		for (Robot rob : robots.getRobots()) {
			RobAlgo adnro = new RobAlgo(rob, algo, dist, gameServer, fruits);
			androids.put(rob.getId(), adnro);
		}

	}

	@Override
	public void run() {
		long start,end,current,max = 0;
		while (MyGameGUI.isRunning(gameServer)) {
			start = System.currentTimeMillis();
			
			fruits.updateFruits();
			robots.updateRobots();
			handleFruits();
			moveRobots();
			
			end = System.currentTimeMillis();
			current = end - start;
			if(max < current)
				max = current;
			
			try {
				Thread.sleep(50);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		System.out.println("AutoPlayer2 max delay: "+max);
	}

	private void moveRobots() {
		for (RobAlgo andro : androids.values()) {
			andro.move();
		}
	}

	private void handleFruits() {
		Fruit[] fruitsArr = fruits.getFruits();
		Arrays.sort(fruitsArr);
				
		for (Fruit fruit : fruitsArr) {
			edge_data e = getEdgeByFruit(fruit);
			if (isHandling(fruit, e))
				continue;
			
			handleFruit(fruit, e);		
			
		}
	}
	
	private void handleFruit(Fruit fruit, edge_data e) {
		
		double profit = 0;
		RobAlgo handler = null;
		boolean cut = false;
		
		for (RobAlgo andro : androids.values()) {
			double tmpProf = andro.addProfit(e.getSrc(), fruit.getValue());
			if(andro.isStuck())
				tmpProf *= 2;
			
			if(profit < tmpProf) {
				profit = tmpProf;
				handler = andro;
				cut = false;
			}
			
			if(andro.isStuck())
				continue;
				
			
			
			tmpProf = andro.cutProfit(e.getSrc(), fruit.getValue());
			if(profit < tmpProf) {
				profit = tmpProf;
				handler = andro;
				cut = true;
			}
		}
		
		if(profit > 0) {
			if(cut)
				handler.changePath(e, fruit);
			else
				handler.addPath(e, fruit);
		}
	}

	private boolean isHandling(Fruit fruit, edge_data e) {
		for (RobAlgo andro : androids.values()) {
			if(andro.isHandle(fruit, e))
				return true;
		}
		return false;
	}

	private edge_data getEdgeByFruit(Fruit fruit) {
		if (fruit == null)
			return null;

		Point3D pos = fruit.getPos();
		int type = fruit.getType();

		for (node_data node : algo.getGraph().getV()) {
			for (edge_data edge : algo.getGraph().getE(node.getKey())) {
				if ((type > 0) != (edge.getSrc() < edge.getDest()))
					continue;
				Point3D src = algo.getGraph().getNode(edge.getSrc()).getLocation();
				Point3D dest = algo.getGraph().getNode(edge.getDest()).getLocation();

				double diff = src.distance2D(pos) + pos.distance2D(dest) - src.distance2D(dest);
				if (diff < EPS) // TODO maybe Math.abs()
					return edge;
			}
		}
		return null;
	}

	private int maxKeyInGraph() {
		if (algo == null || algo.getGraph() == null)
			return -1;

		int max = -1;
		for (node_data node : algo.getGraph().getV()) {
			if (max < node.getKey())
				max = node.getKey();
		}
		return max;
	}

	/**
	 * All Pairs shortest path algorithm
	 */
	private void setDist() {
		int size = maxKeyInGraph() + 1;
		if (size == 0)
			return;

		dist = new double[size][size];
		for (int i = 0; i < dist.length; i++) {
			for (int j = 0; j < dist.length; j++) {
				if (i == j && algo.getGraph().getNode(i) != null)
					dist[i][j] = 0;
				else
					dist[i][j] = Double.MAX_VALUE;
			}
		}

		for (node_data node : algo.getGraph().getV()) {
			for (edge_data edge : algo.getGraph().getE(node.getKey())) {
				dist[edge.getSrc()][edge.getDest()] = edge.getWeight();
			}
		}

		for (int k = 0; k < dist.length; k++) {
			for (int i = 0; i < dist.length; i++) {
				for (int j = 0; j < dist.length; j++) {
					if (dist[i][j] > dist[i][k] + dist[k][j])
						dist[i][j] = dist[i][k] + dist[k][j];
				}
			}
		}
	}

	/**
	 * should call only once (before starting the game)
	 */
	private void putRobots() {
		Fruit[] f = fruits.getFruits();
		Arrays.sort(f);
		for (int i = 0; i < serverInfo.getRobots(); i++) {
			if (i < f.length) {

				int pos = getEdgeByFruit(f[i]).getSrc();
				gameServer.addRobot(pos);
				// TODO add to targets list?
			} else {
				int nodeSize = algo.getGraph().nodeSize();
				gameServer.addRobot((int) (Math.random() * nodeSize));
			}
		}
	}
}
