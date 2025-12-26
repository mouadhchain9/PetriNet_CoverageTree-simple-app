package com.svs.ui.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.layout.Pane;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.stage.Modality;
import javafx.stage.FileChooser;
import javafx.animation.PathTransition;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import com.svs.model.*;
import com.svs.ui.nodes.*;
import com.svs.dto.*;

import java.io.File;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.Writer;


import javafx.util.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

public class EditorController {

    @FXML private Button btnAddPlace;
    @FXML private Button btnAddTransition;
    @FXML private Button btnLink;
    @FXML private Button btnSimulate;
    @FXML private Button btnSimulateStop;

    @FXML private Pane canvas;
    @FXML private Label simulationStatus;
    @FXML private ListView<String> transitionList;
    // @FXML private ListView<String> markingEvolution;
    @FXML private Button btnDeleteNode;
    @FXML private Button btnDeleteAll;
    @FXML private Button btnSave;
    @FXML private Button btnLoad;


    // Saving directory Memory
    private File lastDirectory = null;

    // Core Petri net
    private final PetriNet net = new PetriNet();
    private Marking currentMarking;

    // Flags
    private boolean deleteMode = false;
    private boolean linkingMode = false;
    private boolean simulationMode = false;
    private Node linkStartNode = null;     // was "Object" type


    // All drawn arcs
    private final List<ArcLine> arcs = new ArrayList<>();


    // ------------------------------------------------------------
    // Initialization (called automatically from FXML)
    // ------------------------------------------------------------
    @FXML
    private void initialize() {
        simulationStatus.setText("(not started)");
        setupButtonHandlers();
    }


    // ------------------------------------------------------------
    // Button handlers
    // ------------------------------------------------------------
    private void setupButtonHandlers() {
        btnDeleteNode.setDisable(true);
        btnDeleteAll.setDisable(true);
        btnSimulateStop.setDisable(true);
        btnSave.setOnAction(e -> saveToFile());
        btnLoad.setOnAction(e -> loadFromFile());
        btnAddPlace.setOnAction(e -> addPlaceNode());
        btnAddTransition.setOnAction(e -> addTransitionNode());

        btnLink.setOnAction(e -> {
            linkingMode = !linkingMode;

            btnLink.setStyle(linkingMode ? "-fx-background-color: lightgreen;" : "");
            linkStartNode = null;
            canvas.setCursor(linkingMode ? Cursor.CROSSHAIR : Cursor.DEFAULT);

            simulationStatus.setText(
                    linkingMode ? "Link mode: click Place → Transition" : "(not linking)"
            );
        });

        btnDeleteNode.setOnAction(e -> {
            if (simulationMode) return;

            deleteMode = !deleteMode;
            linkingMode = false;

            btnDeleteNode.setStyle(deleteMode ? "-fx-background-color: salmon;" : "");
            btnLink.setStyle("");

            simulationStatus.setText(
                deleteMode ? "Delete mode: click a node" : "(not deleting)"
            );

            canvas.setCursor(deleteMode ? Cursor.DISAPPEAR : Cursor.DEFAULT);
        });

        btnSimulate.setOnAction(e -> openMarkingPopup());
        btnSimulateStop.setOnAction(e -> stopSimulation());
        btnDeleteAll.setOnAction(e -> deleteAll());


    }


    // ------------------------------------------------------------
    // Node creation
    // ------------------------------------------------------------
    private void addPlaceNode() {
        Place p = new Place();
        net.addPlace(p);

        PlaceNode node = new PlaceNode(p);
        enableNodeDrag(node);
        canvas.getChildren().add(node);

        node.relocate(100 + net.getPlaces().size() * 30, 100);

        btnDeleteNode.setDisable(false);
        btnDeleteAll.setDisable(false);
    }

    private void addTransitionNode() {
        Transition t = new Transition();
        net.addTransition(t);

        TransitionNode node = new TransitionNode(t);
        enableNodeDrag(node);
        canvas.getChildren().add(node);

        node.relocate(200 + net.getTransitions().size() * 30, 100);

        btnDeleteNode.setDisable(false);
        btnDeleteAll.setDisable(false);
    }


