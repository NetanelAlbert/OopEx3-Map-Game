package gameClient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.TreeMap;
import Server.game_service;
import algorithms.Graph_Algo;
import gameDataStructure.Fruit;
import gameDataStructure.FruitsContainer;
import gameDataStructure.Robot;
import gameDataStructure.RobotsContainer;
import gameDataStructure.ServerInfo;
import grapgDataStructure.edge_data;
import grapgDataStructure.node_data;
import utils.Point3D;

/**
 * This class is a "robot player" that use an algorithms to play fast.
 * 
 * @author Netanel Albert
 */
public class AutoPlayer3 extends Thread {
	private final game_service gameServer;
	private final ServerInfo serverInfo;
	private final Graph_Algo algo;
	private final FruitsContainer fruits;
	private final RobotsContainer robots;
	private final double EPS;
	// All pairs short distance (dist[i][j] is the length from i to j).
	private double[][] dist;
	private int[][] routingTable; // [i][j] is the best node to go from i. to reach j
	
	private TreeMap<Integer, edge_data> robotsTargets = new TreeMap<Integer, edge_data>();
	private TreeMap<Integer, Point3D> robotsPoints = new TreeMap<Integer, Point3D>();
	private TreeMap<Integer, Long> ETA = new TreeMap<Integer, Long>(); // Estimated Time of Arrival (each robot);
	private HashSet<Point3D> hendledPoints
	/**
	 * Initialize the fields, calculate the shortest distance matrix and place the robots.
	 * 
	 * @param algo - the algorithm object with the graph inside it (getGraph()).
	 * @param EPS - epsilon - for calculations.
	 */
	public AutoPlayer3(game_service gameServer, ServerInfo serverInfo, Graph_Algo algo, FruitsContainer fruits,
			RobotsContainer robots, double EPS) {
		this.gameServer = gameServer;
		this.serverInfo = serverInfo;
		this.algo = algo;
		this.fruits = fruits;
		this.robots = robots;
		this.EPS = EPS;

		setDist();
		setRoutingTable();
		putRobots();

		robots.updateRobots();
		for (Robot rob : robots.getRobots()) {
			//ArrayList<node_data> targs = new ArrayList<node_data>();
			//targs.add(algo.getGraph().getNode(rob.getSrc()));
			//robotsTargets.put(rob.getId(), targs);
			ETA.put(rob.getId(), 0L);
		}

	}

