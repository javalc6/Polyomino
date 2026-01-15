/*
License Information, 2025 Livio (javalc6)

Feel free to modify, re-use this software, please give appropriate
credit by referencing this Github repository.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at
    http://www.apache.org/licenses/LICENSE-2.0
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

IMPORTANT NOTICE
Note that this software is freeware and it is not designed, licensed or
intended for use in mission critical, life support and military purposes.
The use of this software is at the risk of the user. 

DO NOT USE THIS SOFTWARE IF YOU DON'T AGREE WITH STATED CONDITIONS.
*/

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;

import solver.DLXPolyominoSolver;
import solver.PolyominoSolver;

/**
 * Swing application to solve and visualize Polyomino tiling.
 *
 * usage: java -cp classes PolyominoApp [-benchmark]
 *
 * if option -benchmark is specified, headless benchmark is executed
 *
 * v1.0, 31-12-2025: PolyominoApp first release
 * v1.0.1, 14-01-2026: code refactoring and benchmark
 */
public class PolyominoApp extends JFrame {

	private final static int initial_n_rows = 6;
	private final static int initial_n_columns = 6;

    private int[][] board;
    private final JPanel boardContainer;
    private SolverWorker currentWorker;

    private static final Color[] FourColorsPalette = {
        new Color(255, 80, 80),
        new Color(255, 255, 100),
        new Color(80, 255, 80),
        new Color(80, 80, 255)
    };

    private JSpinner rowsSpinner;
    private JSpinner colsSpinner;
    private Map<String, JCheckBox> polyominoCheckboxes;

/*
	//domino
	final static Polyomino I2 = new Polyomino("I2", new boolean[][]{{true, true}});
	//tromino
	final static Polyomino I3 = new Polyomino("I3", new boolean[][]{{true, true, true}});
	final static Polyomino L3 = new Polyomino("L3", new boolean[][]{{true, true}, {true, false}});
*/
	//tetrominoes
	final static Polyomino I4 = new Polyomino("I4", new boolean[][]{{true, true, true, true}});
	final static Polyomino L4 = new Polyomino("L4", new boolean[][]{{true, true, true}, {true, false, false}});
	final static Polyomino O4 = new Polyomino("O4", new boolean[][]{{true, true}, {true, true}});
	final static Polyomino T4 = new Polyomino("T4", new boolean[][]{{true, true, true}, {false, true, false}});
	final static Polyomino S4 = new Polyomino("S4", new boolean[][]{{true, true, false}, {false, true, true}});
	//pentominoes
	final static Polyomino F5 = new Polyomino("F5", new boolean[][]{{false, true, true}, {true, true, false}, {false, true, false}});
	final static Polyomino I5 = new Polyomino("I5", new boolean[][]{{true, true, true, true, true}});
	final static Polyomino L5 = new Polyomino("L5", new boolean[][]{{true, true, true, true}, {true, false, false, false}});
	final static Polyomino N5 = new Polyomino("N5", new boolean[][]{{true, true, true, false}, {false, false, true, true}});
	final static Polyomino P5 = new Polyomino("P5", new boolean[][]{{true, true, true}, {false, true, true}});
	final static Polyomino T5 = new Polyomino("T5", new boolean[][]{{true, true, true}, {false, true, false}, {false, true, false}});
	final static Polyomino U5 = new Polyomino("U5", new boolean[][]{{true, true, true}, {true, false, true}});
	final static Polyomino V5 = new Polyomino("V5", new boolean[][]{{true, true, true}, {true, false, false}, {true, false, false}});
	final static Polyomino W5 = new Polyomino("W5", new boolean[][]{{true, true, false}, {false, true, true}, {false, false, true}});
	final static Polyomino X5 = new Polyomino("X5", new boolean[][]{{false, true, false}, {true, true, true}, {false, true, false}});
	final static Polyomino Y5 = new Polyomino("Y5", new boolean[][]{{true, true, true, true}, {false, true, false, false}});
	final static Polyomino Z5 = new Polyomino("Z5", new boolean[][]{{true, true, false}, {false, true, false}, {false, true, true}});

	final static Polyomino[] allPolyominoes = {I4, O4, T4, L4, S4, F5, I5, L5, N5, P5, T5, U5, V5, W5, X5, Y5, Z5};
	final static Set<Polyomino> disabled = Set.of(I4, L4, O4, I5);//boring polyominoes...

