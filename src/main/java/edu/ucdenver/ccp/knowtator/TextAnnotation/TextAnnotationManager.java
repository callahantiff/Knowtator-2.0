package edu.ucdenver.ccp.knowtator.TextAnnotation;

import edu.ucdenver.ccp.knowtator.KnowtatorSelectionModel;
import edu.ucdenver.ccp.knowtator.KnowtatorView;
import edu.ucdenver.ccp.knowtator.iaa.AssertionRelationship;
import edu.ucdenver.ccp.knowtator.owl.OWLAPIDataExtractor;
import edu.ucdenver.ccp.knowtator.ui.KnowtatorTextPane;
import org.apache.log4j.Logger;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLObjectProperty;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import java.awt.*;
import java.util.*;

@SuppressWarnings({"unused", "MismatchedQueryAndUpdateOfCollection"})
public final class TextAnnotationManager {
    private static final Logger log = Logger.getLogger(KnowtatorView.class);

    private HashMap<String, Collection<TextAnnotation>> textAnnotations;
    private TextAnnotation selectedTextAnnotation;
    private KnowtatorView view;
    private KnowtatorSelectionModel selectionModel;
    public OWLAPIDataExtractor getDataExtractor() {
        return dataExtractor;
    }
    private OWLAPIDataExtractor dataExtractor;

    private ArrayList<AssertionRelationship> assertionRelationships;

    public TextAnnotationManager(KnowtatorView view) {
        this.view = view;
        dataExtractor = new OWLAPIDataExtractor(view.getOWLModelManager());
        selectedTextAnnotation = null;
        textAnnotations = new HashMap<>();
        assertionRelationships = new ArrayList<>();
    }

    public TextAnnotationManager() {
        dataExtractor = new OWLAPIDataExtractor(view.getOWLModelManager());
        textAnnotations = new HashMap<>();
        assertionRelationships = new ArrayList<>();
    }

    public void addTextSource(String textSource) {
        textAnnotations.put(textSource, new ArrayList<>());
    }

    public void addTextAnnotation(String textSource, OWLClass cls, Integer spanStart, Integer spanEnd) throws NoSuchFieldException {

        // Extract info from OWLClass
        dataExtractor.extractOWLObjectData(cls);

        // Make new annotation
        TextAnnotation newTextTextAnnotation = new TextAnnotation(textSource, cls);
        newTextTextAnnotation.setClassID(dataExtractor.getClassID());
        newTextTextAnnotation.setClassName(dataExtractor.getClassName());
        newTextTextAnnotation.setAnnotatorID(view.getProfileManager().getCurrentAnnotator().getAnnotatorID());
        newTextTextAnnotation.setAnnotatorName(view.getProfileManager().getCurrentAnnotator().getAnnotatorName());

        // Assign new annotation the text span
        TextSpan newTextSpan = new TextSpan(spanStart, spanEnd);
        newTextTextAnnotation.setSpan(newTextSpan);

        // Add annotation to annotation manager
        if (textAnnotations.containsKey(textSource)) {
            textAnnotations.get(textSource).add(newTextTextAnnotation);
        } else {
            System.out.println(textSource);
        }
        setSelectedTextAnnotation(newTextTextAnnotation);
        highlightAnnotation(spanStart, spanEnd, cls, textSource);

        view.getGraphViewer().addAnnotationNode(newTextTextAnnotation);
    }

    public void addTextAnnotation(String textSource, TextAnnotation newTextTextAnnotation) throws NoSuchFieldException {

        // Add annotation to annotation manager
        textAnnotations.get(textSource).add(newTextTextAnnotation);
        setSelectedTextAnnotation(newTextTextAnnotation);
        for (TextSpan textSpan : newTextTextAnnotation.getTextSpans()) {
            highlightAnnotation(textSpan.getStart(), textSpan.getEnd(), newTextTextAnnotation.getOwlClass(), textSource);
        }
//        view.getGraphViewer().addAnnotationNode(newTextTextAnnotation);
    }

