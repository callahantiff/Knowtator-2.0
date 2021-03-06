package edu.ucdenver.ccp.knowtator.Annotator;

import edu.ucdenver.ccp.knowtator.KnowtatorManager;
import edu.ucdenver.ccp.knowtator.ui.ListDialog;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

public class AnnotatorManager {

    public static final Logger log = Logger.getLogger(KnowtatorManager.class);
    public Annotator currentAnnotator;

    public Map<String, Annotator> annotatorMap;
    public KnowtatorManager manager;


    public AnnotatorManager(KnowtatorManager manager) {
        this.manager = manager;
        annotatorMap = new HashMap<>();
    }

    public Annotator addNewAnnotator(String annotatorName, String annotatorID) {
        log.warn(String.format("Profile name: %s  id: %s", annotatorName, annotatorID));

        Annotator newAnnotator = new Annotator(manager, annotatorName, annotatorID);
        annotatorMap.put(annotatorName, newAnnotator);

        currentAnnotator = newAnnotator;

        return newAnnotator;
    }

    public Annotator getCurrentAnnotator() {
        return currentAnnotator;
    }

    public String[] getAnnotatorNames() {
        return annotatorMap.keySet().toArray(new String[annotatorMap.keySet().size()]);
    }

    public void switchAnnotator() {
        String annotatorName = ListDialog.showDialog(null, null, "Profiles", "Annotator Chooser", getAnnotatorNames(), getAnnotatorNames()[0], null);

        if (annotatorName != null)
        {
            currentAnnotator = annotatorMap.get(annotatorName);
        }

    }
}
