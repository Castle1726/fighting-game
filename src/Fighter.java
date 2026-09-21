import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Color;
import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;
import javax.swing.Timer;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.awt.Image;



public class Fighter {
    private boolean flip;
    //   Changed to protected for subclass (AIFighter) access in AI logic
    protected Rectangle rect;
    private int velY;
    //   Changed to protected for subclass (AIFighter) access in AI logic
    protected boolean jumping;
    private boolean attacking;
    private int attackType;
    private int health;

    private Image sprite;
    private Image spriteFlipped;
    private String spritePath = null;
    // whether the source sprite faces right by default (true) or left (false)
    private boolean spriteFacesRight = true;
    // true if the fighter is currently facing right in game world coordinates
    private boolean facingRight = true;

    // used to end attack after short duration (makes gameplay friendly)
    private Timer attackTimer;

    // Constructor
    public Fighter(int x, int y) {
        this(x, y, null);
    }

   /*
      Create a Fighter and optionally specify a sprite path for this instance.
      If spritePath is null, the default player1.png path will be used.
     */
    public Fighter(int x, int y, String spritePath) {
        this(x, y, spritePath, true);
    }

    // New constructor: allow specifying whether the source sprite faces right by default
    public Fighter(int x, int y, String spritePath, boolean spriteFacesRight) {
        this.flip = false;
        this.rect = new Rectangle(x, y, 80, 180);
        this.velY = 0;
        this.jumping = false;
        this.attacking = false;
        this.attackType = 0;
        this.health = 100;
        this.spritePath = spritePath;
        this.spriteFacesRight = spriteFacesRight;

        // load sprite
        loadSprite();
    }

    // Load sprite + flipped version
    private void loadSprite() {
        // Use Assets helper that tries classpath first (works in JAR and IDE)
        String path = this.spritePath != null ? this.spritePath : "assets/images/characters/player1.png";
        java.awt.image.BufferedImage img = Assets.loadImage(path);
        if (img != null) {
            sprite = img;
            spriteFlipped = flipImage(sprite);
            System.out.println("Loaded sprite: " + path);
        } else {
            System.out.println("Failed to load sprite: " + path);
        }
    }

    // Flip sprite horizontally
    private Image flipImage(Image img) {
        int w = img.getWidth(null);
        int h = img.getHeight(null);

        BufferedImage flipped = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = flipped.createGraphics();

        g2.drawImage(img, w, 0, -w, h, null);
        g2.dispose();

        return flipped;
    }

    // move signature mirrors python: screen_width, screen_height, surface, target
    // additional booleans for input (since we don't call get_pressed in Swing)
    public void move(int screenWidth, int screenHeight, Component surface, Fighter target,
                     boolean keyA, boolean keyD, boolean keyW, boolean keyR, boolean keyT) {

        final int SPEED = 10;
        final int GRAVITY = 2;
        int dx = 0;
        int dy = 0;

        // can only perform other actions if not currently attacking
        if (!this.attacking) {
            // movement
            if (keyA) dx = -SPEED;
            if (keyD) dx = SPEED;

            // jump
            if (keyW && !this.jumping) {
                this.velY = -25;
                this.jumping = true;
            }

            // attack
            if (keyR || keyT) {
                this.attack(surface, target);
                if (keyR) this.attackType = 1;
                if (keyT) this.attackType = 2;
            }
        }

        // apply gravity
        this.velY += GRAVITY;
        dy += this.velY;

        //************************ COLISION PART (THIS COMMENT CAN BE REMOVED IF NEEDED) *********************** */
        // Here is the Horizontal movement and collision resolution (prevents overlapping with target)
        int newX = this.rect.x + dx;
        Rectangle horizRect = new Rectangle(newX, this.rect.y, this.rect.width, this.rect.height);
        if (horizRect.intersects(target.rect)) 
            {
            if (dx > 0) 
                {
                // Colliding while moving right: snap to left side of target
                newX = target.rect.x - this.rect.width;
            } else if (dx < 0) 
                {
                // Colliding while moving left: snap to right side of target
                newX = target.rect.x + target.rect.width;
            } else 
                {
                // No horizontal velocity but overlapping: push apart based on current positions
                if (this.rect.getCenterX() < target.rect.getCenterX()) 
                    {
                    newX = target.rect.x - this.rect.width;
                } else 
                    {
                    newX = target.rect.x + target.rect.width;
                }
            }
        }
        this.rect.x = newX;

        // CHANGED: Clamp horizontal position to screen bounds (moved after collision resolution for accuracy)
        if (this.rect.x < 0) {
            this.rect.x = 0;
        }
        if (this.rect.x + this.rect.width > screenWidth) {
            this.rect.x = screenWidth - this.rect.width;
        }

        // Vertical movement and collision resolution (handles landing on or bumping into target)
        int newY = this.rect.y + dy;
        Rectangle vertRect = new Rectangle(this.rect.x, newY, this.rect.width, this.rect.height);
        if (vertRect.intersects(target.rect)) 
            {
            if (dy > 0) 
                {
                // Falling: land on top of target
                newY = target.rect.y - this.rect.height;
                this.velY = 0;
                this.jumping = false;
            } else if (dy < 0) 
                {
                // Rising: collide with bottom of target
                newY = target.rect.y + target.rect.height;
                this.velY = 0;
            }
        }
        this.rect.y = newY;

        // CHANGED: Clamp vertical position to screen bounds (ground and ceiling; now after collision for consistency)
        if (this.rect.y + this.rect.height > screenHeight - 20) 
            {
            this.rect.y = (screenHeight - 20) - this.rect.height;
            this.velY = 0;
            this.jumping = false;
        }
        if (this.rect.y < 0) 
            {
            this.rect.y = 0;
            this.velY = 0;
        }

        //************************************************************* */
        // ensure player stays on screen
        // if (this.rect.x + dx < 0) {
        //     dx = -this.rect.x;
        // }
        // if (this.rect.x + this.rect.width + dx > screenWidth) {
        //     dx = screenWidth - (this.rect.x + this.rect.width);
        // }
        // if (this.rect.y + this.rect.height + dy > screenHeight - 20) {
        //     this.velY = 0;
        //     this.jumping = false;
        //     dy = (screenHeight - 20) - (this.rect.y + this.rect.height);
        // }

        // ensure players face each other; compute desired facing
        boolean shouldFaceRight = target.rect.getCenterX() > this.rect.getCenterX();
        this.facingRight = shouldFaceRight;
        // compute whether to draw the flipped image based on the source sprite orientation
        this.flip = this.spriteFacesRight ? !this.facingRight : this.facingRight;

        // update player position
        this.rect.translate(dx, dy);
    }

