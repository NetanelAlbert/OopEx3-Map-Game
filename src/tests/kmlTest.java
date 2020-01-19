package tests;

import static org.junit.jupiter.api.Assertions.*;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import Server.Game_Server;
import Server.game_service;
import gameClient.KML_Logger;
import gameDataStructure.FruitsContainer;
import gameDataStructure.RobotsContainer;
import gameDataStructure.ServerInfo;
import grapgDataStructure.DGraph;

class kmlTest {

	@Test
	void kmlWriteTest() {
		game_service gameServer = Game_Server.getServer(0);
		ServerInfo si = null;
		try {
			si = new ServerInfo(gameServer);
		} catch (JSONException e) {
			e.printStackTrace();
			fail("fail to creat info");
		}
		FruitsContainer fc = new FruitsContainer(gameServer, si);
		RobotsContainer rc = new RobotsContainer(gameServer, si);
		DGraph graph = null;
		try {
			graph = new DGraph(new JSONObject(gameServer.getGraph()));
		} catch (JSONException e) {
			e.printStackTrace();
			fail("fail to creat graph");
		}
		
		KML_Logger kml = new KML_Logger(graph, fc, rc);
		gameServer.addRobot(1);
		gameServer.startGame();
		rc.updateRobots();
		fc.updateFruits();
		kml.writeStatus();
		gameServer.stopGame();
		kml.closeKml();
		kml.save("test.kml");	
	}

}
