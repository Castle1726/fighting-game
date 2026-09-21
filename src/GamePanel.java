import javax.imageio.ImageIO;
import javax.swing.JPanel;
import javax.swing.Timer;
import javax.swing.JButton; // ADDED: For overlay buttons
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
import java.awt.event.ActionEvent; // ADDED: For button listeners
import java.awt.event.ActionListener; // ADDED: For button listeners


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
    private static final int INITIAL_ROUND_TIME = 60; // seconds
    private int timeLeft = INITIAL_ROUND_TIME;
    private Timer roundTimer;    

    // input state (Player 1)
    boolean left, right, up, attackR, attackT;

    // input state (PLAYER 2) Added
    boolean p2Left, p2Right, p2Up, p2Attack1, p2Attack2;
    // ADDED: Overlay button fields (no frame ref needed; uses external GameFrame methods if extended)
    private JButton retryButton;
    private JButton menuButton;

    // Enums moved inside class as static (removed public for compilation; qualified access e.g., GamePanel.GameMode)
    static enum GameMode {
        SINGLE_PLAYER,
        TWO_PLAYER
    }

    static enum Difficulty {
        EASY,
        MEDIUM,
        HARD
    }

    private Difficulty difficulty = Difficulty.MEDIUM;  // New field with default

    public GamePanel() {
        setPreferredSize(new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT));
        setFocusable(true);
        // ADDED: Enable absolute positioning for buttons
        setLayout(null);

        loadAssets();
        initGameObjects();
        initInput();
        // initialize and start the round timer
        resetRoundTimer();
    }

    // Added setter for difficulty (from local; called from frame on startGame)
    public void setDifficulty(Difficulty difficulty) {
        this.difficulty = difficulty;
    }

    private void loadAssets() {
        // Use Assets helper (classpath-first); simplifies running from JAR
        bgImage = Assets.loadImage("assets/images/background/arena 1.png");
        if (bgImage == null) {
            System.out.println("Background image not found: assets/images/background/arena 1.png");
        }
    }

    // Modified to instantiate AI for SINGLE_PLAYER mode (defaults to MEDIUM difficulty; uses frame's currentMode)
    private void initGameObjects() {
        fighter1 = new Fighter(200, 400);
        GameFrame frame = (GameFrame) getTopLevelAncestor();
        if (frame != null && frame.currentMode == GameMode.SINGLE_PLAYER) {
            // Use AIFighter for CPU opponent with custom sprite (assumes source faces left, like player2)
            fighter2 = new AIFighter(700, 400, "assets/images/characters/CPU.png", false, this.difficulty);
        } else {
            // Fallback to human Fighter for TWO_PLAYER
            fighter2 = new Fighter(700, 400, "assets/images/characters/player2.png", false);
        }
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

                // PLAYER 2 (updated controls: J left, L right, I jump, O attack1, P attack2)
                if (k == KeyEvent.VK_J)  p2Left = true;
                if (k == KeyEvent.VK_L) p2Right = true;
                if (k == KeyEvent.VK_I)    p2Up = true;
                if (k == KeyEvent.VK_O) p2Attack1 = true;
                if (k == KeyEvent.VK_P) p2Attack2 = true;
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

                // PLAYER 2 (updated controls: J left, L right, I jump, O attack1, P attack2)
                if (k == KeyEvent.VK_J)  p2Left = false;
                if (k == KeyEvent.VK_L) p2Right = false;
                if (k == KeyEvent.VK_I)    p2Up = false;
                if (k == KeyEvent.VK_O) p2Attack1 = false;
                if (k == KeyEvent.VK_P) p2Attack2 = false;
            }
        });
    }

    public void startGame() {
        // ADDED: Ensure focus for input
        requestFocusInWindow();
        int delay = 1000 / FPS;
        gameTimer = new Timer(delay, ev -> {
            update();
            repaint();
        });
        gameTimer.start();
    }

    // Modified update to pass dummy inputs for SINGLE_PLAYER (AI ignores them; ensures no human P2 controls in CPU mode)
    private void update() {
        if (gameOver) return;
        // provide the same signature as your python move(screen_width, screen_height, surface, target)
        fighter1.move(SCREEN_WIDTH, SCREEN_HEIGHT, this, fighter2, left, right, up, attackR, attackT);
        // PLAYER 2 ADDED
        GameFrame frame = (GameFrame) getTopLevelAncestor();
        boolean isSinglePlayer = (frame != null && frame.currentMode == GamePanel.GameMode.SINGLE_PLAYER);
        if (isSinglePlayer) {
            // Pass dummy false inputs for AI (overridden in AIFighter)
            fighter2.move(SCREEN_WIDTH, SCREEN_HEIGHT, this, fighter1, false, false, false, false, false);
        } else {
            // Use human inputs for TWO_PLAYER
            fighter2.move(SCREEN_WIDTH, SCREEN_HEIGHT, this, fighter1, p2Left, p2Right, p2Up, p2Attack1, p2Attack2);
        }
        // fighter2 has no AI; keep it stationary but still able to be hit
        // If you want simple AI later, we can add it.

         // game winner text
        if(!gameOver) {
            if(fighter1.getHealth() <= 0) {
                gameOver = true;
                winner = "Player 2 Wins!";
                // ADDED: Show overlay on game over
                showOverlay();
            }
            if(fighter2.getHealth() <= 0) {
                gameOver = true;
                winner = "Player 1 Wins!";
                // ADDED: Show overlay on game over
                showOverlay();
            }
        }
    }


