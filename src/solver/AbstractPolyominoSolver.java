package solver;

import java.util.*;
/**
 * Abstract Polyominoes Solver, contains utility methods
 */
abstract public class AbstractPolyominoSolver {

    public static List<boolean[][]> getUniqueOrientations(boolean[][] shape) {
        final List<boolean[][]> shapes = new ArrayList<>();
        boolean[][] temp = shape;
        for (int i = 0; i < 4; i++) {
			if (!containsShape(shapes, temp))
				shapes.add(temp);
			if (!containsShape(shapes, flipShape(temp)))
				shapes.add(temp);
            temp = rotateShape(temp);
        }
        return shapes;
    }

    public static boolean containsShape(List<boolean[][]> shapes, boolean[][] refShape) {
		for (boolean[][] shape: shapes)
			if (Arrays.deepEquals(shape, refShape))
				return true;
		return false;
    }

    public static boolean[][] rotateShape(boolean[][] shape) {
        int r = shape.length, c = shape[0].length;
        boolean[][] out = new boolean[c][r];
        for (int i = 0; i < r; i++) 
			for (int j = 0; j < c; j++) 
				out[j][r - 1 - i] = shape[i][j];
        return out;
    }

    public static boolean[][] flipShape(boolean[][] shape) {
        int r = shape.length, c = shape[0].length;
        boolean[][] out = new boolean[r][c];
        for (int i = 0; i < r; i++) 
			for (int j = 0; j < c; j++) 
				out[i][c - 1 - j] = shape[i][j];
        return out;
    }

}