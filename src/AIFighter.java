import java.awt.Component;
import java.awt.Rectangle;
import java.util.Random;

// New class for AI opponent, extending Fighter (ignores human inputs, computes actions probabilistically)
public class AIFighter extends Fighter {
    private GamePanel.Difficulty difficulty; // Tracks AI difficulty level (qualified enum)
    private Random random; // For probabilistic decisions
    private int direction = 1; // 1 for right, -1 for left (movement direction)

    // Constructor initializes with position and difficulty
    public AIFighter(int x, int y, GamePanel.Difficulty difficulty) {
        super(x, y);
        this.difficulty = difficulty;
        this.random = new Random();
    }

    // New overloaded constructor to support custom sprite path and facing direction
    public AIFighter(int x, int y, String spritePath, boolean spriteFacesRight, GamePanel.Difficulty difficulty) {
        super(x, y, spritePath, spriteFacesRight);
        this.difficulty = difficulty;
        this.random = new Random();
    }

    @Override
    public void move(int screenWidth, int screenHeight, Component surface, Fighter target,
                     boolean keyA, boolean keyD, boolean keyW, boolean keyR, boolean keyT) {
        // Ignore passed key booleans; compute AI actions instead

        // Determine aggression levels based on difficulty
        double aggressionLevel = 0.0; // Easy: pure patrol
        double attackProb = 0.01;
        double jumpProb = 0.005;
        double reverseProb = 0.005;

        if (difficulty == GamePanel.Difficulty.MEDIUM) {
            aggressionLevel = 0.5;
            attackProb = 0.03;
            jumpProb = 0.01;
            reverseProb = 0.003; // Less random reversal for more purposeful movement
        } else if (difficulty == GamePanel.Difficulty.HARD) {
            aggressionLevel = 1.0;
            attackProb = 0.05;
            jumpProb = 0.02;
            reverseProb = 0.001; // Minimal random reversal
        }

        // Decide movement direction: approach target with aggression probability, else patrol
        if (Math.random() < aggressionLevel) {
            // Approach target
            double centerDiff = target.getRect().getCenterX() - this.getRect().getCenterX();
            direction = centerDiff > 0 ? 1 : -1;
        } else {
            // Patrol logic: reverse at edges or randomly
            if (this.rect.x <= 0) {
                direction = 1;
            } else if (this.rect.x + this.rect.width >= screenWidth) {
                direction = -1;
            }
            if (Math.random() < reverseProb) {
                direction = -direction;
            }
        }

        // Compute AI input booleans
        boolean aiA = (direction == -1);
        boolean aiD = (direction == 1);
        boolean aiW = (!this.isJumping() && Math.random() < jumpProb);

        // Attack if close enough and probability hits
        double dist = Math.abs(this.getRect().getCenterX() - target.getRect().getCenterX());
        boolean shouldAttack = (dist < 200 && Math.random() < attackProb);
        boolean aiR = false;
        boolean aiT = false;
        if (shouldAttack) {
            if (random.nextBoolean()) {
                aiR = true;
            } else {
                aiT = true;
            }
        }

        // Call super with AI-computed inputs (polymorphic behavior reuses base logic)
        super.move(screenWidth, screenHeight, surface, target, aiA, aiD, aiW, aiR, aiT);
    }
}