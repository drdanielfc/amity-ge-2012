package solution.solvers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import solution.CurrentGame;
import solution.board.HexPoint;
import solution.board.Player;
import solution.board.implementations.dijkstraBoard.DijkstraNode;
import solution.board.implementations.indivBoard.IndivNode;

/**
 * Solver which has us push towards both sides using weighted chains of nodes
 * 
 * @author Daniel Centore
 * @author Mike DiBuduo
 *
 */
public class FollowChain
{
	private SolverController solverController;

	public FollowChain(SolverController solverController)
	{
		this.solverController = solverController;
	}

	/**
	 * Gets the next {@link HexPoint} in order to follow a two-chain across the board
	 * @param solverController The current {@link SolverController}
	 * @param lastMove The last move made by the opponent
	 * @return The next {@link HexPoint} to continue the chain
	 */
	protected HexPoint followChain(SolverController solverController, HexPoint lastMove)
	{
		List<HexPoint> possible = new ArrayList<HexPoint>();

		// Add all points which are two-chains from one of our own pieces
		for (IndivNode node : solverController.indivBoard.getPoints())
		{
			if (node.getOccupied() == Player.ME)
			{
				List<HexPoint> chains = node.getTwoChains();
				for (HexPoint pnt : chains)
				{
					if (solverController.indivBoard.getNode(pnt).getOccupied() == Player.EMPTY && IndivNode.empty(pnt.connections(node.getPoints().get(0)), solverController.indivBoard))
					{
						possible.add(pnt);
					}
				}

				List<HexPoint> touching = node.getPoints().get(0).touching();
				for (HexPoint pnt : touching)
				{
					if (solverController.indivBoard.getNode(pnt).getOccupied() == Player.EMPTY)
					{
						possible.add(pnt);
					}
				}
			}
		}

		Iterator<HexPoint> itr = possible.iterator();

		double left = Double.MAX_VALUE;
		double leftLast = -1;
		HexPoint bestLeft = null;

		double right = Double.MAX_VALUE;
		double rightLast = -1;
		HexPoint bestRight = null;

		if (!itr.hasNext())
			return null;

		// Find the best move for connecting to both the left and right walls
		do
		{
			HexPoint h = itr.next();

			double leftDist = calculateDistance(solverController, h, true);
			double rightDist = calculateDistance(solverController, h, false);

			if (leftDist < left)
			{
				bestLeft = h;
				leftLast = solverController.dijkstraBoard.findDistance(lastMove, h);
				left = leftDist;
			}

			if (rightDist < right)
			{
				bestRight = h;
				rightLast = solverController.dijkstraBoard.findDistance(lastMove, h);
				right = rightDist;
			}

		} while (itr.hasNext());

		// If we have completed a side already, just go for the other one
		if (solverController.mapTools.across(solverController, true) && bestRight != null)
			return bestRight;
		else if (solverController.mapTools.across(solverController, false) && bestLeft != null)
			return bestLeft;

		// Choose the piece which is *weaker* so that we strengthen the link with that wall
		left += within(bestLeft, 2);
		right += within(bestRight, 2);

		left /= leftLast;
		right /= rightLast;

		if (left > right && bestLeft != null)
			return bestLeft;
		else
			return bestRight;
	}

	/**
	 * Counts how many enemy hexes are in a certain radius from one of our points
	 * @param ours The {@link HexPoint} representing where we are
	 * @param within The radius of nodes to look around it
	 * @return The number of enemy hexes in the radius
	 */
	protected int within(HexPoint ours, int within)
	{
		int result = 0;
		for (IndivNode node : solverController.indivBoard.getPoints())
		{
			int x1 = node.getX();
			int y1 = node.getY() - CurrentGame.CHARACTER_SUBTRACT;

			int x2 = ours.getX();
			int y2 = ours.getY() - CurrentGame.CHARACTER_SUBTRACT;

			double dist = Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));

			if (dist <= within && node.getOccupied() == Player.YOU)
				result++;
		}

		return result;
	}

	/**
	 * Figures out how hard it would be to get to the closest wall
	 * @param solverController Our {@link SolverController}
	 * @param pnt The starting {@link HexPoint}
	 * @param left Are we on the left side?
	 * @return The difficulty (arbitrary scale)
	 */
	protected double calculateDistance(SolverController solverController, HexPoint pnt, boolean left)
	{

		DijkstraNode wall;

		if (solverController.curr.getConnectRoute() == CurrentGame.CONNECT_LETTERS)
		{
			if (left)
				wall = solverController.dijkstraBoard.getWallA();
			else
				wall = solverController.dijkstraBoard.getWallK();
		}
		else
		{
			if (left)
				wall = solverController.dijkstraBoard.getWallOne();
			else
				wall = solverController.dijkstraBoard.getWallEle();
		}

		double dist = solverController.dijkstraBoard.findDistance(pnt, wall);

		return dist;
	}

}
