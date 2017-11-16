import java.io.Serializable;

public class MazeInfo implements Serializable {

	public int occupyFlag = -1; // Indicate the current coordinate is occupied
								// or not, -1 for not-occupy, 1 for occupy
	public int mazeTreasure = 0; // Indicate the number of treasures at this
									// coordinate

	public MazeInfo() {
	}

	public MazeInfo(int Occupy_Flag, int Maze_Treasure) {
		occupyFlag = Occupy_Flag;
		mazeTreasure = Maze_Treasure;
	}

}