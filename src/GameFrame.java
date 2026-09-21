import javax.swing.JFrame;
import javax.swing.JPanel; // ADDED: For contentPanel
import java.awt.CardLayout; // ADDED: For panel switching

public class GameFrame extends JFrame {

    private CardLayout cardLayout; // ADDED: Layout manager
    private JPanel contentPanel; // ADDED: Container for panels
    private GamePanel gamePanel; // ADDED: Reference to game panel for management

    //   Track current game mode (set by ModeSelectionPanel; defaults to TWO_PLAYER for compatibility)
    public GamePanel.GameMode currentMode = GamePanel.GameMode.TWO_PLAYER;

    public GameFrame() {
        setTitle("2D Fighting game");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        // ADDED: Initialize CardLayout and content panel
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        // ADDED: Create panels
        MenuPanel menuPanel = new MenuPanel(this); // New main menu panel
        ModeSelectionPanel modePanel = new ModeSelectionPanel(this); // New mode selection panel
        //   Add DifficultyPanel for CPU mode
        DifficultyPanel difficultyPanel = new DifficultyPanel(this);
        gamePanel = new GamePanel(); // Original game panel (no frame ref needed here, but added for consistency)
        
        // ADDED: Add panels to content
        contentPanel.add(menuPanel, "menu");
        contentPanel.add(modePanel, "mode");
        //   Add difficulty panel to layout
        contentPanel.add(difficultyPanel, "difficulty_selection");
        contentPanel.add(gamePanel, "game");
        
        // ADDED: Set content and show menu by default
        add(contentPanel);
        cardLayout.show(contentPanel, "menu");
        
        pack();
        setLocationRelativeTo(null);
        setVisible(true);

// REMOVED: Original direct GamePanel addition and startGame call (replaced by menu flow)
        // GamePanel panel = new GamePanel();
        // add(panel);
        // pack();

        // setLocationRelativeTo(null);
        // setVisible(true);

       // panel.startGame();
    }
    // ADDED: Method to show mode selection (called from main menu)
    public void showModeSelection() {
        cardLayout.show(contentPanel, "mode");
    }

    //   Added method to show difficulty selection (called from Vs CPU)
    public void showDifficultySelection() {
        cardLayout.show(contentPanel, "difficulty_selection");
    }

   // ADDED: Method to start game (called from mode buttons)
    public void startGame() {
        //  Reset game state before starting to clear any prior win overlays or frozen states
        gamePanel.resetGameState();
        cardLayout.show(contentPanel, "game");
        if (!gamePanel.isGameRunning()) { // UPDATED: Use utility method instead of direct field access
            gamePanel.startGame();
        }
        gamePanel.requestFocusInWindow(); // ADDED: Ensure input focus
    }

    public void startGame(GamePanel.GameMode mode, GamePanel.Difficulty diff) {
        currentMode = mode;
        gamePanel.setDifficulty(diff);
        startGame();  // Call parameterless version to proceed
    }

    // ADDED: Method to return to menu (called from game or back button)
    public void goToMenu() {
        gamePanel.stopGame(); // UPDATED: Use utility method instead of direct field access
        cardLayout.show(contentPanel, "menu");
    }



}