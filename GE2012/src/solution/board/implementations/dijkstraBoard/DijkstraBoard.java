package solution.board.implementations.dijkstraBoard;

import java.util.ArrayList;
import java.util.List;

import solution.CurrentGame;
import solution.board.HexPoint;
import solution.board.Player;
import solution.board.implementations.indivBoard.IndivBoard;
import solution.board.implementations.indivBoard.IndivNode;
import solution.debug.DebugWindow;

public class DijkstraBoard
{
	private IndivBoard indivBoard;
	private List<DijkstraNode> nodes = new ArrayList<DijkstraNode>();

	// the four walls
	private DijkstraNode wallA;// = new DijkstraNode(-1, '!');
	private DijkstraNode wallK;// = new DijkstraNode(-1, '!');
	private DijkstraNode wallOne;// = new DijkstraNode(-1, '!');
	private DijkstraNode wallEle;// = new DijkstraNode(-1, '!');

	private static final double ME_WEIGHT = 0;
	private static final double EMPTY_WEIGHT = 1;

	// private static final double YOU_WEIGHT = Double.POSITIVE_INFINITY;

	// NOTE: we need a new one generated whenever the board changes
	public DijkstraBoard(IndivBoard indivBoard, CurrentGame curr)
	{

		this.indivBoard = indivBoard;

		DebugWindow.println("Initializing map");

		HexPoint test = new HexPoint(1, 'a');

		DijkstraNode initial = new DijkstraNode(test.getX(), test.getY(), indivBoard.getNode(test).getOccupied());
		nodes.add(initial);
		createMap();//test, bridges, initial);
		
		
		if (curr.getConnectRoute() == CurrentGame.CONNECT_LETTERS)
		{
			wallA = new DijkstraNode(-1, '!', Player.ME);
			wallK = new DijkstraNode(-1, '!', Player.ME);
			wallOne = new DijkstraNode(-1, '!', Player.YOU);
			wallEle = new DijkstraNode(-1, '!', Player.YOU);
		}
		else
		{
			wallA = new DijkstraNode(-1, '!', Player.YOU);
			wallK = new DijkstraNode(-1, '!', Player.YOU);
			wallOne = new DijkstraNode(-1, '!', Player.ME);
			wallEle = new DijkstraNode(-1, '!', Player.ME);
		}
		
		nodes.add(wallA);
		nodes.add(wallK);
		nodes.add(wallOne);
		nodes.add(wallEle);

		// add the walls if we are touching
		for (DijkstraNode n : nodes)
		{
			// No elses because corners can be part of 2
			if (n == wallA || n == wallK || n == wallOne || n == wallEle)
				continue;
			
			if (n.getX() == 1)
				makeNeighbors(wallOne, n);

			if (n.getX() == 11)
				makeNeighbors(wallEle, n);

			if (n.getY() == 'a')
				makeNeighbors(wallA, n);

			if (n.getY() == 'k')
				makeNeighbors(wallK, n);
		}

		DebugWindow.println("Done map");
	}

	private DijkstraNode getNode(HexPoint pt)
	{
		for (DijkstraNode node : nodes)
		{
			if (node.getX() == pt.getX() && node.getY() == pt.getY())
				return node;
		}

		return null;
	}
	
	public synchronized double findDistance(HexPoint a, HexPoint b)
	{
		DijkstraNode dA = getNode(a);
		DijkstraNode dB = getNode(b);
		
		return findDistance(dA, dB);
	}
	
	public synchronized double findDistance(HexPoint a, DijkstraNode dB)
	{
		DijkstraNode dA = getNode(a);
		
		return findDistance(dA, dB);
	}


	public synchronized double findDistance(DijkstraNode dA, DijkstraNode dB)
	{
		resetNodes(); // puts them in a clean state for a new test

		dA.setNode(null, 0);

		// Is end still in the graph?
		while (!dB.isCompleted())
		{
			// Choose the node with the least distance
			
			double smallestWeight = Double.MAX_VALUE;
			DijkstraNode smallestNode = null;
			
			for (DijkstraNode n : nodes)
			{
				if (!n.isCompleted() && n.getWeight() <= smallestWeight)
				{
					smallestWeight = n.getWeight();
					smallestNode = n;
				}
			}
			
			
			// Remove it from the graph
			smallestNode.setCompleted(true);
			
			// Calculate distances between it and neighbors that are still in the graph
			// Update distances, choosing the lowest
			for (DijkstraNode n : smallestNode.getNeighbors())
			{
				if (!n.isCompleted())
				{
					double edgeWeight = 500;
					
					if (n.getPlayer() == Player.ME)
						edgeWeight = ME_WEIGHT;
					else if (n.getPlayer() == Player.EMPTY)
						edgeWeight = EMPTY_WEIGHT;
					else if (n.getPlayer() == Player.YOU)	// just means we got to a wall. ignore it.
						edgeWeight = 200;	// wont overflow but will never be lowest
					
					n.setNode(smallestNode, smallestNode.getWeight() + edgeWeight);
				}
			}
			
			
		}
		
		
		
		DijkstraNode node = dB;
		
		// Print path
		do
		{
			DebugWindow.println(node.getFrom().toString());
			node = node.getFrom();
		} while(node.getFrom() != null);

		return dB.getWeight();
	}

	private void resetNodes()
	{
		for (DijkstraNode n : nodes)
			n.resetNode();
	}

	public void makeNeighbors(DijkstraNode a, DijkstraNode b)
	{
		a.addNeighbor(b);
		b.addNeighbor(a);
	}

	private void createMap()
	{
		for (IndivNode newPoint : indivBoard.getPoints())
		{
			DijkstraNode newNode = new DijkstraNode(newPoint.getX(), newPoint.getY(), newPoint.getOccupied());
			nodes.add(newNode);
		}
		
		for (DijkstraNode node : nodes)
		{
			HexPoint point = new HexPoint(node.getX(), node.getY());
			
			for (HexPoint touch : point.touching())
			{
				node.addNeighbor(getNode(touch));
			}
		}
//		for (HexPoint newPoint : pt.touching())
//		{
//			Player p = indivBoard.getNode(newPoint).getOccupied();
//			if (!bridges.contains(newPoint))// && p != Player.YOU)
//			{
//				DijkstraNode newNode = new DijkstraNode(newPoint.getX(), newPoint.getY(), indivBoard.getNode(newPoint).getOccupied());
//
//				makeNeighbors(node, newNode);
//
//				bridges.add(newPoint);
//				nodes.add(newNode);
//
//				createMap(newPoint, bridges, newNode);
//			}
//		}
	}

	@Override
	public String toString()
	{
		return "DijkstraBoard [nodes=" + nodes + "]";
	}

	public DijkstraNode getWallA()
	{
		return wallA;
	}

	public DijkstraNode getWallK()
	{
		return wallK;
	}

	public DijkstraNode getWallOne()
	{
		return wallOne;
	}

	public DijkstraNode getWallEle()
	{
		return wallEle;
	}
}
