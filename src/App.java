import javax.swing.SwingUtilities;

public class App {
    public static void main(String[] args) {
        // print current working directory to help debug asset loading when run from IDE
        System.out.println("cwd: " + System.getProperty("user.dir"));

        // Diagnostic checks for asset loading
        String[] testAssets = {
            "assets/images/characters/player1.png",
            "assets/images/characters/player2.png",
            "assets/images/background/arena 1.png"
        };
        ClassLoader cl = App.class.getClassLoader();
        for (String p : testAssets) {
            System.out.println("--- Checking: " + p + " ---");
            java.net.URL url = cl.getResource(p);
            System.out.println("getResource: " + url);
            java.io.File f = new java.io.File(p);
            System.out.println("file.exists: " + f.exists() + " -> " + f.getAbsolutePath());
            java.awt.image.BufferedImage img = Assets.loadImage(p);
            System.out.println("Assets.loadImage returned: " + (img != null));
        }

        SwingUtilities.invokeLater(() -> {
            new GameFrame();
        });
    }
}

