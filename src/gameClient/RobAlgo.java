package gameClient;

import java.util.ArrayList;
import java.util.List;

import Server.game_service;
import algorithms.Graph_Algo;
import gameDataStructure.Fruit;
import gameDataStructure.FruitsContainer;
import gameDataStructure.Robot;
import grapgDataStructure.edge_data;
import grapgDataStructure.node_data;
import utils.Point3D;

public class RobAlgo {

	private final game_service gameServer;
	private final Graph_Algo algo;
	private final double[][] dist;
	private final Robot robot;
	private final FruitsContainer fruits;
	
	private ArrayList<node_data> path = new ArrayList<node_data>();
	private long ETA = 0;
	
	private long timeOfMove;
	private double currentEdgeW;
	
	private ArrayList<Point3D> handlingFruit = new ArrayList<Point3D>();
	private double fruitsValue;
	
	
	public RobAlgo(Robot rob, Graph_Algo algo, double[][] dist,
			game_service gameServer, FruitsContainer fruits) {
		
		this.robot = rob;
		this.algo = algo;
		this.dist = dist;
		this.gameServer = gameServer;
		this.fruits = fruits;
		
		path.add(algo.getGraph().getNode(rob.getSrc()));
	}
	
	public double cutProfit(int dest, double value) {
		int src = (robot.getDest() == -1) ? robot.getSrc() : robot.getDest();
		double newPathDist = dist[src][dest];
		long newPathTime = timeOfWalk(newPathDist) + timeToNextNode();
		double newVal = value/newPathTime;
		double curVal = (ETA != 0) ? fruitsValue/ETA : fruitsValue;
		
		return newVal - curVal;
	}
	
	public double addProfit(int dest, double value) {
		int src = endOfPath();
		double newPathDist = dist[src][dest];
		long newPathTime = timeOfWalk(newPathDist);
		double newVal = value/newPathTime;
	
		return newVal;
	}
	
	public void addPath(edge_data e, Fruit target) {
		updatePath(e, target, true);
	}
	
	public void changePath(edge_data e, Fruit target) {
		path.clear();		
		ETA = 0;
		handlingFruit.clear();
		fruitsValue = 0;
		
		updatePath(e, target, false);
	}
	
	private void updatePath(edge_data e, Fruit target, boolean add) {
		int src = add ? endOfPath() : robot.getSrc();
		double distance = dist[src][e.getSrc()]+e.getWeight();

		List<node_data> p = algo.shortestPath(src, e.getSrc());
		p.add(algo.getGraph().getNode(e.getDest()));
		if(add)
			p.remove(0);
		path.addAll(p);
		
		ETA += timeOfWalk(distance);
		handlingFruit.add(0, target.getPos());
		fruitsValue += target.getValue();
		
		updateHandledFruits();
	}
	
	private void updateHandledFruits() {
		ArrayList<Point3D> tmp = new ArrayList<Point3D>();
		fruitsValue = 0;
		for (Fruit f : fruits.getFruits()) {
			if(handlingFruit.contains(f.getPos())) {
				tmp.add(f.getPos());
				fruitsValue += f.getValue();
			}
		}
		handlingFruit = tmp;
	}

	public void move() {
		if(onMove() || isStuck())
			return;
		
		timeOfMove = System.currentTimeMillis();
		int src = path.get(0).getKey();
		int dest = path.get(1).getKey();
		currentEdgeW = dist[src][dest];
		
		gameServer.chooseNextEdge(robot.getId(), dest);
		path.remove(0);
	}
	
	public boolean isHandle(Fruit f, edge_data e) {
		return handlingFruit.contains(f.getPos()) || isOnTheWay(f, e);
	}
	
	private boolean isOnTheWay(Fruit f, edge_data e) {
		for (int i = 0; i < path.size()-1; i++) {
			int src = path.get(i).getKey();
			int dest = path.get(i+1).getKey();
			if(src == e.getSrc() && dest == e.getDest()) {
				handlingFruit.add(0, f.getPos());
				fruitsValue += f.getValue();
				return true;
			}
		}
		return false;
	}

	public boolean onMove() {
		return robot.getDest() != -1;
	}

	public boolean isStuck() {
		return this.path.size() <= 1;
	}
	
	public int nextNode() {
		if(isStuck())
			return -1;
		return path.get(1).getKey();
	}
	
	public long timeToNextNode() {
		if(isStuck())
			return 0;
		
		long timeFromMove = System.currentTimeMillis() - timeOfMove;
		long totalEdgeTime = timeOfWalk(currentEdgeW);
		return totalEdgeTime - timeFromMove;
	}
	
	public long GetETA() {
		return ETA;
	}
	
	public int endOfPath() {
		return path.get(path.size()-1).getKey();
	}
	
	private long timeOfWalk(double distance) {
		return (long)(1000*distance/robot.getSpeed());
	}

}