    // ------------------------------------------------------------
    // Linking logic (CLEAN + WORKING)
    // ------------------------------------------------------------
    private void handleLinkClick(Node uiNode) { // was "Object" type
        if (!linkingMode) return;

        if (linkStartNode == null) {
            linkStartNode = uiNode;
            simulationStatus.setText("Select target...");
            return;
        }

        Object src = linkStartNode;
        Object dst = uiNode;

        // Validate: must be Place → Transition OR Transition → Place
        boolean valid =
                (src instanceof PlaceNode && dst instanceof TransitionNode) ||
                (src instanceof TransitionNode && dst instanceof PlaceNode);

        if (!valid) {
            simulationStatus.setText("Invalid link (must be Place ↔ Transition)");
            linkStartNode = null;
            return;
        }

        // Normalize
        PlaceNode pNode;
        TransitionNode tNode;
        boolean isPre;

        if (src instanceof PlaceNode) {
            pNode = (PlaceNode) src;
            tNode = (TransitionNode) dst;
            isPre = true;     // Place → Transition means pre arc
        } else {
            pNode = (PlaceNode) dst;
            tNode = (TransitionNode) src;
            isPre = false;    // Transition → Place means post arc
        }

        // Update Petri net model (edited for weight setting)
        // int weight = 1;
        // if (isPre)
        //     tNode.getTransition().addPreArc(pNode.getPlace(), weight);
        // else
        //     tNode.getTransition().addPostArc(pNode.getPlace(), weight);
        Integer weight = showArcWeightDialog();
        if (weight == null) {
            simulationStatus.setText("Arc creation cancelled");
            linkStartNode = null;
            linkingMode = false;
            btnLink.setStyle("");
            canvas.setCursor(Cursor.DEFAULT);
            return;
        }

        if (isPre)
            tNode.getTransition().addPreArc(pNode.getPlace(), weight);
        else
            tNode.getTransition().addPostArc(pNode.getPlace(), weight);
        

        // Draw arc visually
        ArcLine arcLine;
        if (isPre) {
            arcLine = new ArcLine(pNode, tNode); // Place → Transition
        } else {
            arcLine = new ArcLine(tNode, pNode); // Transition → Place
        }

        arcLine.setWeight(weight);
        canvas.getChildren().add(arcLine);
        arcs.add(arcLine);


        simulationStatus.setText("Link created ✔");
        // System.out.println(tNode.getTransition());

        linkStartNode = null;
        linkingMode = false;
        btnLink.setStyle("");
        canvas.setCursor(Cursor.DEFAULT);
    }


    // ------------------------------------------------------------
    // wewight setting popup
    // ------------------------------------------------------------
    private Integer showArcWeightDialog() {

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/arc_weight_popup.fxml")
            );

            Parent root = loader.load();

            ArcWeightPopupController controller = loader.getController();

            Stage dialog = new Stage();
            controller.setStage(dialog);

            dialog.setTitle("Set Arc Weight");
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setScene(new Scene(root));
            dialog.showAndWait();

            return controller.getResult();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // ------------------------------------------------------------
    // Enable dragging + linking clicks on each node
    // ------------------------------------------------------------
    private void enableNodeDrag(Node node) {
        final double[] delta = new double[2];

        // node.setOnMousePressed(e -> {
        //     if (simulationMode) {
        //         // Handle firing transitions instead of dragging
        //         if (node instanceof TransitionNode) {
        //             fireTransition((TransitionNode) node);
        //         }
        //         e.consume();
        //         return;
        //     }

        //     if (linkingMode) {
        //         handleLinkClick(node);
        //         e.consume();
        //         return;
        //     }

        //     delta[0] = e.getSceneX() - node.getLayoutX();
        //     delta[1] = e.getSceneY() - node.getLayoutY();
        // });
        node.setOnMousePressed(e -> {

            if (deleteMode) {
                deleteNode(node);
                e.consume();
                return;
            }

            if (simulationMode) {
                if (node instanceof TransitionNode) {
                    fireTransition((TransitionNode) node);
                }
                e.consume();
                return;
            }

            if (linkingMode) {
                handleLinkClick(node);
                e.consume();
                return;
            }

            delta[0] = e.getSceneX() - node.getLayoutX();
            delta[1] = e.getSceneY() - node.getLayoutY();
        });


        node.setOnMouseDragged(e -> {
            if (linkingMode || simulationMode) return;

            node.relocate(e.getSceneX() - delta[0], e.getSceneY() - delta[1]);
            updateArcs();
        });
    }


