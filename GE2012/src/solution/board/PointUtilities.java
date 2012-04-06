package solution.board;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

/**
 * Has utilities for working with {@link HexPoint}s
 * 
 * @author Daniel Centore
 * @author Mike DiBuduo
 *
 */
public class PointUtilities
{
	
	/**
	 * Generates a {@link List} of {@link HexPoint}s which are directly surrounding a point
	 * @param of The {@link HexPoint} in the center
	 * @return The neighboring points
	 */
	public static List<HexPoint> getNeighbors(HexPoint of)
	{
		int x = of.getX();
		char y = of.getY();

		// These are possible surroundings (gotta check bounds)
		HexPoint[] potential = {
				new HexPoint(x, (char) (y - 1)),
				new HexPoint(x - 1, y),
				new HexPoint(x - 1, (char) (y + 1)),
				new HexPoint(x, (char) (y + 1)),
				new HexPoint(x + 1, y),
				new HexPoint(x + 1, (char) (y - 1)) };
		
		List<HexPoint> result = new ArrayList<HexPoint>();
		for (HexPoint p : potential)
		{
			// Make sure the point is on the board
			if (p.getX() >= 1 && p.getX() <= 11 && p.getY() >= 'a' && p.getY() <= 'k')
				result.add(p);
		}
		
		return result;
	}
	
	/**
	 * Checks if two {@link HexPoint}s are neighbors
	 * @param hp1 first {@link HexPOint}
	 * @param hp2 second {@link HexPoint}
	 * @return Whether they are neighbors
	 */
	public static boolean areNeighbors(HexPoint hp1, HexPoint hp2) {
		return getNeighbors(hp1).contains(hp2);
	}
}
