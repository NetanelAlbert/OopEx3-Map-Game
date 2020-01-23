package tests;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import dataBase.DBHelper;
import dataBase.MyGamesData;

class DatabaseTest {

	@Test
	void userInfoTest() {
		MyGamesData data = DBHelper.userInfo(-1);
		assertEquals(0, data.getCurrentLevel());
		assertEquals(0, data.getPlayedGames());
		assertEquals(0, data.getTopScors().size());
	}

	@Test
	void placeInLevelTest() {
		int ans = DBHelper.placeInLevel(0, 10000); // it's much higher then possible grade so must b 1st
		assertEquals(1, ans);
	}
}
