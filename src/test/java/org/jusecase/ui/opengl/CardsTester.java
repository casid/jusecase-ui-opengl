package org.jusecase.ui.opengl;

import org.jusecase.Application;
import org.jusecase.ApplicationBackend;
import org.jusecase.inject.Component;
import org.jusecase.scenegraph.Node;
import org.jusecase.scenegraph.color.Color;
import org.jusecase.scenegraph.node2d.Node2d;
import org.jusecase.scenegraph.node2d.Quad;
import org.jusecase.scenegraph.render.Renderer;
import org.jusecase.scenegraph.time.Timer;
import org.jusecase.scenegraph.tween.Tweens;
import org.jusecase.scenegraph.tween.animations.QuadraticOut;
import org.jusecase.scenegraph.tween.properties.FloatProperty;
import org.jusecase.ui.Ui;
import org.jusecase.ui.elements.Button;
import org.jusecase.ui.elements.Element;
import org.jusecase.ui.signal.OnHover;
import org.jusecase.ui.signal.OnResize;
import org.jusecase.ui.style.ButtonStyle;
import org.jusecase.ui.touch.TouchEvent;

import javax.inject.Inject;

@Component
public class CardsTester implements Application, OnResize {

    @Inject ApplicationBackend applicationBackend;
    @Inject Timer timer;

    private Ui ui = new Ui();
    private Tweens tweens = new Tweens();
    private Cards cards;

    public static void main(String[] args) {
        new LwjglApplicationBackend(CardsTester.class).start();
    }

    @Override
    public void init() {
        initStyles();

        cards = new Cards();
        ui.add(cards);

        Button button = new Button();
        button.setSize(200, 50);
        button.onClick.add(e -> cards.addCard());
        ui.add(button);

        applicationBackend.onResize().add(this);
        onResize(applicationBackend.getWidth(), applicationBackend.getHeight());
    }

    private void initStyles() {
        ButtonStyle buttonStyle = new ButtonStyle();
        buttonStyle.active.color = new Color("#eee", 0.5f);
        buttonStyle.hovered.color = new Color("#0e0", 0.5f);
        buttonStyle.pressed.color = new Color("#fff", 0.5f);
        ui.setDefaultStyle(Button.class, buttonStyle);
    }

    @Override
    public void process(TouchEvent touchEvent) {
        ui.process(touchEvent);
    }

    @Override
    public void update() {
        tweens.update(timer.dt());
    }

    @Override
    public void render(Renderer renderer) {
        renderer.render(ui);
    }

    @Override
    public void dispose() {
    }

    @Override
    public void onResize(int width, int height) {
        cards.setX(width * 0.5f);
        cards.setY(height * 0.95f);

        cards.layout();
    }

    private class Card extends Element {
        private Quad quad;
        private boolean selected;
        private boolean justDeselected;

        public Card() {
            quad = new Quad();
            add(quad);
        }

        public void setColor(Color color) {
            quad.setColor(color);
        }

        public boolean isSelected() {
            return selected;
        }

        public void setSelected(boolean selected) {
            this.selected = selected;
        }

        @Override
        public Node2d setSize(float width, float height) {
            quad.setSize(width, height);
            return super.setSize(width, height);
        }

        public boolean isJustDeselected() {
            return justDeselected;
        }

        public void setJustDeselected(boolean justDeselected) {
            this.justDeselected = justDeselected;
        }

        public float getAlpha() {
            return quad.getColor().a;
        }

        public void setAlpha(float alpha) {
            quad.getColor().a = alpha;
        }
    }

    private class Cards extends Node2d implements OnHover {

        public void addCard() {
            CardsTester.Card card = new CardsTester.Card();
            card.setSize(getCardWidth(), getCardHeight());
            card.setPivot(0.5f, 1.0f);
            card.setColor(new Color().randomHue());
            card.onHover.add(this);
            add(card);

            layout();
        }

        private float getCardWidth() {
            return 150;
        }

        private float getCardHeight() {
            return 200;
        }

        public void layout() {
            float distanceX = 0.8f * getCardWidth();
            float startX = -0.5f * distanceX * getChildCount(Card.class) + 0.5f * distanceX;

            visitAllDirectChildren(Card.class, (c, index) -> layoutCard(c, index, startX, distanceX));
        }

        private void layoutCard(Card card, int index, float startX, float distanceX) {
            float duration = 1.0f;
            float alpha = 1.0f;

            float targetX = startX + distanceX * index;

            float normalizedX = 2.0f * targetX / applicationBackend.getWidth();
            float targetY = normalizedX * normalizedX * 100; // TODO constant

            float targetRotation = -normalizedX * 20;

            if (card.isSelected()) {
                targetY -= 20; // TODO constant
                targetRotation *= 0.4f;
                duration = 0.2f;
            }

            if (card.isJustDeselected()) {
                duration = 0.4f;
            }

            Card sibling = getSibling(card, -1, Card.class);
            if (sibling != null && sibling.isSelected()) {
                alpha = 0.2f;
            }

            tweens.tween(card)
                    .duration(duration)
                    .animation(QuadraticOut.animation)
                    .property(new FloatProperty(card.getX(), targetX, card::setX))
                    .property(new FloatProperty(card.getY(), targetY, card::setY))
                    .property(new FloatProperty(card.getRotation(), targetRotation, card::setRotation))
                    .property(new FloatProperty(card.getAlpha(), alpha, card::setAlpha))
            ;
        }

        @Override
        public void onHover(Element element, boolean started) {
            Card card = (Card)element;
            card.setSelected(started);
            card.setJustDeselected(!started);
            layout();
        }
    }
}