    private void fireTransition(TransitionNode tNode) {
        if (currentMarking == null) return;

        Transition t = tNode.getTransition();
        if (!t.isEnabled(net, currentMarking)) {
            simulationStatus.setText(t.getName() + " is not enabled!");
            return;
        }
        animateFiring(tNode);

        currentMarking = t.fire(net, currentMarking);
        // markingEvolution.getItems().add(currentMarking.toString());
        // markingEvolution.getItems().add("Fired: " + t.getName() + currentMarking.toString());
        transitionList.getItems().add("Fired: " + t.getName() + " --> " +currentMarking.toString());
        // simulationStatus.setText("Fired: " + t.getName());
        updateEnabledTransitions();

        //enabled transition print for testing
        // List<Transition> enabled = net.getEnabledTransitions(currentMarking);             
        // for (Transition ts : enabled)
        //     System.out.println(ts.getName() + ",");
        /////////////////
    }

    //// edited animation into a better style.

    private void animateFiring(TransitionNode tNode) {
        animateArcs(tNode, true, () ->
            animateArcs(tNode, false, null)
        );
    }

    private void animateArcs(TransitionNode tNode,
                             boolean prePhase,
                             Runnable onFinished) {

        List<ArcLine> relevant = new ArrayList<>();

        for (ArcLine arc : arcs) {
            if (!arc.connects(tNode)) continue;
            if (prePhase && arc.isPreArcOf(tNode)) relevant.add(arc);
            if (!prePhase && arc.isPostArcOf(tNode)) relevant.add(arc);
        }

        if (relevant.isEmpty()) {
            if (onFinished != null) onFinished.run();
            return;
        }

        final int[] finishedCount = {0};

        for (ArcLine arc : relevant) {

            double sx = arc.getStartX();
            double sy = arc.getStartY();
            double ex = arc.getEndX();
            double ey = arc.getEndY();

            Circle dot = new Circle(5);
            dot.setStyle("-fx-fill: orange;");
            dot.setCenterX(sx);
            dot.setCenterY(sy);
            canvas.getChildren().add(dot);

            Line path = new Line(sx, sy, ex, ey);
            PathTransition pt = new PathTransition(
                    Duration.millis(400), path, dot
            );

            pt.setOnFinished(e -> {
                canvas.getChildren().remove(dot);

                finishedCount[0]++;
                if (finishedCount[0] == relevant.size() && onFinished != null) {
                    onFinished.run();
                }
            });

            pt.play();
        }
    }



    // ------------------------------------------------------------
    // Delete and Delete All Logic
    // ------------------------------------------------------------

    private void deleteNode(Node node) {

        // 1. Remove arcs connected to this node
        List<ArcLine> toRemove = new ArrayList<>();

        for (ArcLine arc : arcs) {
            if (arc.getSourceNode() == node || arc.getTargetNode() == node) {
                toRemove.add(arc);
            }
        }

        for (ArcLine arc : toRemove) {
            canvas.getChildren().remove(arc);
            arcs.remove(arc);
        }

        // 2. Remove from model
        if (node instanceof PlaceNode pn) {
            net.removePlace(pn.getPlace());
        }
        else if (node instanceof TransitionNode tn) {
            net.removeTransition(tn.getTransition());
        }

        // 3. Remove UI node
        canvas.getChildren().remove(node);

        // 4. Reset delete mode
        deleteMode = false;
        btnDeleteNode.setStyle("");
        canvas.setCursor(Cursor.DEFAULT);

        if (net.getPlaces().size()==0 &&
            net.getTransitions().size()==0){

            btnDeleteNode.setDisable(true);
            btnDeleteAll.setDisable(true);
        }

        simulationStatus.setText("Node deleted ✔");
    }

    private void deleteAll() {

        canvas.getChildren().clear();
        arcs.clear();

        net.clear();
        net.resetCounters();


        btnDeleteNode.setDisable(true);
        btnDeleteAll.setDisable(true);

        simulationStatus.setText("All nodes deleted");
    }


    // ------------------------------------------------------------
    // File Saving/Loading functionality
    // ------------------------------------------------------------
    // Saving part :

