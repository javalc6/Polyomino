package solver;

import java.util.*;
/**
 * Polyominoes Solver based on recursion, pruning impossible holes early
 */
public class PolyominoSolver extends AbstractPolyominoSolver {

	public static int[][] solve(int[][] board, List<boolean[][]> polyominoTypes) {
		int n_filled = 0;
		for (int[] row: board) 
			for (int cell: row) 
				if (cell > 0) n_filled++;

		final List<boolean[][]> allOrientations = new ArrayList<>();
        for (int i = 0; i < polyominoTypes.size(); i++)
			allOrientations.addAll(getUniqueOrientations(polyominoTypes.get(i)));

		// Find the smallest piece size to prune impossible holes early
		int minPieceSize = Integer.MAX_VALUE;
		for (boolean[][] shape: allOrientations) {
			int size = 0;
			for (boolean[] r: shape) 
				for (boolean c: r) 
					if (c) size++;
			minPieceSize = Math.min(minPieceSize, size);
		}
		return solve(board, board[0].length, board.length, 0, 0, n_filled, 1, allOrientations, minPieceSize) ? board : null;
	}

	private static boolean solve(int[][] board, int cols, int rows, int ii, int jj, int n_filled, int ord, List<boolean[][]> allOrientations, int minPieceSize) {
		if (!canFillHole(board, cols, rows, minPieceSize)) return false;
		int j = jj;
		for (int i = ii; i < rows; i++) {
			for (; j < cols; j++) {
				if (board[i][j] != 0) continue;

				for (boolean[][] shape: allOrientations) {
					if (j + shape[0].length <= cols && i + shape.length <= rows) {
						if (!shape[0][0]) continue; // Pruning: avoids leaving holes behind

						if (isAreaFree(board, shape, i, j)) {
							placePiece(board, shape, i, j, ord);//take area
							int new_n_filled = n_filled + getPieceSize(shape);
							if ((new_n_filled == cols * rows) || 
								solve(board, cols, rows, i, j + 1, new_n_filled, ord + 1, allOrientations, minPieceSize))
								return true;
							placePiece(board, shape, i, j, 0);//backtrack
						}
					}
				}
				return false; 
			}
			j = 0;
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

	private static boolean isAreaFree(int[][] board, boolean[][] shape, int r, int c) {
		for (int dr = 0; dr < shape.length; dr++) {
			for (int dc = 0; dc < shape[0].length; dc++)
				if (shape[dr][dc] && board[r + dr][c + dc] > 0) 
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