package solution.board.implementations.indivBoard;

import java.util.ArrayList;
import java.util.List;

import solution.board.HexPoint;
import solution.board.NodeInterface;
import solution.board.Player;

/**
 * Represents an individual spot on the grid
 * 
 * @author Daniel Centore
 *
 */
public class IndivNode implements NodeInterface
{
	private int x;	// our 'x' location
	private char y;	// our 'y' location
	private Player occupied;	// who currently owns the space
	private List<HexPoint> points;
	
	/**
	 * Creates an individual spot which is unowned
	 * @param x The x location
	 * @param y The y location
	 */
	public IndivNode(int x, char y)
	{
		this(x, y, Player.EMPTY);
	}
	
	/**
	 * Creates an individual spot
	 * @param x The x location
	 * @param y The y location
	 * @param occupied Who owns the space
	 */
	public IndivNode(int x, char y, Player occupied)
	{
		this.x = x;
		this.y = y;
		this.occupied = occupied;
		
		points = new ArrayList<HexPoint>();
		points.add(new HexPoint(x, y));
	}
	
	@Override
	public List<HexPoint> getPoints()
	{
		return points;
	}

	/**
	 * @return The 'x' location of this spot
	 */
	public int getX()
	{
		return x;
	}

	/**
	 * @return The 'y' location of this spot
	 */
	public char getY()
	{
		return y;
	}

	@Override
	public Player getOccupied()
	{
		return occupied;
	}

	@Override
	public String toString()
	{
		return "IndivNode [x=" + x + ", y=" + y + ", occupied=" + occupied + "]";
	}

	/**
	 * Sets who owns this square
	 * @param occupied The {@link Player} who occupies this square
	 * @throws RuntimeException If we try to set it to {@link Player#EMPTY}
	 */
	public void setOccupied(Player occupied)
	{
		if (occupied == Player.EMPTY)
			throw new RuntimeException("We can\'t change to empty once the game has started...");
		
		this.occupied = occupied;
	}

}
