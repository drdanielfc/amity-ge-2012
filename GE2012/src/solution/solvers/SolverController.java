package solution.solvers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import solution.CurrentGame;
import solution.board.HexPoint;
import solution.board.Player;
import solution.board.implementations.dijkstraBoard.DijkstraBoard;
import solution.board.implementations.dijkstraBoard.DijkstraNode;
import solution.board.implementations.indivBoard.IndivBoard;
import solution.board.implementations.indivBoard.IndivNode;
import solution.debug.DebugWindow;

/**
 * This controls all our solvers and generates the master weight table
 * Vocabulary: 
 * two-chain: This occurs when there are two empty points between two spaces
 *			  or between a point and the wall. When there is a two-chain,
 *			  we can guarantee a connection between the two points or a 
 *			  connection to the wall.
 * 
 * @author Daniel Centore
 * @author Mike DiBuduo
 *
 */
public class SolverController
{
	private CurrentGame curr; // Current game
	private IndivBoard indivBoard;
	private DijkstraBoard dijkstraBoard = null;
	private ClassicBlock classicBlock;
	private HexPoint lastMove;
	private double difficulty = -1;

	public SolverController(CurrentGame curr)
	{
		this.curr = curr;
		indivBoard = curr.getBoardController().getIndivBoard();
		classicBlock = new ClassicBlock(indivBoard, curr, this);
	}

	/**
	 * Chooses our next move
	 * @return the next {@link HexPoint} to occupy
	 */
	public HexPoint getMove(HexPoint lastMove)
	{
		this.lastMove = lastMove;

		HexPoint broken = null;

		try
		{
			if (classicBlock.shouldBlock())
			{
				// Fix chains between points if necessary
				broken = twoChainsBroken();
				if (broken != null)
				{
					DebugWindow.println("Came from A");
					return broken;
				}

				try
				{
					// Fix chains between a point and the wall if necessary
					broken = baseTwoChainsBroken();
					if (broken != null)
					{
						DebugWindow.println("Came from B");
						return broken;
					}
				} catch (Exception e)
				{
					// Fails on some corner cases. just ignore this.
				}

				broken = classicBlock.block(lastMove);

				if (broken != null)
				{
					DebugWindow.println("Came from C");
					return broken;
				}
			}
		} catch (Exception e1)
		{
			DebugWindow.println("ERROR: ClassicBlock crashed. Take a look at the trace. Using Default solver.");
			e1.printStackTrace();
		}

		dijkstraBoard = new DijkstraBoard(indivBoard, curr);
		
		HexPoint follow = followChain(); // we do this early so we can use the weight from it
		
		// Fix chains between points if necessary
		broken = twoChainsBroken();
		if (broken != null)
		{
			DebugWindow.println("Came from D");
			return broken;
		}

		try
		{
			// Fix chains between a point and the wall if necessary
			broken = baseTwoChainsBroken();
			if (broken != null)
			{
				DebugWindow.println("Came from E");
				return broken;
			}
		} catch (Exception e)
		{
			// Fails on some corner cases. just ignore this.
		}

		// grab immediate fixes
		broken = immediatePoint();
		if (broken != null)
		{
			DebugWindow.println("Came from F");
			return broken;
		}

		DebugWindow.println("Came from G");
		// follow chain down board
		return follow;

		// TODO: Use a new method which actually checks all 2 chains and makes sure they're connected
	}

