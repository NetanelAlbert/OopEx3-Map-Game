package gameDataStructure;

import org.json.JSONArray;
import org.json.JSONObject;

import Server.game_service;

/**
 * This Class is holding a collection of Fruits, 
 * and should be update all the time by updateFruits(),
 * that take a new information from server
 * 
 * @author Netanel Albert
 */
public class FruitsContainer {
	private final game_service gameServer;
	private final ServerInfo serverInfo;
	private Fruit[] fruits;
	
	/**
	 * 
	 * @param gameServer, @param serverInfo - needed for update the information
	 */
	public FruitsContainer(game_service gameServer, ServerInfo serverInfo) {
		this.gameServer = gameServer;
		this.serverInfo = serverInfo;
		updateFruits();
	}
	
	/**
	 * Update the information about the fruits from the server.
	 */
	public synchronized void updateFruits() {
		if (gameServer == null || serverInfo == null) {
			return;
		}
		if (fruits == null || fruits.length != serverInfo.getFruits())
			fruits = new Fruit[serverInfo.getFruits()];
		
		JSONArray fruitsJson = null;
		try {
			fruitsJson = new JSONArray(gameServer.getFruits());
			for (int i = 0; i < fruitsJson.length(); i++) {
				JSONObject fruitsJSON = new JSONObject(fruitsJson.getString(i)).getJSONObject("Fruit");
				if(fruits[i] == null)
					fruits[i] = new Fruit(fruitsJSON);
				else
					fruits[i].updateFruit(fruitsJSON);
			}	
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public synchronized Fruit[] getFruits() {
		return fruits;
	}
}