    private void saveToFile(File file) throws Exception {

        NetDTO dto = new NetDTO();

        // --- Places
        for (Node n : canvas.getChildren()) {
            if (n instanceof PlaceNode pn) {
                PlaceDTO p = new PlaceDTO();
                p.id = pn.getPlace().getName();
                p.x = pn.getLayoutX();
                p.y = pn.getLayoutY();
                dto.places.add(p);
            }
        }

        // --- Transitions
        for (Node n : canvas.getChildren()) {
            if (n instanceof TransitionNode tn) {
                TransitionDTO t = new TransitionDTO();
                t.id = tn.getTransition().getName();
                t.x = tn.getLayoutX();
                t.y = tn.getLayoutY();
                dto.transitions.add(t);
            }
        }

        // --- Arcs
        for (ArcLine arc : arcs) {
            ArcDTO a = new ArcDTO();
            a.from = extractId(arc.getSourceNode());
            a.to   = extractId(arc.getTargetNode());
            a.weight = arc.getWeight(); // updated
            dto.arcs.add(a);
        }

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try (Writer w = new FileWriter(file)) {
            gson.toJson(dto, w);
        }

        simulationStatus.setText("Saved ✔");
    }

    private String extractId(Node n) {
        if (n instanceof PlaceNode pn) return pn.getPlace().getName();
        if (n instanceof TransitionNode tn) return tn.getTransition().getName();
        throw new IllegalStateException("Unknown node type");
    }

    private void saveToFile() {
        FileChooser fc = createJsonFileChooser("Save Petri Net");
        File file = fc.showSaveDialog(canvas.getScene().getWindow());
        if (file == null) return;

        lastDirectory = file.getParentFile();

        try {
            saveToFile(file);
            simulationStatus.setText("Saved to " + file.getName());
        } catch (Exception ex) {
            ex.printStackTrace();
            simulationStatus.setText("Save failed");
        }
    }


    // Loading part :
    
    private void loadFromFile(File file) throws Exception {

        stopSimulation(); ///??? for later
        deleteAll();


        Gson gson = new Gson();
        NetDTO dto = gson.fromJson(new FileReader(file), NetDTO.class);

        Map<String, PlaceNode> placeMap = new HashMap<>();
        Map<String, TransitionNode> transitionMap = new HashMap<>();

        // --- Places
        for (PlaceDTO p : dto.places) {
            Place place = new Place();
            place.setName(p.id);
            net.addPlace(place);

            PlaceNode pn = new PlaceNode(place);
            pn.relocate(p.x, p.y);
            enableNodeDrag(pn);

            canvas.getChildren().add(pn);
            placeMap.put(p.id, pn);
        }

        // --- Transitions
        for (TransitionDTO t : dto.transitions) {
            Transition tr = new Transition();
            tr.setName(t.id);
            net.addTransition(tr);

            TransitionNode tn = new TransitionNode(tr);
            tn.relocate(t.x, t.y);
            enableNodeDrag(tn);

            canvas.getChildren().add(tn);
            transitionMap.put(t.id, tn);
        }

        // --- Arcs (MODEL FIRST, then UI)
        for (ArcDTO a : dto.arcs) {
            Node src = placeMap.containsKey(a.from)
                    ? placeMap.get(a.from)
                    : transitionMap.get(a.from);

            Node dst = placeMap.containsKey(a.to)
                    ? placeMap.get(a.to)
                    : transitionMap.get(a.to);

            if (src instanceof PlaceNode pn && dst instanceof TransitionNode tn)
                tn.getTransition().addPreArc(pn.getPlace(), a.weight);
            else if (src instanceof TransitionNode tn && dst instanceof PlaceNode pn)
                tn.getTransition().addPostArc(pn.getPlace(), a.weight);

            ArcLine arc = new ArcLine(src, dst);
            arc.setWeight(a.weight);
            canvas.getChildren().add(arc);
            arcs.add(arc);
        }

        simulationStatus.setText("Loaded ✔");
        
        btnDeleteNode.setDisable(false);
        btnDeleteAll.setDisable(false);
    }

