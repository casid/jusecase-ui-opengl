package org.jusecase.ui.opengl.cards;

import org.jusecase.ApplicationBackend;
import org.jusecase.inject.Component;
import org.jusecase.scenegraph.node2d.Node2d;
import org.jusecase.scenegraph.tween.Tweens;
import org.jusecase.scenegraph.tween.animations.QuadraticOut;
import org.jusecase.scenegraph.tween.properties.FloatProperty;
import org.jusecase.ui.elements.Element;
import org.jusecase.ui.input.ScrollEvent;
import org.jusecase.ui.signal.OnHoverListener;
import org.jusecase.ui.signal.OnScrollListener;
import org.jusecase.ui.signal.OnTouchListener;
import org.jusecase.ui.input.TouchEvent;
import org.jusecase.ui.input.TouchPhase;

import javax.inject.Inject;


@Component
public class Cards extends Node2d implements OnHoverListener, OnTouchListener, OnScrollListener {

    private static final float BASE_DURATION = 0.8f;
    private static final float SELECT_DURATION = 0.18f;
    private static final float DESELECT_DURATION = 0.3f;

    @Inject
    private ApplicationBackend applicationBackend;
    @Inject
    private Tweens tweens;

    private boolean needsLayout;

    public void addCard() {
        Card card = new Card();
        card.setPosition(applicationBackend.getWidth(), 0);
        card.setRotation(-60);
        card.setSize(getCardWidth(), getCardHeight());
        card.setPivot(0.5f, 1.0f);
        card.setIndex(getChildCount(Card.class));
        card.onHover.add(this);
        card.onTouch.add(this);
        card.onScroll.add(this);
        add(card);

        needsLayout();
    }

    private float getCardWidth() {
        return 621 * 0.6f;
    }

    private float getCardHeight() {
        return 826 * 0.6f;
    }

    public void needsLayout() {
        needsLayout = true;
    }

    public void update() {
        if (needsLayout) {
            layout();
            needsLayout = false;
        }
    }

    private void layout() {
        float distanceX = 0.8f * getCardWidth();
        float startX = -0.5f * distanceX * getChildCount(Card.class) + 0.5f * distanceX;

        visitAllDirectChildren(Card.class, c -> layoutCard(c, startX, distanceX));
        visitAllDirectChildren(Card.class, c -> c.setJustDeselected(false));
    }

    private void layoutCard(Card card, float startX, float distanceX) {
        if (card.isGrabbed()) {
            return;
        }

        float duration = BASE_DURATION;
        float scale = 1.0f;

        float targetX = startX + distanceX * card.getIndex();

        float normalizedX = 2.0f * targetX / applicationBackend.getWidth();
        float targetY = normalizedX * normalizedX * 0.5f * getCardHeight();

        float targetRotation = -normalizedX * 20;

        if (card.isSelected()) {
            targetY -= 0.1f * getCardHeight();
            targetRotation *= 0.4f;
            duration = SELECT_DURATION;
            scale = 1.25f;
        } else if (card.isJustDeselected()) {
            duration = DESELECT_DURATION;
        } else {
            Card sibling = getSibling(card, -1, Card.class);
            if (sibling != null) {
                if (sibling.isSelected()) {
                    targetX += 0.25f * getCardWidth();
                    duration = SELECT_DURATION;
                }
                if (sibling.isJustDeselected()) {
                    duration = DESELECT_DURATION;
                }
            }
        }

        tweens.tween(card)
                .duration(duration)
                .animation(QuadraticOut.animation)
                .property(new FloatProperty(card.getX(), targetX, card::animateX))
                .property(new FloatProperty(card.getY(), targetY, card::animateY))
                .property(new FloatProperty(card.getRotation(), targetRotation, card::setRotation))
                .property(new FloatProperty(card.getScaleX(), scale, card::setScale))
        ;
    }

    @Override
    public void onHover(Element element, boolean started) {
        Card card = (Card) element;
        card.setSelected(started);
        card.setJustDeselected(!started);
        needsLayout();
    }

    @Override
    public void onTouch(TouchEvent touchEvent) {
        Card card = (Card) touchEvent.element;

        if (touchEvent.phase == TouchPhase.Begin) {
            card.setGrabbed(true);
        } else if (touchEvent.phase == TouchPhase.End) {
            card.setGrabbed(false);
            card.setSelected(false);
            card.setJustDeselected(true);
            needsLayout();
        } else if (touchEvent.phase == TouchPhase.Move && card.isGrabbed()) {
            card.setX(card.getX() + touchEvent.deltaX);
            card.setY(card.getY() + touchEvent.deltaY);
        }
    }

    @Override
    public void onScroll(ScrollEvent scrollEvent) {
        System.out.println(scrollEvent); // TODO
    }
}
