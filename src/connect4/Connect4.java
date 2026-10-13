package connect4;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.net.URL;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;

public class Connect4
{
    // Main game board panel; it is rebuilt whenever a new game starts.
    JPanel gridPanel = new JPanel();

    // Board dimensions the player can select before starting a game.
    String[] layoutOptions = {"6x7", "8x9", "10x11"};
    JComboBox<String> selectGridLayout = new JComboBox<>(layoutOptions);

    // Displays the accumulated score for both players.
    JLabel scoreLabel;

    // Tracks whose turn it is and which colour is used for the current move.
    int turn = 0;
    String selectedColor;
    String defaultColor = "#e9f5db";

    // Each array records the positions occupied by one player.
    int[][] playerBlue;
    int[][] playerRed;

    // Scores are kept for the current application session and can be saved to disk.
    int blueScore = 0;
    int redScore = 0;

    // Creates the application window and adds the game controls and board.
    @SuppressWarnings("Convert2Lambda")
    public void window()
    {
        JFrame window = new JFrame("Connect Four");
        window.setSize(800, 800);
        window.setLocationRelativeTo(null);
        window.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        window.addWindowListener(new WindowAdapter()
        {
            @Override
            public void windowClosing(WindowEvent event)
            {
                closeWindow();
            }
        });

        // Loads the application icon from the connect4 package resources.
        URL iconURL = getClass().getResource("Connect4.png");
        ImageIcon icon = new ImageIcon(iconURL);
        window.setIconImage(icon.getImage());

        // Shows both players' scores above the board.
        scoreLabel = new JLabel("<html>Blue: " + blueScore + "<br/><html>" + "Red: " + redScore);
        scoreLabel.setFont(new Font(scoreLabel.getFont().getName(), scoreLabel.getFont().getStyle(), 15));

        // Applies the shared background colour to the board-size selector and controls.
        selectGridLayout.setBackground(Color.decode(defaultColor));

        JButton restartGame = new JButton("Restart");
        restartGame.setBackground(Color.decode(defaultColor));
        restartGame.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                // Rebuilds the board using the currently selected dimensions.
                grid(gridPanel);
            }
        });

        JButton saveScore = new JButton("Save Score");
        saveScore.setBackground(Color.decode(defaultColor));
        saveScore.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                try
                {
                    // Writes both players' current scores to the save file.
                    saveScore();
                }
                catch (IOException ex)
                {
                    Logger.getLogger(Connect4.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });

        JButton loadScore = new JButton("Load Score");
        loadScore.setBackground(Color.decode(defaultColor));
        loadScore.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                // Restores both scores from the save file and refreshes the label.
                loadScore();
            }
        });

        // Groups the score display and game controls in the window header.
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new FlowLayout());
        topPanel.setBorder(new EmptyBorder(5, 0, 0, 0));
        topPanel.add(scoreLabel);
        topPanel.add(Box.createVerticalStrut(10));
        topPanel.add(selectGridLayout);
        topPanel.add(Box.createVerticalStrut(10));
        topPanel.add(restartGame);
        topPanel.add(Box.createVerticalStrut(10));
        topPanel.add(saveScore);
        topPanel.add(Box.createVerticalStrut(10));
        topPanel.add(loadScore);

        window.add(topPanel, BorderLayout.NORTH);

        // Creates the initial board before displaying the window.
        grid(gridPanel);
        window.add(gridPanel, BorderLayout.CENTER);
        window.setVisible(true);
    }

    // Creates the selectable board and attaches a move handler to every cell.
    @SuppressWarnings("Convert2Lambda")
    public void grid(JPanel panel)
    {
        // Blue moves first because even turn values are assigned to Blue.
        turn = 2;

        int columns = 6;
        int rows = 7;

        // Maps the selected board-size option to its column and row counts.
        if (selectGridLayout.getSelectedItem().equals("6x7"))
        {
            columns = 6;
            rows = 7;
        }
        else if (selectGridLayout.getSelectedItem().equals("8x9"))
        {
            columns = 8;
            rows = 9;
        }
        else if (selectGridLayout.getSelectedItem().equals("10x11"))
        {
            columns = 10;
            rows = 11;
        }

        // Resets the occupancy arrays to match the newly selected board size.
        playerBlue = new int[columns][rows];
        playerRed = new int[columns][rows];

        panel.removeAll();
        panel.setLayout(new GridLayout(rows, columns, 3, 3));
        panel.setBorder(new EmptyBorder(5, 5, 5, 5));

        // Button coordinates are stored as row first, then column.
        JButton[][] buttons = new JButton[rows][columns];

        // Builds one clickable button for each cell in the selected board.
        for (int a = 0; a < rows; a++)
        {
            for (int b = 0; b < columns; b++)
            {
                final int tempRow = rows - 1;

                buttons[a][b] = new JButton();
                buttons[a][b].setBackground(Color.decode(defaultColor));
                buttons[a][b].setName("button_" + (b) + "_" + (a));

                buttons[a][b].addActionListener(new ActionListener()
                {
                    @Override
                    public void actionPerformed(ActionEvent e)
                    {
                        JButton button = (JButton) e.getSource();
                        String name = button.getName();
                        String[] parts = name.split("_");
                        int x = Integer.parseInt(parts[1]);

                        // Selects the colour belonging to the player whose turn it is.
                        if (turn % 2 == 1)
                        {
                            selectedColor = "#E96479";
                        }
                        else
                        {
                            selectedColor = "#62CDFF";
                        }

                        // Drops the piece into the lowest unoccupied cell in this column.
                        for (int y = tempRow; y >= 0; y--)
                        {
                            if (buttons[y][x].getBackground().equals(Color.decode(defaultColor)))
                            {
                                buttons[y][x].setBackground(Color.decode(selectedColor));

                                // Records the move and checks whether the current player won.
                                if (turn % 2 == 0)
                                {
                                    playerBlue[x][y] = 1;
                                    if (checkWin(x, y, playerBlue))
                                    {
                                        blueScore += 1;
                                        updateScoreLabel();
                                        winWindow(turn);
                                    }
                                }
                                else
                                {
                                    playerRed[x][y] = 1;
                                    if (checkWin(x, y, playerRed))
                                    {
                                        redScore += 1;
                                        updateScoreLabel();
                                        winWindow(turn);
                                    }
                                }
                                turn++;
                                break;
                            }
                        }
                    }
                });
                panel.add(buttons[a][b]);
            }
        }

        // Refreshes the Swing panel after all cells have been recreated.
        panel.revalidate();
        panel.repaint();
    }

    // Checks the current player's board state for a four-piece winning line.
    @SuppressWarnings("empty-statement")
    public boolean checkWin(int x, int y, int[][] player)
    {
        // Checks the three cells above the most recent move for a vertical line.
        try
        {
            if (player[x][y + 1] == 1 && player[x][y + 2] == 1 && player[x][y + 3] == 1)
            {
                return true;
            }
        }
        catch (Exception e)
        {
            ;
        }

        // Scans each possible four-cell horizontal segment in the board.
        for (int columns = 0; columns <= player.length - 4; columns++)
        {
            if (player[columns][y] == 1
                    && player[columns + 1][y] == 1
                    && player[columns + 2][y] == 1
                    && player[columns + 3][y] == 1)
            {
                return true;
            }
        }

        // Checks diagonal lines running from the bottom left to the top right.
        for (int rows = 3; rows < player.length; rows++)
        {
            for (int columns = 0; columns <= player[0].length - 4; columns++)
            {
                if (player[rows][columns] == 1
                        && player[rows - 1][columns + 1] == 1
                        && player[rows - 2][columns + 2] == 1
                        && player[rows - 3][columns + 3] == 1)
                {
                    return true;
                }
            }
        }

        // Checks diagonal lines running from the top left to the bottom right.
        for (int rows = 0; rows <= player.length - 4; rows++)
        {
            for (int columns = 0; columns <= player[0].length - 4; columns++)
            {
                if (player[rows][columns] == 1
                        && player[rows + 1][columns + 1] == 1
                        && player[rows + 2][columns + 2] == 1
                        && player[rows + 3][columns + 3] == 1)
                {
                    return true;
                }
            }
        }
        return false;
    }

    // Prevents further moves after a player has won while leaving the final board visible.
    public void disableButtons()
    {
        for (Component button : gridPanel.getComponents())
        {
            if (button instanceof JButton)
            {
                button.setEnabled(false);
            }
        }
    }

    // Announces the winner and closes the temporary result window after three seconds.
    public void winWindow(int turn)
    {
        disableButtons();

        JFrame win = new JFrame("");
        win.setSize(180, 100);
        win.getContentPane().setBackground(Color.WHITE);
        win.getRootPane().setBorder(BorderFactory.createMatteBorder(4, 4, 4, 4, Color.BLACK));
        win.setLocationRelativeTo(null);
        win.setUndecorated(true);
        win.setAlwaysOnTop(true);

        String winner;

        // The turn value still identifies the player who made the winning move.
        if (turn % 2 == 0)
        {
            winner = "Blue";
        }
        else
        {
            winner = "Red";
        }

        JLabel winnerLabel = new JLabel(winner + " has won!");
        winnerLabel.setFont(new Font(winnerLabel.getFont().getName(), winnerLabel.getFont().getStyle(), 20));
        winnerLabel.setHorizontalAlignment(JLabel.CENTER);
        winnerLabel.setVerticalAlignment(JLabel.CENTER);
        win.add(winnerLabel);

        win.setVisible(true);

        new Timer(3_000, (e) ->
        {
            win.setVisible(false);
            win.dispose();
        }).start();
    }

    // Refreshes the score display after a win or after loading saved scores.
    public void updateScoreLabel()
    {
        scoreLabel.setText("<html>Blue: " + blueScore + "<br/><html>" + "Red: " + redScore);
    }

    // Saves the blue and red scores as separate lines in score.txt.
    @SuppressWarnings({"ConvertToTryWithResources", "UseSpecificCatch"})
    private void saveScore() throws IOException
    {
        File file = new File("score.txt");
        FileWriter fw = new FileWriter(file);
        PrintWriter pw = new PrintWriter(fw);
        pw.println(blueScore);
        pw.println(redScore);
        pw.close();
    }

    // Loads both saved scores and updates the score label.
    public void loadScore()
    {
        try
        {
            File file = new File("score.txt");
            Scanner input = new Scanner(file);
            blueScore = Integer.parseInt(input.nextLine());
            redScore = Integer.parseInt(input.nextLine());
            input.close();

            updateScoreLabel();
        }
        catch (FileNotFoundException ex)
        {
            Logger.getLogger(Connect4.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    // Saves the current scores before exiting when the user closes the main window.
    public void closeWindow()
    {
        try
        {
            saveScore();
        }
        catch (IOException ex)
        {
            Logger.getLogger(Connect4.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.exit(0);
    }

    // Launches the Connect Four application.
    public static void main(String[] args)
    {
        Connect4 connect4 = new Connect4();
        connect4.window();
    }
}
