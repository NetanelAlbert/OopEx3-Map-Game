package tests;

import static org.junit.jupiter.api.Assertions.*;

import org.json.JSONException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import Server.Game_Server;
import Server.game_service;
import gameDataStructure.ServerInfo;

class GameDataTest {
	game_service gameServer;
	
	@BeforeEach
	void initServer() {
		gameServer = Game_Server.getServer(0);
	}
	
	
	@Test
	void infoTest() {
		ServerInfo si = null;
		try {
			si = new ServerInfo(gameServer);
		} catch (JSONException e) {
			e.printStackTrace();
			fail("fail to creat info");
		}
		
		if(si.getFruits() != 1 || si.getRobots() != 1 || si.getGrade() != 0)
			fail("info is wrong");
	}

}