    public void attack(Component surface, Fighter target) {
        this.attacking = true;

        // calculate attacking rect similar to your:
        // attacking_rect = Rect(centerx - (2*width*flip), y, 2*width, height)
        int attackWidth = 2 * this.rect.width;
        int attackX;
        // use world-facing (`facingRight`) to determine attack direction
        if (!this.facingRight) {
            // facing left -> attack area to the left
            attackX = this.rect.x + this.rect.width / 2 - 2 * this.rect.width;
        } else {
            // facing right -> attack area to the right
            attackX = this.rect.x + this.rect.width / 2;
        }
        Rectangle attackingRect = new Rectangle(attackX, this.rect.y, attackWidth, this.rect.height);

        // check collision
        if (attackingRect.intersects(target.rect)) {
            target.health -= 10;
            if (target.health < 0) target.health = 0;
        }

        // draw the attack rectangle immediately on the surface by using surface's repaint
        // (the actual drawing happens in draw(); we store the rect temporarily by triggering repaint).
        // To visualize the attack for a short time, we set an attackTimer that lasts 200ms,
        // after which attacking becomes false.
        if (attackTimer != null && attackTimer.isRunning()) {
            attackTimer.stop();
        }
        attackTimer = new Timer(200, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                attacking = false;
                attackTimer.stop();
            }
        });
        attackTimer.setRepeats(false);
        attackTimer.start();

        // store the last attacking rect so draw() can render it
        this.lastAttackRect = attackingRect;
    }

    // store last attack rect for visualization
    private Rectangle lastAttackRect = null;

    public void draw(Graphics2D g) {
        // draw sprite if available, otherwise fallback to rectangle
        if (this.sprite != null) {
            Image imgToDraw = this.flip && this.spriteFlipped != null ? this.spriteFlipped : this.sprite;
            g.drawImage(imgToDraw, rect.x, rect.y, rect.width, rect.height, null);
        } else {
            g.setColor(Color.RED);
            g.fillRect(rect.x, rect.y, rect.width, rect.height);
        }

        // draw attack rect if attacking (green)
        if (this.attacking && this.lastAttackRect != null) {
            g.setColor(new Color(0, 255, 0, 200));
            g.fillRect(lastAttackRect.x, lastAttackRect.y, lastAttackRect.width, lastAttackRect.height);
        }
    }

    // getters for health (used by GamePanel)
    public int getHealth() {
        return health;
    }

    // allow external access if you want to set health / reset etc.
    public void setHealth(int health) {
        this.health = health;
    }

    //   Added public getter for rect (required for AI centerX calculations)
    public Rectangle getRect() {
        return rect;
    }

    //   Added public getter for jumping state (required for AI jump decisions)
    public boolean isJumping() {
        return jumping;
    }
}