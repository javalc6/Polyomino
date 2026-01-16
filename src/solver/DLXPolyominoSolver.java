package solver;

import java.util.*;
/**
 * Polyominoes Solver based on Donald Knuth's Dancing Links (DLX) algorithm
 */
public class DLXPolyominoSolver extends AbstractPolyominoSolver {
    public static boolean solve(int[][] board, List<boolean[][]> polyominoTypes) {
		int order = 1;
		for (int[] row: board) 
			for (int cell: row) {
				if (cell >= order) order = cell + 1;
			}

		int rows = board.length, cols = board[0].length;
        int totalCells = rows * cols;
        final List<Placement> allOrientations = new ArrayList<>();

        for (int i = 0; i < polyominoTypes.size(); i++) {
            List<boolean[][]> orientations = getUniqueOrientations(polyominoTypes.get(i));
            for (boolean[][] shape: orientations) {
                for (int r = 0; r <= rows - shape.length; r++) {
                    for (int c = 0; c <= cols - shape[0].length; c++) {
                        List<Integer> cells = new ArrayList<>();
                        boolean isValid = true;                        
                        for (int dr = 0; dr < shape.length; dr++) {
                            for (int dc = 0; dc < shape[0].length; dc++) {
                                if (shape[dr][dc]) {
                                    if (board[r + dr][c + dc] != 0) {
                                        isValid = false;
                                        break;
                                    }
                                    cells.add((r + dr) * cols + (c + dc));
                                }
                            }
                            if (!isValid) break;
                        }                       
                        if (isValid)
                            allOrientations.add(new Placement(cells));
                    }
                }
            }
        }

        DLX solver = new DLX(totalCells);
        for (int r = 0; r < rows; r++)
            for (int c = 0; c < cols; c++)
                if (board[r][c] != 0)
                    solver.coverColumn(r * cols + c);
        for (int i = 0; i < allOrientations.size(); i++)
            solver.addRow(i, allOrientations.get(i).cells);
        final List<Integer> solution = solver.solve();
        if (solution == null) return false;

        for (int idx: solution) {
            for (int cell: allOrientations.get(idx).cells)
                board[cell / cols][cell % cols] = order;
            order++;
        }
        return true;
    }

    private static class Placement {
        final List<Integer> cells;
        Placement(List<Integer> c) { 
			this.cells = c; 
		}
    }

    static class DLX {
        class Node { Node L, R, U, D; ColumnNode C; int r; Node() { L = R = U = D = this; } }
        class ColumnNode extends Node { int s, i; ColumnNode(int idx) { super(); i = idx; s = 0; C = this; } }
        
        private final ColumnNode root = new ColumnNode(-1);
        private final ColumnNode[] columnNodes;
        private List<Integer> solution;

        DLX(int n) {
            columnNodes = new ColumnNode[n];
            ColumnNode last = root;
            for (int i = 0; i < n; i++) {
                ColumnNode c = new ColumnNode(i);
                c.L = last; c.R = root; last.R = c; root.L = c;
                columnNodes[i] = c; last = c;
            }
        }

        void coverColumn(int index) { cover(columnNodes[index]); }

        void addRow(int rIdx, List<Integer> cells) {
            Node first = null;
            for (int ci: cells) {
                ColumnNode c = columnNodes[ci];
                if (c.L.R != c) continue; 

                Node n = new Node(); n.r = rIdx; n.C = c; n.U = c.U; n.D = c;
                c.U.D = n; c.U = n; c.s++;
                if (first == null) first = n;
                else { n.L = first.L; n.R = first; first.L.R = n; first.L = n; }
            }
        }

        List<Integer> solve() { solution = new ArrayList<>(); return search() ? solution : null; }

        private boolean search() {
            if (Thread.currentThread().isInterrupted()) return false;

            if (root.R == root) return true;

            ColumnNode c = (ColumnNode) root.R;
            for (ColumnNode t = (ColumnNode) root.R; t != root; t = (ColumnNode) t.R)
                if (t.s < c.s) c = t;
            
            if (c.s == 0) return false;
            
            cover(c);
            for (Node r = c.D; r != c; r = r.D) {
                solution.add(r.r);
                for (Node j = r.R; j != r; j = j.R)
					cover(j.C);
                if (search()) return true;
                for (Node j = r.L; j != r; j = j.L) 
					uncover(j.C);
                solution.remove(solution.size() - 1);
            }
            uncover(c);
            return false;
        }

        private void cover(ColumnNode c) {
            c.R.L = c.L; c.L.R = c.R;
            for (Node i = c.D; i != c; i = i.D)
                for (Node j = i.R; j != i; j = j.R) { j.D.U = j.U; j.U.D = j.D; j.C.s--; }
        }

        private void uncover(ColumnNode c) {
            for (Node i = c.U; i != c; i = i.U)
                for (Node j = i.L; j != i; j = j.L) { j.C.s++; j.D.U = j; j.U.D = j; }
            c.R.L = c; c.L.R = c;
        }
    }
}