	/**
	 * Checks to see if there are two-chains all the way across the board
	 * @return true if there is a two-chain path across the board, false if not
	 */
	private boolean across(boolean left)
	{
		boolean a = false;
		boolean b = false;

		for (IndivNode node : indivBoard.getPoints())
		{
			if (node.getOccupied() == Player.ME)
			{
				if (curr.getConnectRoute() == CurrentGame.CONNECT_LETTERS)
				{
					if (node.getY() == 'a')
						a = true;
					else if (node.getY() == 'b')
					{
						// check the connection to the wall
						HexPoint[] k = { new HexPoint(node.getX(), 'a'), new HexPoint(node.getX() + 1, 'a') };
						List<HexPoint> good = new ArrayList<HexPoint>();
						for (HexPoint j : k)
						{
							if (j.isGood())
								good.add(j);
						}

						if (IndivNode.empty(good, indivBoard) && good.size() == 2) // only if both connections are good
							a = true;
					}

					if (node.getY() == 'k')
						b = true;
					else if (node.getY() == 'j')
					{
						// check the connection to the wall
						HexPoint[] k = { new HexPoint(node.getX(), 'k'), new HexPoint(node.getX() - 1, 'k') };

						List<HexPoint> good = new ArrayList<HexPoint>();
						for (HexPoint j : k)
						{
							if (j.isGood())
								good.add(j);
						}

						if (IndivNode.empty(good, indivBoard) && good.size() == 2) // only if both connections are good
							b = true;
					}
				}
				else
				{
					if (node.getX() == 1 && node.getY() > 'a')		// TODO: second part is a kludge. actually check if we're connected.
						a = true;
					else if (node.getX() == 2)
					{
						// check the connection to the wall
						HexPoint[] k = { new HexPoint(1, node.getY()), new HexPoint(1, (char) (node.getY() + 1)) };

						List<HexPoint> good = new ArrayList<HexPoint>();
						for (HexPoint j : k)
						{
							if (j.isGood())
								good.add(j);
						}

						if (IndivNode.empty(good, indivBoard)) // only if both connections are good
							a = true;
					}

					if (node.getX() == 11)
						b = true;
					else if (node.getX() == 10)
					{
						// check the connection to the wall
						HexPoint[] k = { new HexPoint(11, node.getY()), new HexPoint(11, (char) (node.getY() - 1)) };

						List<HexPoint> good = new ArrayList<HexPoint>();
						for (HexPoint j : k)
						{
							if (j.isGood())
								good.add(j);
						}

						if (IndivNode.empty(good, indivBoard)) // only if both connections are good
							b = true;
					}
				}
			}
		}

		System.out.println("Left" + a);
		System.out.println("right" + b);

		return (left ? a : b);

	}

	private boolean connectedToWall(HexPoint node)
	{
		if (curr.getConnectRoute() == CurrentGame.CONNECT_LETTERS)
		{
			if (node.getY() == 'a')
				return true;
			else if (node.getY() == 'b')
			{
				// check the connection to the wall
				HexPoint[] k = { new HexPoint(node.getX(), 'a'), new HexPoint(node.getX() + 1, 'a') };

				List<HexPoint> good = new ArrayList<HexPoint>();
				for (HexPoint j : k)
				{
					if (j.isGood())
						good.add(j);
				}

				if (IndivNode.empty(good, indivBoard)) // only if both connections are good
					return true;
			}

			if (node.getY() == 'k')
				return true;
			else if (node.getY() == 'j')
			{
				// check the connection to the wall
				HexPoint[] k = { new HexPoint(node.getX(), 'k'), new HexPoint(node.getX() - 1, 'k') };

				List<HexPoint> good = new ArrayList<HexPoint>();
				for (HexPoint j : k)
				{
					if (j.isGood())
						good.add(j);
				}

				if (IndivNode.empty(good, indivBoard)) // only if both connections are good
					return true;
			}
		}
		else
		{
			if (node.getX() == 1)
				return true;
			else if (node.getX() == 2)
			{
				// check the connection to the wall
				HexPoint[] k = { new HexPoint(1, node.getY()), new HexPoint(1, (char) (node.getY() + 1)) };

				List<HexPoint> good = new ArrayList<HexPoint>();
				for (HexPoint j : k)
				{
					if (j.isGood())
						good.add(j);
				}

				if (IndivNode.empty(good, indivBoard)) // only if both connections are good
					return true;
			}

			if (node.getX() == 11)
				return true;
			else if (node.getX() == 10)
			{
				// check the connection to the wall
				HexPoint[] k = { new HexPoint(11, node.getY()), new HexPoint(11, (char) (node.getY() - 1)) };

				List<HexPoint> good = new ArrayList<HexPoint>();
				for (HexPoint j : k)
				{
					if (j.isGood())
						good.add(j);
				}

				if (IndivNode.empty(good, indivBoard)) // only if both connections are good
					return true;
			}
		}

		return false;
	}