    public PolyominoApp() {
        setTitle("Polyomino Solver");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        add(sidebar(true), BorderLayout.WEST);

        boardContainer = new JPanel(new GridBagLayout());
        boardContainer.setBackground(Color.WHITE);
        add(new JScrollPane(boardContainer), BorderLayout.CENTER);

        setSize(900, 700);
        setLocationRelativeTo(null);
    }

	static class Polyomino {
		final String type;
		final boolean[][] shape;
		
		Polyomino(String type, boolean[][] shape) {
			this.type = type;
			this.shape = shape;
		}
	}

    private JPanel sidebar(boolean init) {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBorder(new EmptyBorder(15, 15, 15, 15));
        sidebar.setPreferredSize(new Dimension(250, 700));

        JLabel boardSizeLabel = new JLabel("Board Size:");
        boardSizeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(boardSizeLabel);

        JPanel sizePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        sizePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        rowsSpinner = new JSpinner(new SpinnerNumberModel(initial_n_rows, 1, 25, 1));
        colsSpinner = new JSpinner(new SpinnerNumberModel(initial_n_columns, 1, 25, 1));
        sizePanel.add(new JLabel("R:"));
        sizePanel.add(rowsSpinner);
        sizePanel.add(new JLabel("C:"));
        sizePanel.add(colsSpinner);
        sidebar.add(sizePanel);

        sidebar.add(Box.createVerticalStrut(20));

        JLabel selectLabel = new JLabel("Select Polyominoes:");
        selectLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(selectLabel);

        polyominoCheckboxes = new LinkedHashMap<>();
        for (Polyomino polyomino: allPolyominoes) {
            JCheckBox cb = new JCheckBox(polyomino.type + " Polyomino", !disabled.contains(polyomino));
            cb.setAlignmentX(Component.LEFT_ALIGNMENT);
            polyominoCheckboxes.put(polyomino.type, cb);
            sidebar.add(cb);
        }

        sidebar.add(Box.createVerticalGlue());

        JButton solveBtn = new JButton("Solve Board");
        solveBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        solveBtn.setMargin(new Insets(10, 20, 10, 20));
        solveBtn.addActionListener(e -> startSolving());
        sidebar.add(solveBtn);

		if (init) {
			currentWorker = new SolverWorker(initial_n_rows, initial_n_columns, getSelectedShapes(), null);
			currentWorker.execute();
		}
        return sidebar;
    }

	private List<boolean[][]> getSelectedShapes() {
        final List<boolean[][]> selectedShapes = new ArrayList<>();
        for (Polyomino polyomino: allPolyominoes)
			if (polyominoCheckboxes.get(polyomino.type).isSelected()) 
				selectedShapes.add(polyomino.shape);
		return selectedShapes;
	}

