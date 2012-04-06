package solution.solvers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import solution.CurrentGame;
import solution.board.HexPoint;
import solution.board.Player;
import solution.board.implementations.indivBoard.IndivBoard;
import solution.board.implementations.indivBoard.IndivNode;
import solution.debug.DebugWindow;

/**
 * This controls all our solvers and generates the master weight table
 * 
 * @author Daniel Centore
 * @author Mike
 *
 */
public class SolverController
{
	private CurrentGame curr; // Current game
	private HexPoint initial = null; // our centerpiece
	private IndivBoard indivBoard;

	public SolverController(CurrentGame curr)
	{
		this.curr = curr;
		indivBoard = curr.getBoardController().getIndivBoard();
	}

	/**
	 * Chooses our next move
	 * @return
	 */
	public HexPoint getMove()
	{
		if (initial == null)
			throw new RuntimeException("Should have been set already...");

		// Fix chain if necessary
		HexPoint broken = twoChainsBroken();
		if (broken != null)
			return broken;

		broken = baseTwoChainsBroken();
		if (broken != null)
			return broken;

		// DebugWindow.println(across(true) + " " + across(false));

		// follow chain down board
		return followChain();

		// TODO: if we have chains across, start to fill them in

		// return new HexPoint(6, 'g');

	}

	/**
	 * True if we have 2 chains all the way across
	 * @return
	 */
	private boolean across(boolean left)
	{
		// if we have one in a B row on both sides then basically we do (TODO: CHECK THE WALL FOR BROKEN CONNECTIONS)

		boolean a = false;
		boolean b = false;

		for (IndivNode node : indivBoard.getPoints())
		{
			if (node.getOccupied() == Player.ME)
			{
				// || node.getY() == 'j')
				// || node.getX() == 10)
				if ((curr.getConnectRoute() == CurrentGame.CONNECT_LETTERS && (node.getY() == 'b' || node.getY() == 'a')) ||
						(curr.getConnectRoute() == CurrentGame.CONNECT_NUMBERS && (node.getX() == 2 || node.getX() == 1)))
				{
					a = true;		// TODO: put back to true. its just false for testing reasons.
				}

				if ((curr.getConnectRoute() == CurrentGame.CONNECT_LETTERS && (node.getY() == 'j' || node.getY() == 'k')) ||
						(curr.getConnectRoute() == CurrentGame.CONNECT_NUMBERS && (node.getX() == 10 || node.getX() == 11)))
				{
					b = true;
				}
			}
		}

		return (left ? a : b);

	}

	private HexPoint followChain()
	{
		List<HexPoint> possible = new ArrayList<HexPoint>();

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

		int left = Integer.MAX_VALUE;
		HexPoint bestLeft = null;

		int right = Integer.MAX_VALUE;
		HexPoint bestRight = null;
		
		if (!itr.hasNext())
			return null;

		do
		{
			HexPoint h = itr.next();

			if (curr.getConnectRoute() == CurrentGame.CONNECT_LETTERS)
			{
				if (h.getY() < 'f')
				{
					int dist = calculateDistance(h);
					if (dist < left)
					{
						bestLeft = h;
						left = dist;
					}
				}
				else
				{
					int dist = calculateDistance(h);
					if (dist < right)
					{
						bestRight = h;
						right = dist;
					}
				}
			}
			else
			{
				if (h.getX() < 6)
				{
					int dist = calculateDistance(h);
					if (dist < left)
					{
						bestLeft = h;
						left = dist;
					}
				}
				else
				{
					int dist = calculateDistance(h);
					if (dist < right)
					{
						bestRight = h;
						right = dist;
					}
				}
			}

		} while (itr.hasNext());

		if (across(true))
			return bestRight;
		else if (across(false))
			return bestLeft;

		if (left > right && bestLeft != null)
			return bestLeft;
		else
			return bestRight;

	}

	/**
	 * Figures out distance to wall
	 * @param pnt
	 * @return
	 */
	private int calculateDistance(HexPoint pnt)
	{
		int retn;
		if (curr.getConnectRoute() == CurrentGame.CONNECT_LETTERS)
		{

			int i = pnt.getY() - 'a';
			int j = 'k' - pnt.getY();

			retn = Math.min(i, j);
		}
		else
		{
			int i = pnt.getX() - 1;
			int j = 11 - pnt.getX();

			retn = Math.min(i, j);
		}

		// DebugWindow.println(pnt.toString() + " " + countPaths(pnt));

		// magic numbers which makes it focus more on the side being raped
		int count = countPaths(pnt);
		// count /= 10;

		retn *= 100;
		if (count > 5)
			count = 5;
		if (count <= 0)
			count = 1;

		retn /= count;

		return retn;

	}

	/**
	 * Counts the number of 2-bridge paths that lead from this point to the wall
	 * @param pt
	 * @return
	 */
	private int countPaths(HexPoint pt)
	{

		int k = 1; // 1 for myself

		if (curr.getConnectRoute() == CurrentGame.CONNECT_LETTERS)
		{
			List<HexPoint> bridges = indivBoard.getNode(pt).getTwoChains();

			for (HexPoint p : bridges)
			{
				if (pt.getY() < 'e')
				{
					if (p.getY() < pt.getY()) // only count down
						k += countPaths(p);
				}
				else if (pt.getY() > 'g')
				{
					if (p.getY() > pt.getY()) // only count up
						k += countPaths(p);
				}
			}
		}
		else
		{
			List<HexPoint> bridges = indivBoard.getNode(pt).getTwoChains();

			for (HexPoint p : bridges)
			{
				if (pt.getX() < 5)
				{
					if (p.getX() < pt.getX()) // only count down
						k += countPaths(p);
				}
				else if (pt.getX() > 7)
				{
					if (p.getX() > pt.getX()) // only count up
						k += countPaths(p);
				}
			}
		}

		return k;

	}

	/**
	 * Checks if two-chains to the walls are broken
	 * @return
	 */
	private HexPoint baseTwoChainsBroken()
	{
		for (IndivNode node : indivBoard.getPoints())
		{
			if (node.getOccupied() == Player.ME)
			{
				HexPoint pt = node.getPoints().get(0);

				// TODO: skip over the orner cases (B11, J1, k2, and A10) (o/w we crash!)
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

						DebugWindow.println("AGHHH: " + pt.toString());

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

						DebugWindow.println("AGHHH: " + pt.toString());
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
	 * Checks if a two chain has been broken. If so, fixes it.
	 * @return
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

	public HexPoint getInitial()
	{
		return initial;
	}

	public void setInitial(HexPoint initial)
	{
		this.initial = initial;
	}

}
