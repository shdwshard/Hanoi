package com.weirdgeeks;

import com.jme3.app.FlyCamAppState;
import com.jme3.app.SimpleApplication;
import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.InputListener;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.*;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Sphere;
import com.jme3.system.AppSettings;
import com.weirdgeeks.model.Disc;
import com.weirdgeeks.model.Peg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Application extends SimpleApplication {
    Logger log = LoggerFactory.getLogger(SimpleApplication.class);
    List<Peg> pegs = new ArrayList<>();
    List<Disc> discs = new ArrayList<>();
    private Geometry mark;

    private InputListener actionListener = (ActionListener) (name, isPressed, tpf) -> {
            if (name.equals("Pick") && !isPressed) {
                CollisionResults results = new CollisionResults();
                Vector2f click2d = inputManager.getCursorPosition().clone();
                Vector3f click3d = cam.getWorldCoordinates(
                        click2d, 0f).clone();
                Vector3f dir = cam.getWorldCoordinates(
                        click2d, 1f).subtractLocal(click3d).normalizeLocal();
                Ray ray = new Ray(click3d, dir);
                Stream.concat(pegs.stream().map(Peg::getSpatial),discs.stream().map(Disc::getSpatial)).forEach(node -> node.collideWith(ray,results));
                System.out.println("----- Collisions? " + results.size() + "-----");
                for (int i = 0; i < results.size(); i++) {
                    // For each hit, we know distance, impact point, name of geometry.
                    float dist = results.getCollision(i).getDistance();
                    Vector3f pt = results.getCollision(i).getContactPoint();
                    String hit = results.getCollision(i).getGeometry().getName();
                    System.out.println("* Collision #" + i);
                    System.out.println("  You shot " + hit + " at " + pt + ", " + dist + " wu away.");
                }
                // 5. Use the results (we mark the hit object)
                if (results.size() > 0) {
                    // The closest collision point is what was truly hit:
                    CollisionResult closest = results.getClosestCollision();
                    // Let's interact - we mark the hit with a red dot.
                    mark.setLocalTranslation(closest.getContactPoint());
                    rootNode.attachChild(mark);
                } else {
                    // No hits? Then remove the red mark.
                    rootNode.detachChild(mark);
                }
            }
    };

    public static void main(String[] args) {

        Application app = new Application();

        AppSettings settings = new AppSettings(true);
        settings.setTitle("My Awesome Game");
        app.setSettings(settings);

        app.start();

    }

    @Override
    public void simpleInitApp() {

        Box b = new Box(1, 1, 1);
        Spatial hanoi = assetManager.loadModel("Hanoi.gltf");

        log.info(((Node)hanoi).getChildren().stream().map(node -> node.getName() + "(" + node.getLocalTranslation() + ")").collect(Collectors.joining(", ")));


        hanoi.rotate(0,-90,0);
        ((Node)hanoi).getChildren().stream()
                .peek(this::doMarkup)
                .forEach(rootNode::attachChild);

        discs.sort(Comparator.comparingInt(Disc::getSize));
        pegs.sort(Comparator.comparingInt(Peg::getNumber));

        discs.forEach(disc -> disc.setLocation(pegs.get(0)));

        Arrays.asList(
                new AmbientLight(new ColorRGBA(0.2f,0.2f,0.2f,1f)),
                new DirectionalLight(new Vector3f(-1f,-1f,1f).normalize(),new ColorRGBA(0.8f,0.8f,0.8f,1f)),
                new DirectionalLight(new Vector3f(-1f,-1f,-1f).normalize(),new ColorRGBA(0.4f,0.4f,0.4f,1f))
        ).forEach(rootNode::addLight);

        stateManager.detach( stateManager.getState(FlyCamAppState.class) );
        cam.setLocation(new Vector3f(5.6796985f, 3.3253722f, 0f));
        cam.setRotation(new Quaternion(0.15558477f, -0.68930024f, 0.15535718f, 0.69030625f));

        inputManager.addMapping("Pick",new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
        inputManager.addListener(actionListener, "Pick");
        initMark();
    }

    Pattern discPattern = Pattern.compile("^Disc(\\d+)$");
    Pattern pegPattern = Pattern.compile("^Peg(\\d+)$");
    /** A red ball that marks the last spot that was "hit" by the "shot". */
    protected void initMark() {
        Sphere sphere = new Sphere(30, 30, 0.2f);
        mark = new Geometry("BOOM!", sphere);
        Material mark_mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mark_mat.setColor("Color", ColorRGBA.Red);
        mark.setMaterial(mark_mat);
    }
    private void doMarkup(Spatial spatial) {
        Matcher discMatcher = discPattern.matcher(spatial.getName());
        Matcher pegMatcher = pegPattern.matcher(spatial.getName());
        if (discMatcher.matches()) {
            Integer discNumber = Integer.valueOf(discMatcher.group(1));
            Disc disc = new Disc(discNumber, spatial);
            discs.add(disc);
        } else if (pegMatcher.matches()) {
            Integer pegNumber = Integer.valueOf(pegMatcher.group(1));
            Peg peg = new Peg(pegNumber, spatial);
            pegs.add(peg);
        }
    }

    float color = 0;
    @Override
    public void simpleUpdate(float tpf) {
        //TODO: add update code
//        rootNode.getChildren().stream().filter(node -> node.getUserData("Disc") != null)
//                .peek(node -> node.getUserData("Disc"))

    }
}