    /**
     * Entry point for the solver. Launches the background worker and the modal dialog.
     */
    private void startSolving() {
		if (currentWorker != null && !currentWorker.isDone())
			currentWorker.cancel(true);

        int r = (int) rowsSpinner.getValue();
        int c = (int) colsSpinner.getValue();
        
        List<boolean[][]> selectedShapes = getSelectedShapes();

        if (selectedShapes.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select at least one polyomino type.");
            return;
        }

        // Check solution feasibility: 
		// a necessary (but not sufficient) condition for the existence of an exact cover is that the board area can be computed as n1*size1 + n2*size2 + ...
        
        final Set<Integer> uniqueSizes = new HashSet<>();
        for (boolean[][] shape: selectedShapes)
            uniqueSizes.add(getPieceSize(shape));

		int totalArea = r * c;
        if (!summable(totalArea, uniqueSizes)) {
            JOptionPane.showMessageDialog(this, 
                "Given selected piece sizes " + uniqueSizes + " it is not possible to cover board area " + totalArea,
                "Impossible Configuration",
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Setup Progress Dialog
        final JDialog waitDialog = new JDialog(this, "Solving", true);
        waitDialog.setLayout(new FlowLayout());
        final JLabel label = new JLabel("Please wait, finding solution...");
        final JButton interruptBtn = new JButton("Interrupt");
        
        waitDialog.add(label);
        waitDialog.add(interruptBtn);
        waitDialog.setSize(250, 100);
        waitDialog.setLocationRelativeTo(this);
        waitDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

        // Background worker
        currentWorker = new SolverWorker(r, c, selectedShapes, waitDialog);
        
        interruptBtn.addActionListener(e -> {
            if (currentWorker != null) {
                currentWorker.cancel(true);
                waitDialog.dispose();
            }
        });

        currentWorker.execute();
        waitDialog.setVisible(true); // Modal blocks here until dialog is disposed
    }

    private static boolean summable(int targetSum, Set<Integer> Sizes) {
        final boolean[] dp = new boolean[targetSum + 1];
        dp[0] = true;
        for (int Sj: Sizes) {
            for (int i = Sj; i <= targetSum; i++)
                if (dp[i - Sj])
                    dp[i] = true;
            if (dp[targetSum]) return true;
        }
        return dp[targetSum];
    }

    private int getPieceSize(boolean[][] shape) {
        int count = 0;
        for (boolean[] row: shape)
            for (boolean cell: row)
                if (cell) count++;
        return count;
    }

    /**
     * Background solver
     */
    private class SolverWorker extends SwingWorker<int[][], Void> {
        private final int rows, cols;
        private final List<boolean[][]> shapes;
        private final JDialog dialog;

        public SolverWorker(int rows, int cols, List<boolean[][]> shapes, JDialog dialog) {
            this.rows = rows;
            this.cols = cols;
            this.shapes = shapes;
            this.dialog = dialog;
        }

        @Override
        protected int[][] doInBackground() {
//            return PolyominoSolver.solve(new int[rows][cols], shapes);// slower
            return DLXPolyominoSolver.solve(new int[rows][cols], shapes);
        }

        @Override
        protected void done() {
            if (dialog != null && dialog.isVisible()) dialog.dispose();
            if (isCancelled()) return;

            try {
                int[][] result = get();
                if (result != null) {
                    board = result;
					boardContainer.removeAll();
                    Map<Integer, Color> idToColor = assignColors(board);
					boardContainer.add(new BoardPanel(board, idToColor));
					boardContainer.revalidate();
					boardContainer.repaint();
                } else {
                    JOptionPane.showMessageDialog(PolyominoApp.this, "No exact cover found for this configuration.");
                }
            } catch (Exception e) {
                if (!isCancelled()) {
                    JOptionPane.showMessageDialog(PolyominoApp.this, "Error during calculation: " + e.getMessage());
                }
            }
        }
    }

    private Map<Integer, Color> assignColors(int[][] board) {
        int rows = board.length;
        int cols = board[0].length;
        final Set<Integer> ids = new HashSet<>();
        for (int[] r: board) 
			for (int v: r) 
				if (v > 0) ids.add(v);

        final Map<Integer, Set<Integer>> adj = new HashMap<>();
        for (int id: ids) adj.put(id, new HashSet<>());

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                int id = board[r][c];
                if (id == 0) continue;
                int[][] neighbors = {{r + 1, c}, {r, c + 1}, {r - 1, c}, {r, c - 1}};
                for (int[] n: neighbors) {
                    if (n[0] >= 0 && n[0] < rows && n[1] >= 0 && n[1] < cols) {
                        int nid = board[n[0]][n[1]];
                        if (nid > 0 && nid != id) {
                            adj.get(id).add(nid);
                            adj.get(nid).add(id);
                        }
                    }
                }
            }
        }

        final Map<Integer, Color> colorMap = new HashMap<>();
        for (int id: ids) {
            boolean[] used = new boolean[FourColorsPalette.length];
            for (int neighbor: adj.get(id)) {
                if (colorMap.containsKey(neighbor)) {
                    Color nc = colorMap.get(neighbor);
                    for (int i = 0; i < FourColorsPalette.length; i++) 
						if (FourColorsPalette[i].equals(nc)) used[i] = true;
                }
            }
            for (int i = 0; i < FourColorsPalette.length; i++) {
                if (!used[i]) {
                    colorMap.put(id, FourColorsPalette[i]);
                    break;
                }
            }
            if (!colorMap.containsKey(id)) colorMap.put(id, Color.GRAY);
        }
        return colorMap;
    }

    private static class BoardPanel extends JPanel {
        private final int[][] board;
        private final Map<Integer, Color> idToColor;
        private static final int CELL_SIZE = 45;

