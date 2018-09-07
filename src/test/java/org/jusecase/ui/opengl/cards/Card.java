package org.jusecase.ui.opengl.cards;

import org.jusecase.inject.Component;
import org.jusecase.scenegraph.color.Color;
import org.jusecase.scenegraph.node2d.Image;
import org.jusecase.scenegraph.node2d.Node2d;
import org.jusecase.scenegraph.node2d.Quad;
import org.jusecase.scenegraph.texture.TextureAtlas;
import org.jusecase.ui.elements.Element;

import javax.inject.Inject;

@Component
public class Card extends Element {
    @Inject
    private TextureAtlas textureAtlas;

    private Image background;
    private int index;
    private boolean selected;
    private boolean justDeselected;
    private boolean grabbed;

    public Card() {
        background = new Image(textureAtlas.get("card"));
        add(background);
    }

    public void animateX(float x) {
        if (!grabbed) {
            setX(x);
        }
    }

    public void animateY(float y) {
        if (!grabbed) {
            setY(y);
        }
    }

    public boolean isGrabbed() {
        return grabbed;
    }

    public void setColor(Color color) {
        background.setColor(color);
    }

    public boolean isSelected() {
        return selected;
    }

    public void setGrabbed(boolean grabbed) {
        this.grabbed = grabbed;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    @Override
    public Node2d setSize(float width, float height) {
        background.setSize(width, height);
        return super.setSize(width, height);
    }

    public boolean isJustDeselected() {
        return justDeselected;
    }

    public void setJustDeselected(boolean justDeselected) {
        this.justDeselected = justDeselected;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }
}


