package tests;

import Server.Game_Server;
import Server.game_service;

public class SandBox {

	public static void main(String[] args) {
		game_service gameServer = Game_Server.getServer(5);
		gameServer.getFruits();
		gameServer.getRobots();
		gameServer.chooseNextEdge(0, 4);
		gameServer.addRobot(1);
		gameServer.startGame();
		gameServer.stopGame();
		gameServer.chooseNextEdge(0, 4);

		System.out.println("finish");
	}

}
