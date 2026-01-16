package solver;

import java.util.*;
/**
 * Polyominoes Solver based on recursion: try "harder" pieces before and prune impossible holes early
 */
public class PolyominoSolver extends AbstractPolyominoSolver {

	public static boolean solve(int[][] board, List<boolean[][]> polyominoTypes) {
		int n_filled = 0;
		int order = 1;
		for (int[] row: board) 
			for (int cell: row) {
				if (cell > 0) n_filled++;
				if (cell >= order) order = cell + 1;
			}

		final List<boolean[][]> allOrientations = new ArrayList<>();
		for (int i = 0; i < polyominoTypes.size(); i++) {
            List<boolean[][]> orientations = getUniqueOrientations(polyominoTypes.get(i));
            for (boolean[][] shape: orientations)
				if (!containsShape(allOrientations, shape))
					allOrientations.add(shape);
        }

		// Sort orientations to try "harder" pieces before
		allOrientations.sort((a, b) -> {
			// Check for piece with higher size
			int s1 = getPieceSize(a);
			int s2 = getPieceSize(b);
			if (s1 != s2) return s2 - s1;

			// Check for piece with higher bounding box area
			int area1 = a.length * a[0].length;
			int area2 = b.length * b[0].length;
			return area2 - area1;
		});

		// Find the smallest piece size to prune impossible holes early
		int minPieceSize = Integer.MAX_VALUE;
		for (boolean[][] shape: allOrientations) {
			int size = 0;
			for (boolean[] r: shape) 
				for (boolean c: r) 
					if (c) size++;
			minPieceSize = Math.min(minPieceSize, size);
		}
		return solve(board, board[0].length, board.length, n_filled, order, allOrientations, minPieceSize);
	}

	private static boolean solve(int[][] board, int cols, int rows, int n_filled, int ord, List<boolean[][]> allOrientations, int minPieceSize) {
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				if (board[i][j] == 0) {
					if (!canFillHole(board, cols, rows, minPieceSize)) return false;

					for (boolean[][] shape : allOrientations) {
						for (int dr = 0; dr < shape.length; dr++) {
							for (int dc = 0; dc < shape[0].length; dc++) {
								if (shape[dr][dc] && canPlace(board, cols, rows, shape, i - dr, j - dc)) {
									placePiece(board, shape, i - dr, j - dc, ord);//take area
									int new_n_filled = n_filled + getPieceSize(shape);
									if (new_n_filled == rows * cols || solve(board, cols, rows, new_n_filled, ord + 1, allOrientations, minPieceSize))
										return true;
									placePiece(board, shape, i - dr, j - dc, 0); //backtrack
								}
							}
						}
					}
					return false;
				}
			}
		}
		return false;
	}

	private static boolean canFillHole(int[][] board, int cols, int rows, int minSize) {
		boolean[][] visited = new boolean[rows][cols];

		for (int r = 0; r < rows; r++) {
			for (int c = 0; c < cols; c++) {
				if (board[r][c] == 0 && !visited[r][c]) {
					int holeSize = measureHoleSize(board, visited, r, c);
					if (holeSize < minSize) return false;
				}
			}
		}
		return true;
	}

	private static int measureHoleSize(int[][] board, boolean[][] visited, int r, int c) {
		if (board[r][c] != 0 || visited[r][c]) return 0;

		visited[r][c] = true;
		int count = 1;
		if (r > 0)
			count += measureHoleSize(board, visited, r - 1, c);
		if (r < board.length - 1)
			count += measureHoleSize(board, visited, r + 1, c);
		if (c > 0)
			count += measureHoleSize(board, visited, r, c - 1);
		if (c < board[0].length - 1)
			count += measureHoleSize(board, visited, r, c + 1);
		return count;
	}

	private static boolean canPlace(int[][] board, int cols, int rows, boolean[][] shape, int r, int c) {
		for (int dr = 0; dr < shape.length; dr++) {
			for (int dc = 0; dc < shape[0].length; dc++)
				if (shape[dr][dc] && (r + dr < 0 || r + dr >= rows || c + dc < 0 || c + dc >= cols || board[r + dr][c + dc] != 0))
					return false;
		}
		return true;
	}

	private static void placePiece(int[][] board, boolean[][] shape, int r, int c, int val) {
		for (int dr = 0; dr < shape.length; dr++)
			for (int dc = 0; dc < shape[0].length; dc++)
				if (shape[dr][dc]) 
					board[r + dr][c + dc] = val;
	}

	private static int getPieceSize(boolean[][] shape) {
		int count = 0;
		for (boolean[] row: shape) 
			for (boolean cell: row) 
				if (cell) count++;
		return count;
	}
}