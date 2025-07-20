import java.awt.image.BufferedImage;

public class Obstacles {
    int x;
    int y;
    int width;
    int height;
    BufferedImage image;

    public Obstacles(int x, int y, int width, int height, BufferedImage image){
            this.x = x;
            this.y = y - 5;
            this.width = width*4;
            this.height = height*4;
            this.image = image;
    }


}