    public void removeTextAnnotation(Integer spanStart, Integer spanEnd) {

        //String mentionSource = "Default";
        for (Collection<TextAnnotation> textAnnotations : this.textAnnotations.values()) {
            for (TextAnnotation textTextAnnotation : textAnnotations) {

                for (TextSpan textSpan : textTextAnnotation.getTextSpans()) {
                    if (Objects.equals(spanStart, textSpan.getStart()) && Objects.equals(spanEnd, textSpan.getEnd())) {
                        textAnnotations.remove(textTextAnnotation);
                        highlightAllAnnotations();
                        return;
                    }
                }
            }
        }
    }

    public void removeTextAnnotation(String textSource, TextAnnotation textTextAnnotation) {
        textAnnotations.get(textSource).remove(textTextAnnotation);
        highlightAllAnnotations();
    }

    private void highlightAllAnnotations() {
        for(KnowtatorTextPane textPane: view.getTextViewer().getAllTextPanes()) {
            textPane.getHighlighter().removeAllHighlights();
            textAnnotations.forEach((textSource, annotations) -> {
                for (TextAnnotation textTextAnnotation : annotations) {
                    for (TextSpan textSpan : textTextAnnotation.getTextSpans()) {
                        highlightAnnotation(textSpan.getStart(), textSpan.getEnd(), textTextAnnotation.getOwlClass(), textSource);
                    }
                }
            });
        }
    }

    private void highlightAnnotation(int spanStart, int spanEnd, OWLClass cls, String textSource) {
        DefaultHighlighter.DefaultHighlightPainter highlighter = view.getProfileManager().getCurrentAnnotator().getHighlighter(cls);
        if (highlighter == null) {
            Color c = JColorChooser.showDialog(null, String.format("Pick a color for %s", cls.toString()), Color.CYAN);
            if (c != null) {
                view.getProfileManager().addHighlighter(cls, c, view.getProfileManager().getCurrentAnnotator());
            }
            highlighter = view.getProfileManager().getCurrentAnnotator().getHighlighter(cls);
        }

        try {

            view.getTextViewer().getTextPaneByName(textSource).getHighlighter().addHighlight(spanStart, spanEnd, highlighter);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    //TODO hover over annotations in text to see what they are

    public HashMap<String, Collection<TextAnnotation>> getTextAnnotations() {
        return textAnnotations;
    }

    public TextAnnotation getSelectedTextAnnotation() {
        return selectedTextAnnotation;
    }

    public Set<TextAnnotation> getAnnotationsInRange(Integer spanStart, Integer spanEnd) {
        Set<TextAnnotation> annotationsInRange = new HashSet<>();
        for (Collection<TextAnnotation> textAnnotations : this.textAnnotations.values()) {
            for (TextAnnotation textTextAnnotation : textAnnotations) {
                for (TextSpan textSpan : textTextAnnotation.getTextSpans()) {
                    if (spanStart >= textSpan.getStart() && spanEnd <= textSpan.getEnd()) {
                        annotationsInRange.add(textTextAnnotation);
                    }
                }
            }
        }

        return annotationsInRange;
    }

    public AssertionRelationship addAssertion() {
        OWLObjectProperty property = view.getOWLWorkspace().getOWLSelectionModel().getLastSelectedObjectProperty();
        dataExtractor.extractOWLObjectData(property);
        AssertionRelationship newAssertionRelationship = new AssertionRelationship(property, dataExtractor.getClassName());
        assertionRelationships.add(newAssertionRelationship);
        return newAssertionRelationship;
    }

    public void setSelectedTextAnnotation(TextAnnotation selectedTextAnnotation) {
        this.selectedTextAnnotation = selectedTextAnnotation;
        view.setGlobalSelection1(selectedTextAnnotation.getOwlClass());
    }
}