	// tries to find a point right around a hex which will connect to a side
	private HexPoint immediatePoint()
	{
		for (IndivNode node : indivBoard.getPoints())
		{
			if (node.getOccupied() == Player.ME)
			{
				// DebugWindow.println(node.toString() + " " + node.getPoints().get(0).touching().toString());

				for (HexPoint around : node.getPoints().get(0).touching())
				{
					if (connectedToWall(around) && indivBoard.getNode(around).getOccupied() == Player.EMPTY && !connectedToWall(node.getPoints().get(0)))
					{
						boolean left = false;
						if ((curr.getConnectRoute() == CurrentGame.CONNECT_LETTERS && around.getY() < 'f') ||
								(curr.getConnectRoute() == CurrentGame.CONNECT_NUMBERS && around.getX() < 6))
							left = true;

						if (!across(left))
							return around;
					}
				}
			}
		}

		return null;
	}

	// slips through an opening of two of the other player's pieces
	private HexPoint slipThrough()
	{
		for (IndivNode node : indivBoard.getPoints())
		{
			if (node.getOccupied() == Player.YOU)
			{
				for (HexPoint bridge : node.getTwoChains())
				{
					if (indivBoard.getNode(bridge).getOccupied() == Player.YOU)
					{
						List<HexPoint> conns = bridge.connections(node.getPoints().get(0));

						if (indivBoard.getNode(conns.get(0)).getOccupied() == Player.ME && indivBoard.getNode(conns.get(1)).getOccupied() == Player.EMPTY)
						{
							// DebugWindow.println(node.toString() + " " + node.getTwoChains());
							return conns.get(1);
						}
						else if (indivBoard.getNode(conns.get(1)).getOccupied() == Player.ME && indivBoard.getNode(conns.get(0)).getOccupied() == Player.EMPTY)
						{
							return conns.get(0);
						}
					}
				}
			}
		}

		return null;
	}

	/**
	 * gets the next {@link HexPoint} in order to follow a two-chain across the board
	 * used when there is a complete two-chain connection from one side to the other
	 * @return the next {@link HexPoint} to complete the chain
	 */
	private HexPoint followChain()
	{
		List<HexPoint> possible = new ArrayList<HexPoint>();

		// add all 2-chains as possible
		for (IndivNode node : indivBoard.getPoints())
		{
			if (node.getOccupied() == Player.ME)
			{
				List<HexPoint> chains = node.getTwoChains();// indivBoard);
				for (HexPoint pnt : chains)
				{
					if (indivBoard.getNode(pnt).getOccupied() == Player.EMPTY && IndivNode.empty(pnt.connections(node.getPoints().get(0)), indivBoard))
					{
						possible.add(pnt);
					}
				}
			}
		}

		// DebugWindow.println(possible.toString());
		Iterator<HexPoint> itr = possible.iterator();

		double left = Double.MAX_VALUE;
		HexPoint bestLeft = null;

		double right = Double.MAX_VALUE;
		HexPoint bestRight = null;

		if (!itr.hasNext())
			return null;

		do
		{
			HexPoint h = itr.next();

			double leftDist = calculateDistance(h, true);
			double rightDist = calculateDistance(h, false);

			if (leftDist < left)
			{
				bestLeft = h;
				left = leftDist;
			}

			if (rightDist < right)
			{
				bestRight = h;
				right = rightDist;
			}

		} while (itr.hasNext());

		if (across(true) && bestRight != null)
			return bestRight;
		else if (across(false) && bestLeft != null)
			return bestLeft;

		HexPoint worse;
		if (left > right && bestLeft != null)
		{
			difficulty = left;
			worse = bestLeft;

			// Slip through if it'll be beneficial
			HexPoint broken = slipThrough();
			if (broken != null && (curr.getConnectRoute() == CurrentGame.CONNECT_LETTERS ? broken.getY() < 'f' : broken.getX() < 6) && left > 4)
				worse = broken;
		}
		else
		{
			difficulty = right;
			worse = bestRight;

			// Slip through if it'll be beneficial
			HexPoint broken = slipThrough();
			if (broken != null && (curr.getConnectRoute() == CurrentGame.CONNECT_LETTERS ? broken.getY() < 'f' : broken.getX() < 6) && left > 4)
				worse = broken;
		}

		return worse;
	}

