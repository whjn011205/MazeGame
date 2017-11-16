import java.io.Serializable;

public class PlayerInfo implements Serializable {
	
	public int pID = -1;
	public int directionX = -1;
	public int directionY = -1;
	public int playerTreasureCount = 0;

	public PlayerInfo() {
	}

	public PlayerInfo(int ID, int Direction_X, int Direction_Y, int Player_Treasure_Count) {
		pID = ID;
		directionX = Direction_X;
		directionY = Direction_Y;
		playerTreasureCount = Player_Treasure_Count;
	}
	
}