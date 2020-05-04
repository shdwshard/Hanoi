package com.weirdgeeks;

import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Node;
import com.weirdgeeks.model.Rules;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

public class GameStats extends AbstractAppState {

    private Rules rules;
    private Application app;
    private Node guiNode;
    private BitmapFont guiFont;
    BitmapText time;
    BitmapText moves;
    BitmapText win;

    public void setRules(Rules rules) {
        this.rules = rules;
    }

    @Override
    public void update(float tpf) {
        moves.setText("Moves: " + rules.getMoves());
        time.setText("Time: " + humanReadableFormat(Duration.from(rules.getElapsedTime())));
        win.setText(rules.isWin(app.pegs) ? "You Win!": "Not there yet");
    }

    private static String humanReadableFormat(Duration duration) {
        return String.format("%sh %sm %ss",
                duration.toHours(),
                duration.toMinutes() - TimeUnit.HOURS.toMinutes(duration.toHours()),
                duration.getSeconds() - TimeUnit.MINUTES.toSeconds(duration.toMinutes()));
    }
    @Override
    public void initialize(AppStateManager stateManager, com.jme3.app.Application app) {
        super.initialize(stateManager, app);
        this.app = (Application)app;

        if (app != null) {
            SimpleApplication simpleApp = (SimpleApplication)app;
            if (guiNode == null) {
                guiNode = simpleApp.getGuiNode();
            }
            if (guiFont == null ) {
                guiFont = app.getAssetManager().loadFont("Interface/Fonts/Console.fnt");
            }
        }

        if (guiNode == null) {
            throw new RuntimeException( "No guiNode specific and cannot be automatically determined." );
        }

        if (guiFont == null) {
            guiFont = app.getAssetManager().loadFont("Interface/Fonts/Default.fnt");
        }

        time = new BitmapText(guiFont, false);
        moves = new BitmapText(guiFont, false);
        win = new BitmapText(guiFont, false);
        time.setLocalTranslation(50, 3 * time.getLineHeight(),0);
        moves.setLocalTranslation(50, 4 * time.getLineHeight(),0);
        win.setLocalTranslation(50,8 * time.getLineHeight(),0);
        time.setColor(ColorRGBA.White);
        moves.setColor(ColorRGBA.White);
        win.setColor(ColorRGBA.White);
        guiNode.attachChild(time);
        guiNode.attachChild(win);
        guiNode.attachChild(moves);

        win.setSize(20f);
    }
}
