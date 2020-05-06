package com.weirdgeeks;

import com.jme3.app.FlyCamAppState;
import com.jme3.app.SimpleApplication;
import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.effect.ParticleEmitter;
import com.jme3.effect.ParticleMesh;
import com.jme3.effect.shapes.EmitterMeshFaceShape;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.InputListener;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.light.Light;
import com.jme3.material.Material;
import com.jme3.math.*;
import com.jme3.post.Filter;
import com.jme3.post.FilterPostProcessor;
import com.jme3.post.filters.BloomFilter;
import com.jme3.post.filters.FXAAFilter;
import com.jme3.post.ssao.SSAOFilter;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Sphere;
import com.jme3.shadow.DirectionalLightShadowFilter;
import com.jme3.shadow.DirectionalLightShadowRenderer;
import com.jme3.system.AppSettings;
import com.weirdgeeks.model.Disc;
import com.weirdgeeks.model.Peg;
import com.weirdgeeks.model.Rules;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Application extends SimpleApplication {
    Logger log = LoggerFactory.getLogger(SimpleApplication.class);
    List<Peg> pegs = new ArrayList<>();
    List<Disc> discs = new ArrayList<>();
    Peg selectedPeg;
    Pattern discPattern = Pattern.compile("^Disc(\\d+)$");
    Pattern pegPattern = Pattern.compile("^Peg(\\d+)$");
    Rules rules = new Rules();
    ParticleEmitter emitter;

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
                    log.info("* Collision #" + i);
                    log.info("  You shot {} at {}, {} wu away.",hit,pt,dist);
                }
                // 5. Use the results (we mark the hit object)
                if (results.size() > 0) {
                    // The closest collision point is what was truly hit:
                    CollisionResult closest = results.getClosestCollision();
                    log.info("Closest hit: {}",closest.getGeometry().getName());
                    Matcher pegMatcher = pegPattern.matcher(closest.getGeometry().getName());
                    Matcher discMatcher = discPattern.matcher(closest.getGeometry().getName());
                    Peg tappedPeg = null;
                    if (discMatcher.matches()) {
                        tappedPeg = discs.get(Integer.parseInt(discMatcher.group(1)) - 1).getLocation();
                    } else if (pegMatcher.matches()) {
                        tappedPeg = pegs.get(Integer.parseInt(pegMatcher.group(1)) -1);
                    }

                    if (tappedPeg != null) {
                        // Drop target
                        if (selectedPeg != null) {
                            // Undo peg selection
                            if (selectedPeg == tappedPeg) {
                                selectedPeg = null;
                                log.info("De-selected peg {}", tappedPeg.getNumber());
                            } else {
                                Disc topDisc = selectedPeg.getDiscs().get(selectedPeg.getDiscs().size() -1);
                                if (rules.canPlace(topDisc, tappedPeg)) {
                                    selectedPeg.getDiscs().remove(topDisc);
                                    tappedPeg.getDiscs().add(topDisc);
                                    topDisc.setLocation(tappedPeg);
                                    selectedPeg = null;
                                    rules.doMove();
                                    log.info("Moved disc {} to peg {}", topDisc.getSize(), tappedPeg.getNumber());
                                }
                            }
                        } else if (tappedPeg.getDiscs().size() > 0) {
                            selectedPeg = tappedPeg;
                            log.info("Selected peg {}",selectedPeg.getNumber());
                        }
                    }
                }
            }
    };

    public Application() {
        super(new GameStats());
    }

    public static void main(String[] args) {

        Application app = new Application();

        AppSettings settings = new AppSettings(true);
        settings.setTitle("Towers of Hanoi");
        settings.setVSync(true);
        settings.setResolution(800,600);
        app.setSettings(settings);
        app.setShowSettings(false);
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
        pegs.get(0).getDiscs().addAll(discs);
        pegs.get(0).getDiscs().sort(Comparator.comparingInt(Disc::getSize).reversed());

        // Lights
        List<Light> lights = Arrays.asList(
                new AmbientLight(new ColorRGBA(0.2f,0.8f,0.8f,1f)),
                new DirectionalLight(new Vector3f(-1f,-1f,1f).normalize(),new ColorRGBA(0.8f,0.8f,0.8f,1f)),
                new DirectionalLight(new Vector3f(-1f,-1f,-1f).normalize(),new ColorRGBA(0.4f,0.4f,0.4f,1f))
        );
        lights.forEach(rootNode::addLight);

        // Shadows
        final int SHADOWMAP_SIZE=1024;
        rootNode.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);
        lights.stream().filter(light -> light instanceof DirectionalLight).forEach(light -> {
            DirectionalLightShadowRenderer dlsr = new DirectionalLightShadowRenderer(assetManager, SHADOWMAP_SIZE, 3);
            dlsr.setShadowIntensity((float)Math.sqrt(light.getColor().toVector3f().lengthSquared()/3)/2);
            dlsr.setLight((DirectionalLight)light);
            viewPort.addProcessor(dlsr);
        });

        FilterPostProcessor fpp = new FilterPostProcessor(assetManager);
        SSAOFilter ssaoFilter = new SSAOFilter(12.94f, 43.92f, 0.33f, 0.61f);
        fpp.addFilter(ssaoFilter);
        viewPort.addProcessor(fpp);

        // Camera setup
        stateManager.detach( stateManager.getState(FlyCamAppState.class) );

        cam.setLocation(new Vector3f(5.6796985f, 3.3253722f, 0f));
        cam.setRotation(new Quaternion(0.15558477f, -0.68930024f, 0.15535718f, 0.69030625f));


        inputManager.addMapping("Pick",new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
        inputManager.addListener(actionListener, "Pick");

        glowMaterial = new Material(getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        glowMaterial.setColor("Color", ColorRGBA.DarkGray);
        glowMaterial.setColor("GlowColor", ColorRGBA.White);
        glowProcessor=new FilterPostProcessor(assetManager);
        BloomFilter bloom= new BloomFilter(BloomFilter.GlowMode.Objects);
        glowProcessor.addFilter(bloom);
        bloom.setBlurScale(1f);
        stateManager.getState(GameStats.class).setRules(rules);
    }

    FilterPostProcessor glowProcessor;
    Material glowMaterial;
    Material prevMaterial;

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
        spatial.setShadowMode(RenderQueue.ShadowMode.Inherit);
    }


    float discHeight = 0.2f;

    @Override
    public void simpleUpdate(float tpf) {
        // Step1: Update disc locations
        discs.forEach(disc -> {
            Vector3f peg = disc.getLocation().getSpatial().getLocalTranslation();
            int discNum = disc.getLocation().getDiscs().indexOf(disc);
            disc.getSpatial().setLocalTranslation(peg.x,discHeight * discNum + discHeight, peg.z);
        });
        // Step2: Selection target
        if (selectedPeg == null) {
            if (viewPort.getProcessors().contains(glowProcessor)) {
                discs.forEach(disc -> {;
                    if (((Geometry)disc.getSpatial()).getMaterial() == glowMaterial) {
                        disc.getSpatial().setMaterial(prevMaterial);
                    }
                });
                viewPort.removeProcessor(glowProcessor);
            }
        } else {
            if (!viewPort.getProcessors().contains(glowProcessor)) {
                Disc disc = selectedPeg.getDiscs().get(selectedPeg.getDiscs().size() -1);
                viewPort.addProcessor(glowProcessor);
                prevMaterial = ((Geometry)disc.getSpatial()).getMaterial();
                disc.getSpatial().setMaterial(glowMaterial);
            }
        }
    }
}
