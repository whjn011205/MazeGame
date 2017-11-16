import java.io.Serializable;
import java.util.Vector;

public class ReplyPackage implements Serializable {

	public int primaryID = -1;
	public int backupID = -1;

	public int mazeSize = -1;
	public PlayerInfo playerInfo = new PlayerInfo();
	public MazeInfo[][] mazeInfo = null;

	public int error = 0; // error indicate the error message 0 means moved
							// successfully, 1 means target destination occupied
							// by other players, 2 means hit the wall

	public Vector<PlayerInfo> players = null;
	
	public ReplyPackage() {
	}

	public ReplyPackage(int Error, PlayerInfo Player_Info, MazeInfo[][] Maze_Info) {
		error = Error;
		playerInfo.pID = Player_Info.pID;
		playerInfo.directionX = Player_Info.directionX;
		playerInfo.directionY = Player_Info.directionY;
		playerInfo.playerTreasureCount = Player_Info.playerTreasureCount;
		mazeInfo = Maze_Info.clone();
	}

}