	/**
	 * Figures out how hard it would be to get to the closest wall
	 * @param pnt the starting {@link HexPoint}
	 * @return The difficulty (arbitrary scale)
	 */
	private double calculateDistance(HexPoint pnt, boolean left)
	{

		DijkstraNode wall;

		if (curr.getConnectRoute() == CurrentGame.CONNECT_LETTERS)
		{
			if (left)
				wall = dijkstraBoard.getWallA();
			else
				wall = dijkstraBoard.getWallK();
		}
		else
		{
			if (left)
				wall = dijkstraBoard.getWallOne();
			else
				wall = dijkstraBoard.getWallEle();
		}

		double dist = dijkstraBoard.findDistance(pnt, wall);

		if (curr.getConnectRoute() == CurrentGame.CONNECT_LETTERS)
		{
			dist *= (left == (pnt.getY() < 'f') ? 1 : 1.5);
		}
		else
		{
			dist *= (left == (pnt.getX() < 6) ? 1 : 1.5);
		}

		return dist;

	}

	/**
	 * Checks if the path ehre is broken
	 * @param a
	 * @param b
	 * @return
	 */
	private boolean broken(HexPoint a, HexPoint b)
	{
		List<HexPoint> conns = a.connections(b);

		return !IndivNode.empty(conns, indivBoard);
	}