// ADDED: Method to display interactive overlay on game over
    private void showOverlay() {
        if (retryButton != null) return; // Already shown

        retryButton = new JButton("Retry");
        retryButton.setBounds(SCREEN_WIDTH / 2 - 150, SCREEN_HEIGHT / 2 + 50, 100, 50);
        retryButton.setFont(new Font("Arial", Font.BOLD, 18));
        retryButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // restartGame();
                restartAndResume();
            }
        });
        add(retryButton);

        menuButton = new JButton("Menu");
        menuButton.setBounds(SCREEN_WIDTH / 2 + 50, SCREEN_HEIGHT / 2 + 50, 100, 50);
        menuButton.setFont(new Font("Arial", Font.BOLD, 18));
        menuButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // ADDED: External navigation via GameFrame (assumes frame access; extend if needed)
                ((GameFrame) getTopLevelAncestor()).goToMenu();
            }
        });
        add(menuButton);
        repaint();
    }

    // ADDED: Method to restart the game
    private void restartGame() {
        remove(retryButton);
        remove(menuButton);
        retryButton = null;
        menuButton = null;
        initGameObjects(); // Reset fighters
        gameOver = false;
        repaint();
    }
    //  Public method to fully reset game state (called on menu navigation or pre-start; clears win text/overlays/freezes)
    public void resetGameState() {
        stopGame(); // Halt timer if active
        if (retryButton != null) {
            remove(retryButton);
            retryButton = null;
        }
        if (menuButton != null) {
            remove(menuButton);
            menuButton = null;
        }
        initGameObjects(); // Reinitialize fighters (clears health/positions)
        gameOver = false; // Allow updates to resume
        winner = ""; // Clear win message
        repaint(); // Force visual refresh (hides persistent text/elements)
    }

    //  Private method for retry: Full reset + resume timer (ensures no latched inputs or stalled loop)
    private void restartAndResume() {
        // Clear overlays and states (like resetGameState)
        if (retryButton != null) {
            remove(retryButton);
            retryButton = null;
        }
        if (menuButton != null) {
            remove(menuButton);
            menuButton = null;
        }
        //  Explicitly reset all input flags to prevent latched states causing unresponsiveness
        left = right = up = attackR = attackT = false;
        p2Left = p2Right = p2Up = p2Attack1 = p2Attack2 = false;
        initGameObjects(); // Reinitialize fighters
        gameOver = false;
        winner = "";
        stopGame(); // Halt current timer
        // NEWNEWNEW: Restart round timer and game loop fresh
        resetRoundTimer(); // reset countdown and restart round timer
        startGame(); // reinitialize and start the game loop timer with focus
        repaint(); // Ensure immediate visual update
    }

    // Helper to reset and restart the round timer
    private void resetRoundTimer() {
        // stop any existing timer
        if (roundTimer != null) {
            roundTimer.stop();
        }
        timeLeft = INITIAL_ROUND_TIME;
        roundTimer = new Timer(1000, e -> {
            if (!gameOver) {
                timeLeft--;
                if (timeLeft <= 0) {
                    timeLeft = 0;
                    gameOver = true;
                    roundTimer.stop();
                    determineWinner();
                    // show overlay on time out
                    showOverlay();
                }
            }
        });
        roundTimer.start();
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

    // ADDED: Public method to check if the game timer is running (encapsulates private field access)
    public boolean isGameRunning() {
        return gameTimer != null && gameTimer.isRunning();
    }

    // ADDED: Public method to stop the game timer if running (encapsulates private field access)
    public void stopGame() {
        if (gameTimer != null && gameTimer.isRunning()) {
            gameTimer.stop();
        }
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