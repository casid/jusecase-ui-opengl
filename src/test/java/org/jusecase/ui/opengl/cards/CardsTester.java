package org.jusecase.ui.opengl.cards;

import org.jusecase.Application;
import org.jusecase.ApplicationBackend;
import org.jusecase.inject.Component;
import org.jusecase.inject.Injector;
import org.jusecase.scenegraph.color.Color;
import org.jusecase.scenegraph.render.Renderer;
import org.jusecase.scenegraph.texture.TextureAtlas;
import org.jusecase.scenegraph.texture.TextureAtlasLoader;
import org.jusecase.scenegraph.time.Timer;
import org.jusecase.scenegraph.tween.Tweens;
import org.jusecase.ui.Ui;
import org.jusecase.ui.elements.Button;
import org.jusecase.ui.input.Event;
import org.jusecase.ui.opengl.LwjglApplicationBackend;
import org.jusecase.ui.signal.OnResize;
import org.jusecase.ui.style.ButtonStyle;

import javax.inject.Inject;

@Component
public class CardsTester implements Application, OnResize {

    @Inject
    private ApplicationBackend applicationBackend;
    @Inject
    private Timer timer;
    @Inject
    private TextureAtlasLoader textureAtlasLoader;

    private Ui ui = new Ui();
    private Tweens tweens = new Tweens();
    private Cards cards;

    public static void main(String[] args) {
        new LwjglApplicationBackend(CardsTester.class).start();
    }

    @Override
    public void init() {
        Injector.getInstance().add(tweens);
        Injector.getInstance().add(textureAtlasLoader.load("images/_theme@2x.xml"));

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
    public void process(Event event) {
        ui.process(event);
    }

    @Override
    public void update() {
        cards.update();
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

        cards.needsLayout();
    }
}
