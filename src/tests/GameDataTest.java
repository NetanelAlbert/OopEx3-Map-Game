package tests;

import static org.junit.jupiter.api.Assertions.*;

import org.json.JSONException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import Server.Game_Server;
import Server.game_service;
import gameDataStructure.Fruit;
import gameDataStructure.FruitsContainer;
import gameDataStructure.Robot;
import gameDataStructure.RobotsContainer;
import gameDataStructure.ServerInfo;

class GameDataTest {
	static game_service gameServer;
	static ServerInfo si;
	@BeforeAll
	static void serverInfoTest() {
		gameServer = Game_Server.getServer(0);
		gameServer.addRobot(4);
		try {
			si = new ServerInfo(gameServer);
		} catch (JSONException e) {
			e.printStackTrace();
			fail("fail to creat info");
		}
		
		if(si.getFruits() != 1 || si.getRobots() != 1 || si.getGrade() != 0)
			fail("info is wrong");
	}
	
	/**
	 * test the constructor and update.
	 */
	@SuppressWarnings("unused")
	@Test
	void fruitContainerTest() {
		FruitsContainer fc = new FruitsContainer(gameServer, si);
		fc.updateFruits();
		
		int count = 0;
		for (Fruit fruit : fc) {
			count++;
		}
		assertEquals(si.getFruits(), count);
	}
	

	/**
	 * test the constructor and update.
	 */
	@SuppressWarnings("unused")
	@Test
	void robotContainerTest() {
		RobotsContainer rc = new RobotsContainer(gameServer, si);
		rc.updateRobots();
		assertEquals(0, rc.getRobotByID(0).getId());
		
		int count = 0;
		for (Robot robot : rc) {
			count++;
		}
		assertEquals(si.getRobots(), count);
	}
	
	
}