        public BoardPanel(int[][] board, Map<Integer, Color> idToColor) {
            this.board = board;
            this.idToColor = idToColor;
            setPreferredSize(new Dimension(board[0].length * CELL_SIZE + 10, board.length * CELL_SIZE + 10));
            setBackground(Color.WHITE);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            for (int r = 0; r < board.length; r++) {
                for (int c = 0; c < board[0].length; c++) {
                    int id = board[r][c];
                    int x = c * CELL_SIZE;
                    int y = r * CELL_SIZE;

                    if (id > 0) {
                        g2.setColor(idToColor.get(id));
                        g2.fillRect(x, y, CELL_SIZE, CELL_SIZE);
                        g2.setColor(new Color(0, 0, 0, 40));
                        g2.drawRect(x, y, CELL_SIZE, CELL_SIZE);
                        
                        g2.setColor(new Color(0, 0, 0, 80));
                        g2.setFont(new Font("SansSerif", Font.PLAIN, 10));
                        g2.drawString(String.valueOf(id), x + 4, y + 12);
                    }
                }
            }

            g2.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.setColor(Color.BLACK);
            for (int r = 0; r < board.length; r++) {
                for (int c = 0; c < board[0].length; c++) {
                    int id = board[r][c];
                    if (c + 1 < board[0].length && board[r][c+1] != id) 
                        g2.drawLine((c+1)*CELL_SIZE, r*CELL_SIZE, (c+1)*CELL_SIZE, (r+1)*CELL_SIZE);
                    if (r + 1 < board.length && board[r+1][c] != id) 
                        g2.drawLine(c*CELL_SIZE, (r+1)*CELL_SIZE, (c+1)*CELL_SIZE, (r+1)*CELL_SIZE);
                }
            }
            g2.drawRect(0, 0, board[0].length * CELL_SIZE, board.length * CELL_SIZE);
        }
    }

	private static boolean dLXPolyominoSolver(int rows, int cols) {//benchmarking adapter for DLXPolyominoSolver
        final List<boolean[][]> selectedShapes = new ArrayList<>();
		final Polyomino[] testPolyominoes = {T4, S4, F5, L5, N5, P5, T5, U5, V5, W5, X5, Y5, Z5};
        for (Polyomino polyomino: testPolyominoes)
			selectedShapes.add(polyomino.shape);
		return DLXPolyominoSolver.solve(new int[rows][cols], selectedShapes) != null;
	}

	private static boolean polyominoSolver(int rows, int cols) {//benchmarking adapter for PolyominoSolver
        final List<boolean[][]> selectedShapes = new ArrayList<>();
		final Polyomino[] testPolyominoes = {T4, S4, F5, L5, N5, P5, T5, U5, V5, W5, X5, Y5, Z5};
        for (Polyomino polyomino: testPolyominoes)
			selectedShapes.add(polyomino.shape);
		return PolyominoSolver.solve(new int[rows][cols], selectedShapes) != null;
	}

	private final static int REPEAT_COUNT = 10;
	private static void doBenchmark(BiFunction<Integer, Integer, Boolean> solver) {
		long t0 = System.nanoTime(); long total = 0;
		long max = 0; int n_runs = 0;
		for (int k = 0; k < REPEAT_COUNT; k++) {
			for (int rows = 4; rows < 16; rows += 2) {
				for (int cols = 4; cols < 16; cols += 2) {
					if (solver.apply(rows, cols)) {
						n_runs++;
						long t = System.nanoTime();
						long delta = t - t0;
						t0 = t; total += delta;
						if (delta > max) {
							max = delta;
						}
						if (n_runs % REPEAT_COUNT == 0)
							System.out.print(".");
					}
				}
			}
		}
		System.out.println();
		System.out.println("Average solver time: " + total / 1e6 / n_runs + " ms");
		System.out.println("Max solver time: " + max / 1e6 + " ms");
	}

    public static void main(String[] args) {
		if (args.length > 0 && args[0].equals("-benchmark")) {
			System.out.println("Benchmarking DLXPolyominoSolver.solve()");
			doBenchmark(PolyominoApp::dLXPolyominoSolver);
			System.out.println("----------------------");
			System.out.println("Benchmarking PolyominoSolver.solve()");
			doBenchmark(PolyominoApp::polyominoSolver);
			System.out.println("----------------------");
		} else SwingUtilities.invokeLater(() -> new PolyominoApp().setVisible(true));
    }
}