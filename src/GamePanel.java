import javax.imageio.ImageIO;
import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.awt.Font;
import java.awt.Color;


public class GamePanel extends JPanel {
    public static final int SCREEN_WIDTH = 1000;
    public static final int SCREEN_HEIGHT = 600;
    private final int FPS = 60;
    private Image bgImage;
    private Fighter fighter1;
    private Fighter fighter2;
    private Timer gameTimer;
    private boolean gameOver = false;
    private String winner = "";
    private int timeLeft = 10; // seconds
    private Timer roundTimer;

    // input state (Player 1)
    boolean left, right, up, attackR, attackT;

    // input state (PLAYER 2) Added
    boolean p2Left, p2Right, p2Up, p2Attack1, p2Attack2;


    public GamePanel() {
        setPreferredSize(new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT));
        setFocusable(true);

        loadAssets();
        initGameObjects();
        initInput();
        roundTimer = new Timer(1000, e -> {
        if (!gameOver) {
        timeLeft--;

        if (timeLeft == 0) {
            timeLeft = 0;
            gameOver = true;
            roundTimer.stop();
            determineWinner();
        }
    }
});      
roundTimer.start(); 
    }

    private void loadAssets() {
        try {
            // load same path you used in python; update if needed
            bgImage = ImageIO.read(new File("assets/images/background/arena 1.png"));
        } catch (IOException e) {
            System.out.println("Background image not found: " + e.getMessage());
            bgImage = null;
        }
    }

    private void initGameObjects() {
        fighter1 = new Fighter(200, 400);
        // player 2 uses a separate sprite file (place player2.png at this path)
        // player2.png faces left by default, so pass `false` for spriteFacesRight
        fighter2 = new Fighter(700, 400, "assets/images/characters/player2.png", false);
    }

    private void initInput() {
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int k = e.getKeyCode();

                // PLAYER 1
                if (k == KeyEvent.VK_A) left = true;
                if (k == KeyEvent.VK_D) right = true;
                if (k == KeyEvent.VK_W) up = true;
                if (k == KeyEvent.VK_R) attackR = true;
                if (k == KeyEvent.VK_T) attackT = true;

                // PLAYER 2
                if (k == KeyEvent.VK_LEFT)  p2Left = true;
                if (k == KeyEvent.VK_RIGHT) p2Right = true;
                if (k == KeyEvent.VK_UP)    p2Up = true;
                if (k == KeyEvent.VK_NUMPAD1) p2Attack1 = true;
                if (k == KeyEvent.VK_NUMPAD2) p2Attack2 = true;
            }

            @Override
            public void keyReleased(KeyEvent e) {
                int k = e.getKeyCode();

                // PLAYER 1
                if (k == KeyEvent.VK_A) left = false;
                if (k == KeyEvent.VK_D) right = false;
                if (k == KeyEvent.VK_W) up = false;
                if (k == KeyEvent.VK_R) attackR = false;
                if (k == KeyEvent.VK_T) attackT = false;

                // PLAYER 2
                if (k == KeyEvent.VK_LEFT)  p2Left = false;
                if (k == KeyEvent.VK_RIGHT) p2Right = false;
                if (k == KeyEvent.VK_UP)    p2Up = false;
                if (k == KeyEvent.VK_NUMPAD1) p2Attack1 = false;
                if (k == KeyEvent.VK_NUMPAD2) p2Attack2 = false;
            }
        });
    }

    public void startGame() {
        int delay = 1000 / FPS;
        gameTimer = new Timer(delay, ev -> {
            update();
            repaint();
     
        });
        gameTimer.start();
    }

    private void update() {
        if (gameOver) return;
        // provide the same signature as your python move(screen_width, screen_height, surface, target)
        fighter1.move(SCREEN_WIDTH, SCREEN_HEIGHT, this, fighter2, left, right, up, attackR, attackT);
        // PLAYER 2 ADDED
        fighter2.move(SCREEN_WIDTH, SCREEN_HEIGHT, this, fighter1, p2Left, p2Right, p2Up, p2Attack1, p2Attack2);
        // fighter2 has no AI; keep it stationary but still able to be hit
        // If you want simple AI later, we can add it.

         // game winner text
        if(!gameOver) {
            if(fighter1.getHealth() <= 0) {
                gameOver = true;
                winner = "Player 2 Wins!";
            }
            if(fighter2.getHealth() <= 0) {
                gameOver = true;
                winner = "Player 1 Wins!";
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g;

        // draw background scaled
        if (bgImage != null) {
            g2.drawImage(bgImage.getScaledInstance(SCREEN_WIDTH, SCREEN_HEIGHT, Image.SCALE_SMOOTH), 0, 0, null);
        } else {
            // fallback background
            g2.clearRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);
        }

        // draw health bars
        drawHealthBar(g2, fighter1.getHealth(), 20, 20);
        drawHealthBar(g2, fighter2.getHealth(), 580, 20);

        // draw fighters
        fighter1.draw(g2);
        fighter2.draw(g2);

         // game winner text
        if (gameOver) {
            g2.setColor(Color.BLACK);
            g2.setFont(new java.awt.Font("Times New Roman", java.awt.Font.BOLD, 60));
            g2.drawString(winner, 300, 300);
        }

        // timer
        g2.setFont(new Font("Arial", Font.BOLD, 36));
        g2.setColor(Color.WHITE);

        String timeText = String.valueOf(timeLeft);

        // Center horizontally
        int textWidth = g2.getFontMetrics().stringWidth(timeText);
        int x = (getWidth() / 2) - (textWidth / 2);

        // Place vertically between health bars
        int y = 50;

        g2.drawString(timeText, x, y);        
    }

    private void drawHealthBar(Graphics2D g, int health, int x, int y) {
        int fullW = 400;
        float ratio = Math.max(0, Math.min(1f, health / 100f));

        // white border
        g.setColor(java.awt.Color.WHITE);
        g.fillRect(x - 2, y - 2, fullW + 4, 34);

        // red background
        g.setColor(java.awt.Color.RED);
        g.fillRect(x, y, fullW, 30);

        // yellow health
        g.setColor(java.awt.Color.YELLOW);
        g.fillRect(x, y, (int) (fullW * ratio), 30);
    }

    private void determineWinner(){
        if (fighter1.getHealth() < fighter2.getHealth()){
            gameOver = true;
            winner = "Player 2 wins!";
        }
        else if (fighter2.getHealth() < fighter1.getHealth()){
            gameOver = true;
            winner = "Player 1 wins!";
        }
        else {
            gameOver = true;
            winner = "Draw!";
        }

    }
}
