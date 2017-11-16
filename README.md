# This projects implements a P2P maze game:
-	There is a maze map, and there are some treasures in the maze
-	Players will join in the game, and collect the treasures. Players that collect the most treasures wins
-	Since this is a P2P version, there is no dedicated server because every peer(player) can be the server
-	Fault tolerance mechanism: we use a primary server and a backup(secondary) server, both servers will be selected from the peers. If one server is down, we will recover this server using the other server.

# Files Description:
-	## Game.java:	
	Definition of "Game" interface
-	Peer.java:	
	Implementation of the "Game" interface. This is also where the main function is located

-	PlayerInfo/ReplyPackage/MazeInfo.java:	Define some classes to store game data, such as the game maze map, or the player information(where the player is in the map), etc.