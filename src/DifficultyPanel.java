import javax.swing.JPanel;
import javax.imageio.ImageIO;
import javax.swing.JButton;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.Font;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.awt.Image;




public class DifficultyPanel extends JPanel {
    private GameFrame frame;
    private Image difficultyBgImage;

    //   New panel for difficulty selection (from local; shown on "Vs CPU")
    public DifficultyPanel(GameFrame frame) {
        this.frame = frame;
        setPreferredSize(new Dimension(GamePanel.SCREEN_WIDTH, GamePanel.SCREEN_HEIGHT));
        setLayout(null); // Absolute positioning

        loadMenuBackground();

        //   Buttons for each difficulty, calling startGame with SINGLE_PLAYER and diff
        addDifficultyButton("Easy", GamePanel.Difficulty.EASY, 250);
        addDifficultyButton("Medium", GamePanel.Difficulty.MEDIUM, 320);
        addDifficultyButton("Hard", GamePanel.Difficulty.HARD, 390);

        // Back button
        JButton backButton = new JButton("Back");
        backButton.setBounds(400, 460, 200, 50);
        backButton.setFont(new Font("Arial", Font.BOLD, 24));
        backButton.setToolTipText("Return to mode selection");
        backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                frame.showModeSelection();
            }
        });
        add(backButton);
    }

    //   Helper to add difficulty buttons (adapted to team; calls frame.startGame with mode/diff)
    private void addDifficultyButton(String label, GamePanel.Difficulty diff, int yPos) {
        JButton button = new JButton("Play " + label);
        button.setBounds(400, yPos, 200, 50);
        button.setFont(new Font("Arial", Font.BOLD, 24));
        button.setToolTipText("Start game against AI on " + label + " difficulty");
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                frame.startGame(GamePanel.GameMode.SINGLE_PLAYER, diff);
            }
        });
        add(button);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        // draw background image if available, otherwise fallback to blue
        if (difficultyBgImage != null) {
            g2.drawImage(difficultyBgImage.getScaledInstance(getWidth(), getHeight(), Image.SCALE_SMOOTH), 0, 0, null);
        } else {
            g2.setColor(Color.BLUE);
            g2.fillRect(0, 0, getWidth(), getHeight());
        }
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 48));
        g2.drawString("Select Difficulty", 300, 200);
    }

        private void loadMenuBackground() {
        difficultyBgImage = Assets.loadImage("assets/images/background/arena 1.png"); // Customize path
    }

}