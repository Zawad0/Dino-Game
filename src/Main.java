import javax.swing.*;

public class Main {

    public static void main(String[] args){
        JFrame frame = new JFrame();
        GamePanel gamePanel = new GamePanel();
        frame.setTitle("Dino Game");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.add(gamePanel);
        gamePanel.startThread();
        frame.setResizable(false);
        frame.setSize(Constant.SCREEN_WIDTH,Constant.SCREEN_HEIGHT);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
