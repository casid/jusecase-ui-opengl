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

    private Node2d scene = new Node2d();
    private Ball ball;

    public static void main(String[] args) {
        new LwjglApplicationBackend(QuadPongTester.class).start();
    }

    @Override
    public void init() {
        ball = new Ball();
        ball.setWidth(20.0f);
        ball.setHeight(20.0f);
        ball.speedX = ball.speedY = 500.0f;
        moveBallToCenter();
        scene.add(ball);
    }

    private void moveBallToCenter() {
        ball.setX((applicationBackend.getWidth() - ball.getWidth()) / 2);
        ball.setY((applicationBackend.getHeight() - ball.getHeight()) / 2);
    }

    @Override
    public void process(TouchEvent touchEvent) {
    }

    @Override
    public void update() {
        ball.update();
    }

    @Override
    public void render(Renderer renderer) {
        renderer.render(scene);
    }

    @Override
    public void dispose() {
        // nothing to do
    }

    @Component
    public static class Ball extends Quad {
        @Inject
        private ApplicationBackend applicationBackend;
        @Inject
        private Timer timer;

        private float speedX;
        private float speedY;

        public void reflectX() {
            speedX = -speedX;
            changeColor();
        }

        public void reflectY() {
            speedY = -speedY;
            changeColor();
        }

        public void changeColor() {
            getColor().randomHue();
        }

        public void update() {
            float step = 0.001f;
            float ballX = getX();
            float ballY = getY();
            float width = applicationBackend.getWidth();
            float height = applicationBackend.getHeight();

            for (float dt = 0; dt <= timer.dt(); dt += step) {
                ballX += speedX * step;
                ballY += speedY * step;

                if (ballX < 0.0f) {
                    ballX = 0.0f;
                    reflectX();
                }

                if (ballX + getWidth() > width) {
                    ballX = width - getWidth();
                    reflectX();
                }

                if (ballY < 0.0f) {
                    ballY = 0.0f;
                    reflectY();
                }

                if (ballY + getHeight() > height) {
                    ballY = height - getHeight();
                    reflectY();
                }
            }
            setX(ballX);
            setY(ballY);
        }
    }
}