	private void setRoutingTable() {
		long start = System.currentTimeMillis();
		int size = dist.length;
		routingTable = new int[size][size];
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				//if(i == j)
				routingTable[i][j] = -1;
			}
		}
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				if(dist[i][j] != 0 && dist[i][j] != Double.MAX_VALUE && routingTable[i][j] == -1)
					setRoutingTable(i,j);
			}
		}
		long end = System.currentTimeMillis();
		System.out.println("setRoutingTable() time: "+ (end-start));
	}

	private void setRoutingTable(int i, int j) {
		edge_data e = algo.getGraph().getEdge(i, j);
		if(e != null && e.getWeight() == dist[i][j])
			routingTable[i][j] = j;
		else {
			int dest = -1;
			double shortest = Double.MAX_VALUE;
			for (edge_data ed : algo.getGraph().getE(i)) {
				if(ed.getWeight() + dist[ed.getDest()][j] < shortest) {
					shortest = ed.getWeight() + dist[ed.getDest()][j];
					dest = ed.getDest();
				}
			}
			routingTable[i][j] = dest;
		}
	}

	/**
	 * The actual play part, mainly update the data and choose where will the robots go.
	 * Need to call by Start.
	 */
	@Override
	public void run() {
		int cleanCounter = 0;
		while (MyGameGUI.isRunning(gameServer)) {

			fruits.updateFruits();
			handleFruits();
			robots.updateRobots();
			moveRobots();
			
			cleanCounter++;
			if(cleanCounter >= 20) {
				clearHandlingFruits();
				cleanCounter = 0;
			}
			try {
				Thread.sleep(50);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void moveRobots() {
		for (Robot rob : robots.getRobots()) {
			if (rob.getDest() == -1)
				moveRobot(rob);
			}
	}

	private void moveRobot(Robot rob) {
		if(rob.getDest() != -1)
			return;
		
		edge_data e = robotsTargets.get(rob.getId());
		if(e != null) {
			if(rob.getSrc() != e.getSrc())
				gameServer.chooseNextEdge(rob.getId(), routingTable[rob.getSrc()][e.getSrc()]);
			else {
				gameServer.chooseNextEdge(rob.getId(), e.getDest());
				robotsTargets.put(rob.getId(), null);
				robotsPoints.put(rob.getId(), null);
			}
			
		}
	}

	/**
	 * Make sure that all the fruit have a robot that handle (go to) them.
	 */
	private void handleFruits() {
		for (Fruit fruit : fruits.getFruits()) {
			if (!handlingFruits.contains(fruit.getPos()))
				if(isOnTheWay(fruit))
					handlingFruits.add(0, fruit.getPos());
				else
					handleFruit(fruit);	
		}
	}
	
	

	private boolean isOnTheWay(Fruit fruit) {
		edge_data e = getEdgeByFruit(fruit);
		for (int robID : robotsTargets.keySet()) {
			if(isOnList(robotsTargets.get(robID), e))
				return true;
		}
		return false;
	}

	private boolean isOnList(ArrayList<node_data> list, edge_data e) {
		for (int i = 0; i < list.size()-1; i++) {
			int src = list.get(i).getKey();
			int dest = list.get(i+1).getKey();
			if(src == e.getSrc() && dest == e.getDest()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 
	 * Check which of the robot is best for handle this fruit @param fruit
	 */
	private void handleFruit(Fruit fruit) {
		edge_data edge = getEdgeByFruit(fruit);
		int fruitSrcNode = edge.getSrc();

		long eta = gameServer.timeToEnd() + 1000;
		ArrayList<node_data> targets = null;
		ArrayList<node_data> tmpTargs = null;
		int lastNode = 0;
		int handlerRobID = 0;
		for (int robID : ETA.keySet()) {
			long tmpETA = ETA.get(robID);
			tmpTargs = robotsTargets.get(robID);
			lastNode = tmpTargs.get(tmpTargs.size() - 1).getKey();
			double speed = robots.getRobotByID(robID).getSpeed();
			long addingTime = (long) (dist[lastNode][fruitSrcNode] / speed);
			if (tmpETA + addingTime < eta) {
				eta = tmpETA + addingTime;
				targets = tmpTargs;
				handlerRobID = robID;
			}
		}
		lastNode = targets.get(targets.size() - 1).getKey();
		List<node_data> path = algo.shortestPath(lastNode, fruitSrcNode);

		path.remove(0);
		targets.addAll(path);
		targets.add(algo.getGraph().getNode(edge.getDest()));
		
		ETA.put(handlerRobID, eta);
		handlingFruits.add(0, fruit.getPos());
		
	}

	
	private void clearHandlingFruits() {
		HashSet<Point3D> tmp = new HashSet<Point3D>()
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

				double diff = src.distance2D(dest) - src.distance2D(pos) - pos.distance2D(dest);
				if (Math.abs(diff) < EPS)
					return edge;
			}
		}
		return null;
	}
	/*
	 * Use for the dist matrix size, in case that the vertexes number arn't 0 to n
	 */
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
		Arrays.sort(f); // sort by weight - large first.
		for (int i = 0; i < serverInfo.getRobots(); i++) {
			if (i < f.length) {

				int pos = getEdgeByFruit(f[i]).getSrc();
				gameServer.addRobot(pos);
			} else {
				int nodeSize = algo.getGraph().nodeSize();
				gameServer.addRobot((int) (Math.random() * nodeSize));
			}
		}
	}
}
