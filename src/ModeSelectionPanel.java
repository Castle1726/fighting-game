import javax.swing.JPanel;
import javax.swing.JButton;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.Font;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Image;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

public class ModeSelectionPanel extends JPanel {
    private GameFrame frame;
    private Image modeBgImage; // ADDED: Optional background image

    public ModeSelectionPanel(GameFrame frame) {
        this.frame = frame;
        setPreferredSize(new Dimension(GamePanel.SCREEN_WIDTH, GamePanel.SCREEN_HEIGHT));
        setLayout(null); // Absolute positioning

        // ADDED: Load optional background image (customize path as needed; falls back to blue)
        loadModeBackground();

        // Vs CPU button (stub: starts two-player; extensible for AI)
        JButton vsCpuButton = new JButton("Vs CPU");
        vsCpuButton.setBounds(400, 250, 200, 50);
        vsCpuButton.setFont(new Font("Arial", Font.BOLD, 24));
        vsCpuButton.setToolTipText("Play against AI opponent (stubbed as 2P)");
        vsCpuButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //   Show difficulty selection for CPU mode (from local)
                frame.showDifficultySelection();
            }
        });
        add(vsCpuButton);

        // Vs 2 Players button
        JButton vs2PButton = new JButton("Vs 2 Players");
        vs2PButton.setBounds(400, 320, 200, 50);
        vs2PButton.setFont(new Font("Arial", Font.BOLD, 24));
        vs2PButton.setToolTipText("Local multiplayer with two human players");
        vs2PButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //   Set mode to TWO_PLAYER and start (no difficulty)
                frame.currentMode = GamePanel.GameMode.TWO_PLAYER;
                frame.startGame();
            }
        });
        add(vs2PButton);

        // Back button
        JButton backButton = new JButton("Back");
        backButton.setBounds(400, 390, 200, 50);
        backButton.setFont(new Font("Arial", Font.BOLD, 24));
        backButton.setToolTipText("Return to main menu");
        backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                frame.goToMenu();
            }
        });
        add(backButton);
    }

    // ADDED: Method to load optional background image
    private void loadModeBackground() {
        modeBgImage = Assets.loadImage("assets/images/background/arena 1.png"); // Customize path
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        // ADDED: Draw optional background image or fallback blue
        if (modeBgImage != null) {
            g2.drawImage(modeBgImage.getScaledInstance(getWidth(), getHeight(), Image.SCALE_SMOOTH), 0, 0, null);
        } else {
            g2.setColor(Color.BLUE);
            g2.fillRect(0, 0, getWidth(), getHeight());
        }
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 48));
        g2.drawString("Select Mode", 350, 200);
    }
}