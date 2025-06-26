import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Random;

public class SudokuGUI extends JFrame {
    private final JTextField[][] cells = new JTextField[9][9];
    private final int[][] board = new int[9][9];
    private boolean darkTheme = false;

    private Timer userTimer;
    private int elapsedSeconds = 0;
    private final JLabel timerLabel = new JLabel("Time: 0s");

    private final JSlider speedSlider = new JSlider(0, 1000, 50);

    public SudokuGUI() {
        setTitle("Sudoku Solver");
        setSize(650, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel gridPanel = new JPanel(new GridLayout(9, 9));
        Font font = new Font("Arial", Font.BOLD, 20);

        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                cells[i][j] = new JTextField();
                cells[i][j].setHorizontalAlignment(JTextField.CENTER);
                cells[i][j].setFont(font);
                gridPanel.add(cells[i][j]);
            }
        }

        JButton solveButton = new JButton("Solve");
        JButton clearButton = new JButton("Clear");
        JButton generateButton = new JButton("Random Puzzle");
        JButton themeButton = new JButton("Toggle Theme");
        JButton startTimerButton = new JButton("Start Timer");

        solveButton.addActionListener(e -> {
            readBoardFromUI();
            if (!isValidInputBoard(board)) {
                JOptionPane.showMessageDialog(this, "❌ Invalid Sudoku input!");
                return;
            }

            if (userTimer != null && userTimer.isRunning()) {
                userTimer.stop();
                JOptionPane.showMessageDialog(this, "⏱ You took " + elapsedSeconds + " seconds to try solving!");
            }

            new Thread(() -> {
                long startTime = System.currentTimeMillis();
                if (solveSudokuWithDelay(board)) {
                    long endTime = System.currentTimeMillis();
                    updateUIWithSolution();
                    JOptionPane.showMessageDialog(this, "✅ Sudoku Solved in " + (endTime - startTime) + " ms!");
                } else {
                    JOptionPane.showMessageDialog(this, "⚠️ No solution exists.");
                }
            }).start();
        });

        clearButton.addActionListener(e -> clearBoardUI());

        generateButton.addActionListener(e -> {
            generateNewPuzzle();
        });

        themeButton.addActionListener(e -> {
            darkTheme = !darkTheme;
            applyTheme();
        });

        startTimerButton.addActionListener(e -> startUserTimer());

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(solveButton);
        buttonPanel.add(clearButton);
        buttonPanel.add(generateButton);
        buttonPanel.add(themeButton);
        buttonPanel.add(startTimerButton);
        buttonPanel.add(timerLabel);

        JPanel speedPanel = new JPanel();
        JLabel fastLabel = new JLabel("FAST!");
        fastLabel.setForeground(Color.GREEN.darker());
        JLabel slowLabel = new JLabel("SLOW!");
        slowLabel.setForeground(Color.RED.darker());
        speedPanel.add(fastLabel);
        speedPanel.add(speedSlider);
        speedPanel.add(slowLabel);

        add(gridPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.NORTH);
        add(speedPanel, BorderLayout.SOUTH);
        setVisible(true);
    }

    private void startUserTimer() {
        if (userTimer != null && userTimer.isRunning()) return;

        elapsedSeconds = 0;
        timerLabel.setText("Time: 0s");

        userTimer = new Timer(1000, e -> {
            elapsedSeconds++;
            timerLabel.setText("Time: " + elapsedSeconds + "s");
        });
        userTimer.start();
    }

    private void applyTheme() {
        Color bg = darkTheme ? Color.DARK_GRAY : Color.WHITE;
        Color fg = darkTheme ? Color.WHITE : Color.BLACK;
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                cells[i][j].setBackground(bg);
                cells[i][j].setForeground(fg);
                cells[i][j].setCaretColor(fg);
            }
        }
        getContentPane().setBackground(bg);
    }

    private void readBoardFromUI() {
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                String text = cells[i][j].getText().trim();
                board[i][j] = (text.isEmpty()) ? 0 : Integer.parseInt(text);
            }
        }
    }

    private void updateUIWithSolution() {
        for (int i = 0; i < 9; i++)
            for (int j = 0; j < 9; j++)
                cells[i][j].setText(Integer.toString(board[i][j]));
    }

    private void clearBoardUI() {
        for (int i = 0; i < 9; i++)
            for (int j = 0; j < 9; j++) {
                cells[i][j].setText("");
                board[i][j] = 0;
            }
        timerLabel.setText("Time: 0s");
        if (userTimer != null) userTimer.stop();
        elapsedSeconds = 0;
    }

    private void updateUIWithPuzzle() {
        for (int i = 0; i < 9; i++)
            for (int j = 0; j < 9; j++) {
                cells[i][j].setText(board[i][j] == 0 ? "" : Integer.toString(board[i][j]));
            }
    }

    private boolean isSafe(int[][] b, int row, int col, int num) {
        for (int i = 0; i < 9; i++)
            if (b[row][i] == num || b[i][col] == num)
                return false;

        int boxRow = row - row % 3, boxCol = col - col % 3;
        for (int i = boxRow; i < boxRow + 3; i++)
            for (int j = boxCol; j < boxCol + 3; j++)
                if (b[i][j] == num)
                    return false;

        return true;
    }

    private boolean isValidInputBoard(int[][] b) {
        for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 9; col++) {
                int num = b[row][col];
                if (num != 0) {
                    b[row][col] = 0;
                    if (!isSafe(b, row, col, num)) {
                        b[row][col] = num;
                        return false;
                    }
                    b[row][col] = num;
                }
            }
        }
        return true;
    }

    private boolean solveSudokuWithDelay(int[][] b) {
        for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 9; col++) {
                if (b[row][col] == 0) {
                    for (int num = 1; num <= 9; num++) {
                        if (isSafe(b, row, col, num)) {
                            b[row][col] = num;
                            final int r = row, c = col, n = num;
                            SwingUtilities.invokeLater(() -> cells[r][c].setText(Integer.toString(n)));
                            try {
                                Thread.sleep(speedSlider.getValue());
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            if (solveSudokuWithDelay(b)) return true;
                            b[row][col] = 0;
                            SwingUtilities.invokeLater(() -> cells[r][c].setText(""));
                        }
                    }
                    return false;
                }
            }
        }
        return true;
    }

    private boolean solveSudokuNoDelay(int[][] b) {
        for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 9; col++) {
                if (b[row][col] == 0) {
                    for (int num = 1; num <= 9; num++) {
                        if (isSafe(b, row, col, num)) {
                            b[row][col] = num;
                            if (solveSudokuNoDelay(b)) return true;
                            b[row][col] = 0;
                        }
                    }
                    return false;
                }
            }
        }
        return true;
    }

    private void generateNewPuzzle() {
        clearBoardUI();
        solveSudokuNoDelay(board); // fills board with complete solution
        removeCells(50); // removes 50 random cells to create a puzzle
        updateUIWithPuzzle();
    }

    private void removeCells(int count) {
        Random rand = new Random();
        while (count > 0) {
            int i = rand.nextInt(9);
            int j = rand.nextInt(9);
            if (board[i][j] != 0) {
                board[i][j] = 0;
                count--;
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(SudokuGUI::new);
    }
}
