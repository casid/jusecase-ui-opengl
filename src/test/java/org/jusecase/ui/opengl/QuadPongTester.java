package org.jusecase.ui.opengl;

import org.jusecase.Application;
import org.jusecase.ApplicationBackend;
import org.jusecase.inject.Component;
import org.jusecase.scenegraph.node2d.Node2d;
import org.jusecase.scenegraph.node2d.Quad;
import org.jusecase.scenegraph.render.Renderer;
import org.jusecase.scenegraph.time.Timer;
import org.jusecase.ui.touch.TouchEvent;

import javax.inject.Inject;

@Component
public class QuadPongTester implements Application {

    @Inject
    private ApplicationBackend applicationBackend;
    @Inject
    private Timer timer;

    private Node2d scene = new Node2d();
    private Ball ball;
    private Player leftPlayer;
    private Player rightPlayer;

    public static void main(String[] args) {
        new LwjglApplicationBackend(QuadPongTester.class).start();
    }

    @Override
    public void init() {
        addBall();
        addRightPlayer();
        addLeftPlayer();

        applicationBackend.onResize().add((width, height) -> rightPlayer.setX(width - rightPlayer.getWidth()));
    }

    private void addBall() {
        ball = new Ball();
        ball.setSize(20.0f, 20.0f);
        ball.speedX = ball.speedY = 400.0f;
        moveBallToCenter();
        scene.add(ball);
    }

    private void addRightPlayer() {
        rightPlayer = addPlayer();
        rightPlayer.setX(applicationBackend.getWidth() - rightPlayer.getWidth());
    }

    private void addLeftPlayer() {
        leftPlayer = addPlayer();
        leftPlayer.setX(0.0f);
    }

    private Player addPlayer() {
        Player player = new Player();
        player.setSize(20.0f, 100.0f);
        player.setY((applicationBackend.getHeight() - player.getHeight()) / 2);
        scene.add(player);
        return player;
    }

    private void moveBallToCenter() {
        ball.setX((applicationBackend.getWidth() - ball.getWidth()) / 2);
        ball.setY((applicationBackend.getHeight() - ball.getHeight()) / 2);
    }

    @Override
    public void process(TouchEvent touchEvent) {
        rightPlayer.setY(touchEvent.y - 0.5f * rightPlayer.getHeight());
    }

    @Override
    public void update() {
        leftPlayer.setY(ball.getY() - 0.5f * (leftPlayer.getHeight() - ball.getHeight()));

        float step = 0.001f;
        float width = applicationBackend.getWidth();
        float height = applicationBackend.getHeight();

        for (float dt = 0; dt <= timer.dt(); dt += step) {
            ball.setX(ball.getX() + ball.speedX * step);
            ball.setY(ball.getY() + ball.speedY * step);

            if (ball.getX() < 0.0f) {
                score();
            }

            if (ball.getX() > width) {
                score();
            }

            if (ball.getX() < leftPlayer.getX() + leftPlayer.getWidth() && ball.getY() >= leftPlayer.getY() && ball.getY() <= leftPlayer.getY() + leftPlayer.getHeight()) {
                ball.setX(leftPlayer.getX() + leftPlayer.getWidth());
                ball.reflectX();
                ball.changeColor();
                leftPlayer.getColor().set(ball.getColor());
            }

            if (ball.getX() + ball.getWidth() > rightPlayer.getX() && ball.getY() >= rightPlayer.getY() && ball.getY() <= rightPlayer.getY() + rightPlayer.getHeight()) {
                ball.setX(rightPlayer.getX() - ball.getWidth());
                ball.reflectX();
                ball.changeColor();
                rightPlayer.getColor().set(ball.getColor());
            }

            if (ball.getY() < 0.0f) {
                ball.setY(0.0f);
                ball.reflectY();
            }

            if (ball.getY() + ball.getHeight() > height) {
                ball.setY(height - ball.getHeight());
                ball.reflectY();
            }
        }
    }

    private void score() {
        ball.reflectX();
        moveBallToCenter();
    }

    @Override
    public void render(Renderer renderer) {
        renderer.render(scene);
    }

    @Override
    public void dispose() {
        // nothing to do
    }

    public static class Ball extends Quad {
        private float speedX;
        private float speedY;

        public void reflectX() {
            speedX = -speedX;
        }

        public void reflectY() {
            speedY = -speedY;
        }

        public void changeColor() {
            getColor().randomHue();
        }
    }

    public static class Player extends Quad {
    }
}