    private void loadFromFile() {
        FileChooser fc = createJsonFileChooser("Load Petri Net");
        File file = fc.showOpenDialog(canvas.getScene().getWindow());
        if (file == null) return;

        lastDirectory = file.getParentFile();

        try {
            loadFromFile(file);
            simulationStatus.setText("Loaded " + file.getName());
        } catch (Exception ex) {
            ex.printStackTrace();
            simulationStatus.setText("Load failed");
        }
    }


    private FileChooser createJsonFileChooser(String title) {
        FileChooser fc = new FileChooser();
        fc.setTitle(title);
        fc.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Petri Net (*.json)", "*.json")
        );
        if (lastDirectory != null)
            fc.setInitialDirectory(lastDirectory);
        return fc;
    }


    // ------------------------------------------------------------
    // Update arc positions during drag
    // ------------------------------------------------------------
    private void updateArcs() {
        for (ArcLine arc : arcs) {
            arc.update();
        }
    }


    // ------------------------------------------------------------
    // Marking popup
    // ------------------------------------------------------------
    private void openMarkingPopup() {
        if (net.getPlaces().size()==0 ||
            net.getTransitions().size()==0) {
            simulationStatus.setText("Your graph is incomplete!!");
            return;
        }

        // if (arcs.size()==0) {
        //     // needs a more thoughrout validation
        //     simulationStatus.setText("your graph is not fully connected!!");

        //     return;
        // }
        // more detailed validation --> 
        for (Node n : canvas.getChildren()) {
            if (n instanceof TransitionNode tn) {
                int pre = tn.getTransition().getPre().size();
                int post = tn.getTransition().getPost().size();
                if(pre == 0 || post == 0){
                    simulationStatus.setText("your graph is not fully connected!!");
                    return;
                }
            }
        }

        try {
            FXMLLoader loader =
                    new FXMLLoader(getClass().getResource("/popup_init_marking.fxml"));
            Parent root = loader.load();

            PopupInitMarkingController popup = loader.getController();
            popup.setNet(net);

            Stage dialog = new Stage();
            popup.setStage(dialog);

            dialog.setTitle("Initial Marking");
            dialog.setScene(new Scene(root));
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.showAndWait();

            currentMarking = popup.getResultMarking();
            if (currentMarking != null) {
                simulationStatus.setText("Simulation ready");
                updateEnabledTransitions();
                // //enabled transition print for testing
                // List<Transition> enabled = net.getEnabledTransitions(currentMarking);             
                // for (Transition t : enabled)
                //     System.out.println(t.getName() + ",");
                // /////////////////
                simulationMode = true;  //
                btnSimulate.setDisable(true);
                btnSimulateStop.setDisable(false);
                btnAddPlace.setDisable(true);
                btnAddTransition.setDisable(true);
                btnLink.setDisable(true);
                btnDeleteNode.setDisable(true);
                btnDeleteAll.setDisable(true);
                btnSave.setDisable(true);
                btnLoad.setDisable(true);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    // ------------------------------------------------------------
    // Stop Simulation and reset marking
    // ------------------------------------------------------------

    private void stopSimulation() {

        currentMarking = null;
        simulationMode = false;

        transitionList.getItems().clear();
        // markingEvolution.getItems().clear();

        btnSimulate.setDisable(false);
        btnAddPlace.setDisable(false);
        btnAddTransition.setDisable(false);
        btnLink.setDisable(false);
        btnSimulateStop.setDisable(true);
        btnDeleteNode.setDisable(false);
        btnDeleteAll.setDisable(false);
        btnSave.setDisable(false);
        btnLoad.setDisable(false);

        for (Node n : canvas.getChildren()) {
            if (n instanceof TransitionNode tn)
                tn.highlight(false);
        }

    }

    private void updateEnabledTransitions() {
        if (currentMarking == null) return;

        // 1. Reset all transitions' highlight
        for (Node n : canvas.getChildren()) {
            if (n instanceof TransitionNode tn)
                tn.highlight(false);
        }

        // 2. Highlight only enabled transitions
        List<Transition> enabled = net.getEnabledTransitions(currentMarking);
        // transitionList.getItems().add(enabled.toString());
        for (Transition t : enabled) {
            // Find matching UI node
            for (Node n : canvas.getChildren()) {
                if (n instanceof TransitionNode tn && tn.getTransition() == t) {
                    tn.highlight(true);
                }
            }
        }
    }

}
