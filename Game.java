import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Vector;

public interface Game extends Remote {

	ReplyPackage join() throws RemoteException;

	ReplyPackage move(int ID, String Dir) throws RemoteException;

	ReplyPackage gameStatus(int ID) throws RemoteException;

	void update(MazeInfo[][] Maze_Info, Vector<PlayerInfo> Players_, int Maze_Size, int Treasure_Count)
			throws RemoteException;

	ReplyPackage primaryDown(int ID) throws RemoteException;

	void backupUp() throws RemoteException;

	int waitStart() throws RemoteException;

}