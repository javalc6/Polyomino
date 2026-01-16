# Introduction
**PolyominoApp** is a simple java Swing application designed to solve and visualize polyominoes tiling.\
[Polyominoes](https://en.wikipedia.org/wiki/Polyomino) are planar polyforms whose cells are squares.

An interesting problem related to polyominoes is the rectangle tiling, an exact cover problem that can be solved efficiently by Donald Knuth's [Dancing Links (DLX) algorithm](https://arxiv.org/abs/cs/0011047). The application can solve this problem either using DLX or ordinary backtracking (slower). 
Before invoking the solver, PolyominoApp performs a preliminary check to determine whether an exact cover could potentially exist. If the board area cannot be computed as n1\*size1+n2\*size2+... no solution exists. This check prevents wasting time on cases where a solution is clearly impossible.

# Running PolyominoApp
Just run [ant](https://ant.apache.org/) to build and run PolyominoApp application.

# Benchmarking different algorithms
Two algorithms are available, DLX implemented by *DLXPolyominoSolver* and recursion implemented by *PolyominoSolver* using try "harder" pieces before and prune impossible holes early heuristics.

Executing the command ``java -cp classes PolyominoApp -benchmark`` may provide the following output running on CPU AMD Ryzen 7 8845HS:

```
Benchmarking DLXPolyominoSolver.solve()
....................................
Average solver time: 1.7291736111111113 ms
Max solver time: 19.2862 ms
----------------------
Benchmarking PolyominoSolver.solve()
....................................
Average solver time: 3.5691394444444446 ms
Max solver time: 114.9226 ms
```

# Usage from other java apps
Call solve method with board defined as int[][] and list of polyominoes defined as *ArrayList* of shapes stored as *boolean*[][], call either *DLXPolyominoSolver.solve* or *PolyominoSolver.solve*. Both methods are static.
```java
final List<boolean[][]> selectedShapes = new ArrayList<>();
final Polyomino[] testPolyominoes = {T4, S4, F5, L5, N5, P5, T5, U5, V5, W5, X5, Y5, Z5};
for (Polyomino polyomino: testPolyominoes)
	selectedShapes.add(polyomino.shape);
int[][] board = new int[rows][cols];
board[0][0] = 1; //mark cell 0,0 with value 1
boolean solved = DLXPolyominoSolver.solve(board, selectedShapes);
```

# PolyominoApp features
On the left panel, users can:

♦ Set the board size effortlessly using spin controls for rows and columns. \
♦ Choose which polyomino pieces to include from an organized checklist, from small shapes to complex pentominoes. \
♦ Start solving instantly with a prominent "Solve Board" button.

The main workspace on the right showcases the solution in a large, colorful grid where each polyomino is displayed in a distinct color, making it easy to visually identify pieces at a glance.

# Screenshot
This is an example of solution after pushing "Solve Board" button:

![Screenshot](images/polyomino_solver.png)
