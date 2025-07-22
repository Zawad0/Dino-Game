import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

import static java.lang.Double.max;

public class GamePanel extends JPanel implements Runnable{
    Thread gameThread;
    BufferedImage background, base, shadow, trees, moonStars, clouds,
            skele, skeleAtk, log, trunk, stone1, stone2;
    Player dino;
    Font pixelFont;

    int velocityY = -20;
    int velocityX = -8;
    int gravity = 1;
    int groundY = Constant.dinoIdleY;
    boolean jumped = false;
    private int score = 0;
    private int highScore = 0;
    private double diffModif = 1;


    GameStates gameStates = GameStates.IDLE;
    private double animationTimer = 0;
    private int currentFrame = 0;


    double backgroundX = Constant.backgroundX;
    double shadowX = Constant.shadowX;
    double treesX = Constant.treesX;
    double cloudsX = Constant.cloudsX;

    ArrayList<Obstacles> obstaclesArray = new ArrayList<>(1);
    double spawnTimer = 0;

    GamePanel(){
        this.setPreferredSize(new Dimension(Constant.SCREEN_WIDTH,Constant.SCREEN_HEIGHT)) ;
        this.setDoubleBuffered(true);
        this.setBackground(Color.BLACK);
        this.setFocusable(true);
        try {
            pixelFont = Font.createFont(Font.TRUETYPE_FONT, getClass().
                    getResourceAsStream("/PixelifySans-VariableFont_wght.ttf"));
            base = ImageIO.read(getClass().getResource("/Sprite-base.png"));
            background = ImageIO.read(getClass().getResource("/Sprite-mountains.png"));
            shadow = ImageIO.read(getClass().getResource("/Sprite-trees2.png"));
            trees = ImageIO.read(getClass().getResource("/Sprite-detailedtrees.png"));
            moonStars = ImageIO.read(getClass().getResource("/Sprite-moon.png"));
            clouds = ImageIO.read(getClass().getResource("/Sprite-clouds.png"));

            stone1 = ImageIO.read(getClass().getResource("/Sprite-stone1.png"));
            stone2 = ImageIO.read(getClass().getResource("/Sprite-stone2.png"));
            trunk = ImageIO.read(getClass().getResource("/Sprite-treestump.png"));
            log = ImageIO.read(getClass().getResource("/Sprite-treelog.png"));
            skele = ImageIO.read(getClass().getResource("/Sprite-skelewag1.png"));
            skeleAtk = ImageIO.read(getClass().getResource("/Sprite-skelewag2.png"));

            dino = new Player(this, Constant.dinoIdleX, Constant.dinoIdleY, 32*3,32*3);


            addKeyListener(new KeyListener() {
                @Override
                public void keyTyped(KeyEvent e) {

                }

                @Override
                public void keyPressed(KeyEvent e) {
                    if(e.getKeyCode() == KeyEvent.VK_SPACE){
                        if(gameStates == GameStates.RUNNING && dino.y == groundY){
                            velocityY = -20;
                            jumped = true;

                        }
                    }
                }

                @Override
                public void keyReleased(KeyEvent e) {
                    if(e.getKeyCode() == KeyEvent.VK_SPACE){
                        if(gameStates == GameStates.IDLE){
                            gameStates = GameStates.RUNNING;
                        }
                        else if(gameStates == GameStates.GAME_OVER){
                            resetGame();
                        }
                    }
                }
            });

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void resetGame(){

        dino.x = Constant.dinoIdleX;
        dino.y = Constant.dinoIdleY;
        velocityY = 0;
        score = 0;
        diffModif = 0;
        backgroundX = Constant.backgroundX;
        shadowX = Constant.shadowX;
        treesX = Constant.treesX;
        cloudsX = Constant.cloudsX;

        obstaclesArray.clear();
        spawnTimer = 0;

        currentFrame = 0;
        animationTimer = 0;

        gameStates = GameStates.IDLE;

    }


    public void startThread(){
        gameThread = new Thread(this);
        gameThread.start();
    }

    class Time{
        static double timeStarted = System.nanoTime();
        static double getTime(){ return (System.nanoTime() - timeStarted) * 1E-9;}

    }
    @Override
    public void run() {
        double lastFrameTime = Time.getTime();
        while(true){
            double time = Time.getTime();
            double deltaTime = time - lastFrameTime;
            lastFrameTime = time;

            update(deltaTime);
            repaint();

            try{
                Thread.sleep(1000/Constant.FPS_CAP);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void update(double dt){
        switch (gameStates){
            case IDLE:
                animationTimer+=dt;
                idleAnimationHandler();
                break;

            case RUNNING:
                animationTimer+=dt;
                move();
                difficulty();
                spawnTimer += dt;
                if(spawnTimer >= 1){
                    createObstacles();
                    spawnTimer=0;
                }

                if(checkCollisions()){
                    if(score>highScore) highScore = score;
                    gameStates = GameStates.GAME_OVER;

                    setBackground(Color.WHITE);
                    Timer timer = new Timer(150, e -> setBackground(Color.BLACK));
                    timer.setRepeats(false);
                    timer.start();
                }

                if(dino.y != groundY){
                    jumpingAnimationHandler();
                }
                else
                    runningAnimationHandler();
                backgroundX -= (1 * (1+diffModif));
                shadowX -= (1.8 * (1+diffModif));
                treesX -= (2.1 * (1+diffModif));
                cloudsX -= (0.9 * (1+diffModif));


                if (backgroundX <= -background.getWidth()*2) {
                    backgroundX = 0;
                }
                if (shadowX <= -shadow.getWidth()*2) {
                    shadowX = 0;
                }
                if (treesX <= -trees.getWidth()*2) {
                    treesX = 0;
                }
                if (cloudsX <= -clouds.getWidth()*2) {
                    cloudsX = 0;
                }
                break;

            case GAME_OVER:
                break;
        }

    }


    private boolean checkCollisions() {
        float shrinkFactor = 0.8f;

        int dinoWidthShrunk = (int)(dino.width * shrinkFactor);
        int dinoHeightShrunk = (int)(dino.height * shrinkFactor);
        int dinoXOffset = (int)(dino.width * (1 - shrinkFactor) / 2);
        int dinoYOffset = (int)(dino.height * (1 - shrinkFactor) / 2);

        Rectangle dinoRect = new Rectangle(
                (int)dino.x + dinoXOffset,
                (int)dino.y + dinoYOffset,
                dinoWidthShrunk,
                dinoHeightShrunk
        );

        for (Obstacles obstacle : obstaclesArray) {
            int obstWidthShrunk = (int)(obstacle.width * shrinkFactor);
            int obstHeightShrunk = (int)(obstacle.height * shrinkFactor);
            int obstXOffset = (int)(obstacle.width * (1 - shrinkFactor) / 2);
            int obstYOffset = (int)(obstacle.height * (1 - shrinkFactor) / 2);

            Rectangle obstacleRect = new Rectangle(
                    obstacle.x + obstXOffset,
                    obstacle.y + obstYOffset,
                    obstWidthShrunk,
                    obstHeightShrunk
            );

            if (dinoRect.intersects(obstacleRect)) {
                return true;
            }
        }
        return false;
    }

    private void move(){
        //System.out.println("Before move - Y: " + dino.y + ", velocityY: " + velocityY); //debug

        dino.y += velocityY;


        if(!jumped){
            velocityY+=gravity;

        } else {
            jumped = false;
        }
        if (dino.y >= groundY) {
            //System.out.println("Hit ground!");
            dino.y = groundY;
            velocityY = 0;
        }

        score++;

        //System.out.println("After move - Y: " + dino.y + ", velocityY: " + velocityY);

        for(int i=0;i<obstaclesArray.size();i++){
            Obstacles obstacles = obstaclesArray.get(i);
            obstacles.x+= (int) (velocityX * (1+diffModif));


        }
    }

    private void createObstacles(){
        double placeChance1 = Math.random();
        double placeChance2 = Math.random();

        if(placeChance1 > 0.90){
                Obstacles skele_obj1 = new Obstacles(Constant.SCREEN_WIDTH, Constant.SCREEN_HEIGHT-skele.getHeight()*5, skele.getWidth(), skele.getHeight(),skele);
                obstaclesArray.add(skele_obj1);
        }
        else if(placeChance1 > 0.70){
            if(placeChance2 > 0.50){
                Obstacles tree_obj1 = new Obstacles(Constant.SCREEN_WIDTH, (Constant.SCREEN_HEIGHT-log.getHeight()*4)-5, log.getWidth(), log.getHeight(),log);
                obstaclesArray.add(tree_obj1);
            }
            else{
                Obstacles tree_obj2 = new Obstacles(Constant.SCREEN_WIDTH, (Constant.SCREEN_HEIGHT-trunk.getHeight()*4)-5, trunk.getWidth(), trunk.getHeight(),trunk);
                obstaclesArray.add(tree_obj2);
            }
        } else if (placeChance1 > 0.50) {
            if(placeChance2 > 0.50){
                Obstacles stone_obj1 = new Obstacles(Constant.SCREEN_WIDTH, (Constant.SCREEN_HEIGHT-stone1.getHeight()*4)-5, stone1.getWidth(), stone1.getHeight(),stone1);
                obstaclesArray.add(stone_obj1);
            }
            else{
                Obstacles stone_obj2 = new Obstacles(Constant.SCREEN_WIDTH, (Constant.SCREEN_HEIGHT-stone2.getHeight()*4)-5, stone2.getWidth(), stone2.getHeight(),stone2);
                obstaclesArray.add(stone_obj2);
            }

        }
        if(obstaclesArray.size()>20){
            obstaclesArray.remove(0);
        }
    }

    void drawObstacles(Graphics g){
        for (int i = 0; i<obstaclesArray.size();i++){
            Obstacles obstacles = obstaclesArray.get(i);
            g.drawImage(obstacles.image, obstacles.x, obstacles.y, obstacles.width, obstacles.height, null);
        }
    }

    private void difficulty(){
        diffModif = (double)score/4500;
        if(diffModif >= Constant.terminalVelo) diffModif = Constant.terminalVelo;

    }

    private void idleAnimationHandler(){
        dino.dinoIdle.frameX = Constant.dinoIdleX;
        dino.y = groundY;
        if (animationTimer >= Constant.FRAME_DURATION_IDLE) {
            animationTimer = 0;
            currentFrame++;
            if (currentFrame >= dino.dinoIdle.getFrameCount()) {
                currentFrame = 0;
            }
        }
    }

    private void runningAnimationHandler(){

        if (animationTimer >= Constant.FRAME_DURATION_RUN) {
            animationTimer = 0;
            currentFrame++;
            if (currentFrame >= dino.dinoRun.getFrameCount()) {
                currentFrame = 0;
            }
        }
    }

    private void jumpingAnimationHandler(){

        if (animationTimer >= Constant.FRAME_DURATION_JUMP) {
            animationTimer = 0;
            currentFrame++;
            if (currentFrame >= dino.dinoJump.getFrameCount()) {
                currentFrame = 0;
            }
        }
    }



    public void paintComponent(Graphics g){
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setColor(Color.white);
        g2.setFont(pixelFont.deriveFont( 40f));
        switch(gameStates){

            case RUNNING:


                g2.drawImage(base, 0, 0, Constant.SCREEN_WIDTH, Constant.SCREEN_HEIGHT, null);
                g2.drawImage(moonStars, 0, 0, Constant.SCREEN_WIDTH, Constant.SCREEN_HEIGHT, null);
                g2.drawImage(background, (int)backgroundX, 0, Constant.SCREEN_WIDTH, Constant.SCREEN_HEIGHT, null);
                g2.drawImage(background, (int)backgroundX + background.getWidth()*2, 0, Constant.SCREEN_WIDTH, Constant.SCREEN_HEIGHT, null);

                g2.drawImage(shadow, (int)shadowX, 0, Constant.SCREEN_WIDTH, Constant.SCREEN_HEIGHT, null);
                g2.drawImage(shadow, (int)shadowX + shadow.getWidth()*2, 0, Constant.SCREEN_WIDTH, Constant.SCREEN_HEIGHT, null);
                g2.drawImage(trees, (int)treesX, 0, Constant.SCREEN_WIDTH, Constant.SCREEN_HEIGHT, null);
                g2.drawImage(trees, (int)treesX + trees.getWidth()*2, 0, Constant.SCREEN_WIDTH, Constant.SCREEN_HEIGHT, null);


                g2.drawImage(clouds, (int)cloudsX, 0, Constant.SCREEN_WIDTH, Constant.SCREEN_HEIGHT, null);
                g2.drawImage(clouds, (int)cloudsX + clouds.getWidth()*2, 0, Constant.SCREEN_WIDTH, Constant.SCREEN_HEIGHT, null);
                drawObstacles(g2);
                if(dino.y != groundY){
                    dino.drawJump(g2, currentFrame);
                }
                else
                    dino.drawRun(g2, currentFrame);
                g.drawString(String.valueOf(score), 10, 35);
//                // Draw debug text
//                g2.setColor(Color.WHITE);
//                g2.drawString("Y Position: " + dino.y, 20, 20);
//                g2.drawString("VelocityY: " + velocityY, 20, 40);
//                g2.drawString("GroundY: " + groundY, 20, 60);
                break;

            case IDLE:
                g2.drawImage(base, 0, 0, Constant.SCREEN_WIDTH, Constant.SCREEN_HEIGHT, null);
                g2.drawImage(moonStars, 0, 0, Constant.SCREEN_WIDTH, Constant.SCREEN_HEIGHT, null);
                g2.drawImage(background, (int)backgroundX, 0, Constant.SCREEN_WIDTH, Constant.SCREEN_HEIGHT, null);
                g2.drawImage(shadow, (int)shadowX, 0, Constant.SCREEN_WIDTH, Constant.SCREEN_HEIGHT, null);
                g2.drawImage(trees, (int)treesX, 0, Constant.SCREEN_WIDTH, Constant.SCREEN_HEIGHT, null);

                g2.drawImage(clouds, (int)cloudsX, 0, Constant.SCREEN_WIDTH, Constant.SCREEN_HEIGHT, null);
                dino.drawIdle(g2, currentFrame);
                g2.setFont(pixelFont.deriveFont( 50f));
                g2.drawString("PRESS SPACE TO START GAME", 140, (Constant.SCREEN_HEIGHT/2)-20);

                break;

            case GAME_OVER:
                g2.setFont(pixelFont.deriveFont( 70f));
                g2.drawString("GAME OVER", (Constant.SCREEN_WIDTH/2)-165, (Constant.SCREEN_HEIGHT/2)-50);
                g2.setFont(pixelFont.deriveFont( 45f));
                g2.drawString("Score: "+String.valueOf(score), (Constant.SCREEN_WIDTH/2)-165, (Constant.SCREEN_HEIGHT/2)+10);
                g2.drawString("High Score: "+String.valueOf(highScore), (Constant.SCREEN_WIDTH/2)-165, (Constant.SCREEN_HEIGHT/2)+75);

        }

    }
}
