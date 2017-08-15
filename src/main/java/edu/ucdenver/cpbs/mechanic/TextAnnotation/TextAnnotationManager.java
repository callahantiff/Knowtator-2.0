package edu.ucdenver.cpbs.mechanic.TextAnnotation;

import edu.ucdenver.cpbs.mechanic.MechAnICView;
import edu.ucdenver.cpbs.mechanic.ProfileManager;
import edu.ucdenver.cpbs.mechanic.owl.OWLAPIDataExtractor;
import edu.ucdenver.cpbs.mechanic.ui.MechAnICTextViewer;
import org.semanticweb.owlapi.model.*;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import java.awt.*;
import java.io.InputStream;
import java.util.*;

public final class TextAnnotationManager {


   private HashMap<String, HashMap<Integer, TextAnnotation>> textAnnotations;

   private MechAnICView view;
   private ProfileManager profileManager;
   private OWLAPIDataExtractor dataExtractor;

    public TextAnnotationManager(MechAnICView view, ProfileManager profileManager) {
        this.view = view;
        this.profileManager = profileManager;

        dataExtractor = new OWLAPIDataExtractor(this.view.getOWLModelManager());

        textAnnotations = new HashMap<>();
    }



    public void addTextAnnotation(OWLClass cls, Integer spanStart, Integer spanEnd, String spannedText) throws NoSuchFieldException {

        dataExtractor.extractOWLClassData(cls);


        String mentionSource = profileManager.getCurrentHighlighterName();
        int mentionID = textAnnotations.size();
        String classID = dataExtractor.getClassID();
        String className = dataExtractor.getClassName();

        TextAnnotation newTextAnnotation = new TextAnnotation(
                profileManager.getCurrentProfile().getAnnotatorID(),
                profileManager.getCurrentProfile().getAnnotatorName(),
                spanStart,
                spanEnd,
                spannedText,
                classID,
                className
        );
        if (!textAnnotations.containsKey(mentionSource)) {
            textAnnotations.put(mentionSource, new HashMap<>());
        }
        textAnnotations.get(mentionSource).put(mentionID, newTextAnnotation);
    }

    public void removeTextAnnotation(Integer spanStart, Integer spanEnd, MechAnICTextViewer textViewer) {
        String mentionSource = profileManager.getCurrentHighlighterName();
        if (textAnnotations.containsKey(mentionSource)) {
            for (Map.Entry<Integer, TextAnnotation> instance : textAnnotations.get(mentionSource).entrySet()) {
                int mentionID = instance.getKey();
                TextAnnotation textAnnotation = instance.getValue();
                if (Objects.equals(spanStart, textAnnotation.getSpanStart()) && Objects.equals(spanEnd, textAnnotation.getSpanEnd())) {
                    textAnnotations.get(mentionSource).remove(mentionID);
                    break;
                }
            }
        }
        highlightAllAnnotations(textViewer);
    }


    public void highlightAllAnnotations(MechAnICTextViewer textViewer) {
        textViewer.getHighlighter().removeAllHighlights();
        for (Map.Entry<String, HashMap<Integer, TextAnnotation>> instance1 : textAnnotations.entrySet()) {
            String mentionSource = instance1.getKey();
            for (Map.Entry<Integer, TextAnnotation> instance2 : instance1.getValue().entrySet() ){
                TextAnnotation textAnnotation = instance2.getValue();
                highlightAnnotation(textAnnotation.getSpanStart(), textAnnotation.getSpanEnd(), textViewer, mentionSource);
            }
        }
    }

    public void highlightAnnotation(int spanStart, int spanEnd, MechAnICTextViewer textViewer, String mentionSource) {
        DefaultHighlighter.DefaultHighlightPainter highlighter = profileManager.getCurrentProfile().getHighlighter(mentionSource);
        if (highlighter == null) {
            Color c = JColorChooser.showDialog(null, String.format("Pick a color for %s", mentionSource), Color.BLUE);
            if (c != null) {
                profileManager.addHighlighter(mentionSource, c, profileManager.getCurrentProfile());
            }
            highlighter = profileManager.getCurrentHighlighter();
        }

        try {
            textViewer.getHighlighter().addHighlight(spanStart, spanEnd, highlighter);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    //TODO hover over annotations in text to see what they are

    public ProfileManager getProfileManager() {
        return profileManager;
    }

    public HashMap<String, HashMap<Integer, TextAnnotation>> getTextAnnotations() {
        return textAnnotations;
    }
}