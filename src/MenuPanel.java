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

public class MenuPanel extends JPanel {
    private GameFrame frame;
    private Image menuBgImage; // ADDED: Optional background image

    public MenuPanel(GameFrame frame) {
        this.frame = frame;
        setPreferredSize(new Dimension(GamePanel.SCREEN_WIDTH, GamePanel.SCREEN_HEIGHT));
        setLayout(null); // Absolute positioning

        // ADDED: Load optional background image (customize path as needed; falls back to blue)
        loadMenuBackground();

        // Play button
        JButton playButton = new JButton("Play");
        playButton.setBounds(400, 250, 200, 50);
        playButton.setFont(new Font("Arial", Font.BOLD, 24));
        playButton.setToolTipText("Start the game and select mode");
        playButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                frame.showModeSelection();
            }
        });
        add(playButton);

        // Quit button
        JButton quitButton = new JButton("Quit");
        quitButton.setBounds(400, 320, 200, 50);
        quitButton.setFont(new Font("Arial", Font.BOLD, 24));
        quitButton.setToolTipText("Exit the game");
        quitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
        add(quitButton);
    }

    // ADDED: Method to load optional background image
    private void loadMenuBackground() {
        menuBgImage = Assets.loadImage("assets/images/background/arena 1.png"); // Customize path
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        // ADDED: Draw optional background image or fallback blue
        if (menuBgImage != null) {
            g2.drawImage(menuBgImage.getScaledInstance(getWidth(), getHeight(), Image.SCALE_SMOOTH), 0, 0, null);
        } else {
            g2.setColor(Color.BLUE);
            g2.fillRect(0, 0, getWidth(), getHeight());
        }
        // Title text
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 48));
        g2.drawString("2D Fighting Game", 250, 200);
        // Instructions (adapted to team controls)
        g2.setFont(new Font("Arial", Font.PLAIN, 18));
        g2.drawString("Controls \n Player1: A/D - Move, W - Jump, R/T - Attack", 200, 480);
        g2.drawString("Player2: J/L - Move, I - Jump, O/P - Attack", 200, 510);
    }
}