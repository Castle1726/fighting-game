import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * Utility helper for loading images from the classpath first, falling back to filesystem.
 * Works both in IDE and when packaged inside a JAR.
 */
public class Assets {
    public static BufferedImage loadImage(String path) {
        // try classpath (resource path should not start with '/'): e.g., "assets/images/..."
        String resourcePath = path.startsWith("/") ? path.substring(1) : path;
        InputStream is = Assets.class.getClassLoader().getResourceAsStream(resourcePath);
        if (is != null) {
            try {
                BufferedImage img = ImageIO.read(is);
                if (img != null) return img;
            } catch (IOException e) {
                System.out.println("Failed to read resource stream for: " + resourcePath + " -> " + e.getMessage());
            }
        }

        // fallback to filesystem path (useful when running from IDE with project working dir)
        try {
            File f = new File(path);
            if (f.exists()) {
                BufferedImage img = ImageIO.read(f);
                if (img != null) return img;
            }
        } catch (IOException e) {
            System.out.println("Failed to read file for: " + path + " -> " + e.getMessage());
        }

        // Additional fallback: search upward from current working directory for an "assets" folder
        File cwd = new File(System.getProperty("user.dir"));
        File walker = cwd;
        int maxLevels = 6; // don't go up forever
        for (int i = 0; i < maxLevels && walker != null; i++) {
            File candidate = new File(walker, path);
            try {
                if (candidate.exists()) {
                    BufferedImage img = ImageIO.read(candidate);
                    if (img != null) {
                        System.out.println("Loaded asset by walking up: " + candidate.getAbsolutePath());
                        return img;
                    }
                }
            } catch (IOException e) {
                System.out.println("Failed to read candidate file: " + candidate.getAbsolutePath() + " -> " + e.getMessage());
            }
            walker = walker.getParentFile();
        }

        System.out.println("Asset not found: " + path + " (cwd=" + System.getProperty("user.dir") + ")");
        return null;
    }
}
