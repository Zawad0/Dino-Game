import java.awt.*;
import java.awt.image.BufferedImage;

public class Player {

    public SpriteSheet dinoIdle, dinoJump, dinoRun;
    public double x, y, width, height;
    public Player(GamePanel gp, int x, int y, int width, int height) throws Exception {
        dinoIdle = new SpriteSheet("/Sprite-dino-Sheet.png", 32);
        dinoJump = new SpriteSheet("/Sprite-dinojump2-Sheet.png",32);
        dinoRun = new SpriteSheet("/Sprite-dinorun.png",32);
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }
    public void drawIdle(Graphics g, int currentFrame){
        BufferedImage playerFrame = dinoIdle.getFrame(currentFrame);

        g.drawImage(playerFrame, (int)x, (int)y, (int)width, (int)height, null);
    }

    public void drawRun(Graphics g, int currentFrame){
        BufferedImage playerFrame = dinoRun.getFrame(currentFrame);

        g.drawImage(playerFrame, (int)x, (int)y, (int)width, (int)height, null);
    }

    public void drawJump(Graphics g, int currentFrame){
        BufferedImage playerFrame = dinoJump.getFrame(currentFrame);


        g.drawImage(playerFrame, (int)x, (int)y, (int)width, (int)height, null);
    }
}