	/**
	 * Checks if two-chains to the walls are broken
	 * @return the {@link HexPoint} needed to fix a broken two-chain between a point and the wall (or null if none are broken)
	 */
	private HexPoint baseTwoChainsBroken()
	{
		niceloop: for (IndivNode node : indivBoard.getPoints())
		{
			if (node.getOccupied() == Player.ME)
			{
				HexPoint pt = node.getPoints().get(0);

				HexPoint[] bad = { new HexPoint(11, 'b'), new HexPoint(1, 'j'), new HexPoint(2, 'k'), new HexPoint(10, 'a') };

				// skip the corners - these aren't two chains anyway
				for (HexPoint b : bad)
				{
					// DebugWindow.println("Skipping over: "+b.toString());
					if (node.equals(b))
						continue niceloop;
				}

				// DebugWindow.println("Testing bridge: "+node.toString());

				if (curr.getConnectRoute() == CurrentGame.CONNECT_LETTERS && (pt.getY() == 'b' || pt.getY() == 'j'))
				{
					if (pt.getY() == 'b')
					{
						if ((indivBoard.getNode(pt.getX(), 'a').getOccupied() == Player.YOU))
							if ((indivBoard.getNode(pt.getX() + 1, 'a').getOccupied() == Player.EMPTY))
								return new HexPoint(pt.getX() + 1, 'a');

						if ((indivBoard.getNode(pt.getX() + 1, 'a').getOccupied() == Player.YOU))
							if ((indivBoard.getNode(pt.getX(), 'a').getOccupied() == Player.EMPTY))
								return new HexPoint(pt.getX(), 'a');
					}
					else
					{
						if ((indivBoard.getNode(pt.getX(), 'k').getOccupied() == Player.YOU))
							if ((indivBoard.getNode(pt.getX() - 1, 'k').getOccupied() == Player.EMPTY))
								return new HexPoint(pt.getX() - 1, 'k');

						if ((indivBoard.getNode(pt.getX() - 1, 'k').getOccupied() == Player.YOU))
							if ((indivBoard.getNode(pt.getX(), 'k').getOccupied() == Player.EMPTY))
								return new HexPoint(pt.getX(), 'k');
					}
				}
				else if (curr.getConnectRoute() == CurrentGame.CONNECT_NUMBERS && (pt.getX() == 2 || pt.getX() == 10))
				{
					if (pt.getX() == 2)
					{
						if ((indivBoard.getNode(1, pt.getY()).getOccupied() == Player.YOU))
							if ((indivBoard.getNode(1, (char) (pt.getY() + 1)).getOccupied() == Player.EMPTY))
								return new HexPoint(1, (char) (pt.getY() + 1));

						if ((indivBoard.getNode(1, (char) (pt.getY() + 1)).getOccupied() == Player.YOU))
							if ((indivBoard.getNode(1, pt.getY()).getOccupied() == Player.EMPTY))
								return new HexPoint(1, pt.getY());
					}
					else
					{
						if ((indivBoard.getNode(11, pt.getY()).getOccupied() == Player.YOU))
							if ((indivBoard.getNode(11, (char) (pt.getY() - 1)).getOccupied() == Player.EMPTY))
								return new HexPoint(11, (char) (pt.getY() - 1));

						if ((indivBoard.getNode(11, (char) (pt.getY() - 1)).getOccupied() == Player.YOU))
							if ((indivBoard.getNode(11, pt.getY()).getOccupied() == Player.EMPTY))
								return new HexPoint(11, pt.getY());
					}
				}
			}
		}

		return null;
	}

	/**
	 * Checks if a two chain has been broken. 
	 * @return the {@link HexPoint} needed to fix a broken two-chain (or null if none are broken)
	 */
	private HexPoint twoChainsBroken()
	{
		for (IndivNode node : indivBoard.getPoints())
		{
			if (node.getOccupied() == Player.ME)
			{
				List<HexPoint> chains = node.getTwoChains();// indivBoard);
				for (HexPoint pnt : chains)
				{
					if (indivBoard.getNode(pnt).getOccupied() == Player.ME)
					{
						List<HexPoint> connections = pnt.connections(node.getPoints().get(0));

						if (connections.size() < 2)
							throw new RuntimeException("Size less than 2!");

						HexPoint a = connections.get(0);
						HexPoint b = connections.get(1);

						if (difficulty > 8 && checkConnected(node.getPoints().get(0), pnt)) // skip if we are connected anyway in a triangle and need to fix our connection
							continue;

						// if either connector is broken, then cling onto the other
						if (indivBoard.getNode(a).getOccupied() == Player.YOU && indivBoard.getNode(b).getOccupied() == Player.EMPTY)
							return b;
						else if (indivBoard.getNode(b).getOccupied() == Player.YOU && indivBoard.getNode(a).getOccupied() == Player.EMPTY)
							return a;
					}
				}
			}
		}

		return null; // nothing broken
	}

	// check if these 2 are connected by 2 chains in a triangle fashion
	// assumes a and b are same color
	private boolean checkConnected(HexPoint a, HexPoint b)
	{
		for (HexPoint p : indivBoard.getNode(a).getTwoChains())
		{
			for (HexPoint k : indivBoard.getNode(b).getTwoChains())
			{
				if (k.equals(p) && !broken(k, a) && !broken(k, b) && !k.equals(a) && !k.equals(b) && indivBoard.getNode(k).getOccupied() == indivBoard.getNode(a).getOccupied())
				{
					return true;
				}
			}
		}

		return false;
	}

}
