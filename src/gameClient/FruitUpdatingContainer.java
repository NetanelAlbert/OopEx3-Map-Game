package gameClient;

import org.json.JSONArray;
import org.json.JSONObject;

import Server.game_service;

public class FruitUpdatingContainer extends Thread{
	private final game_service gameServer;
	private final ServerInfo serverInfo;
	private Fruit[] fruits;
	
	
	public FruitUpdatingContainer(game_service gameServer, ServerInfo serverInfo) {
		this.gameServer = gameServer;
		this.serverInfo = serverInfo;
		updateFruits();
	}
	
	@Override
	public void run() {
		while (gameServer.isRunning()) {
			updateFruits();
					
		}
	}

	private void updateFruits() {
		if (gameServer == null || serverInfo == null) {
			// TODO how to continue if null
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
					fruits[i].updateFruits(fruitsJSON);
			}	
		} catch (ArrayIndexOutOfBoundsException e) {
			System.out.println("FruistUpdate ERROR: \n"+
					"Graph: "+serverInfo.getGraph()+
					"\nGraph fruits: "+serverInfo.getFruits()+
					"\nfruitsJson: "+ fruitsJson.length());
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	public Fruit[] getFruits() {
		return fruits;
	}
}
