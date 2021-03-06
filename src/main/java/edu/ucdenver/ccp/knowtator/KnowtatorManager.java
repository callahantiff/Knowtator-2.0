package edu.ucdenver.ccp.knowtator;

import com.google.common.base.Optional;
import edu.ucdenver.ccp.knowtator.Annotator.AnnotatorManager;
import edu.ucdenver.ccp.knowtator.TextAnnotation.TextAnnotation;
import edu.ucdenver.ccp.knowtator.TextAnnotation.TextAnnotationManager;
import edu.ucdenver.ccp.knowtator.listeners.DocumentListener;
import edu.ucdenver.ccp.knowtator.listeners.OwlSelectionListener;
import edu.ucdenver.ccp.knowtator.listeners.TextAnnotationListener;
import edu.ucdenver.ccp.knowtator.owl.OntologyTranslator;
import edu.ucdenver.ccp.knowtator.ui.KnowtatorTextPane;
import edu.ucdenver.ccp.knowtator.ui.BasicKnowtatorView;
import edu.ucdenver.ccp.knowtator.xml.XmlUtil;
import org.apache.log4j.Logger;
import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.model.OWLModelManagerImpl;
import org.protege.editor.owl.model.OWLWorkspace;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntologyID;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


/**
 * @author Harrison Pielke-Lombardo
 */
public class KnowtatorManager implements OwlSelectionListener {
    public static final Logger log = Logger.getLogger(KnowtatorManager.class);
    public AnnotatorManager annotatorManager;
//    public OWLSelectionModel selectionModel;
    public TextAnnotationManager textAnnotationManager;
    public XmlUtil xmlUtil;

    public List<TextAnnotationListener> textAnnotationListeners;
    public List<DocumentListener> documentListeners;
    public List<OwlSelectionListener> owlSelectionListeners;
    public OWLModelManagerImpl owlModelManager;
    public OWLWorkspace owlWorkspace;
    public BasicKnowtatorView view;

    /**
     *
     */
    //TODO add a POS highlighter
    public KnowtatorManager(OWLModelManagerImpl owlModelManager, OWLWorkspace owlWorkspace) {
        this.owlModelManager = owlModelManager;
        this.owlWorkspace = owlWorkspace;

        /*
        Initialize the managers, models, and utils
         */
        textAnnotationManager = new TextAnnotationManager(this);
        annotatorManager = new AnnotatorManager(this);  //manipulates annotatorMap and highlighters
        xmlUtil = new XmlUtil(this);  //reads and writes to XML

        textAnnotationListeners = new ArrayList<>();
        documentListeners = new ArrayList<>();
        documentListeners.add(textAnnotationManager);
        owlSelectionListeners = new ArrayList<>();
        owlSelectionListeners.add(this);
    }



    public AnnotatorManager getAnnotatorManager() {
        return annotatorManager;
    }

    public void loadOntologyFromLocation(String classID) {

        List<String> ontologies = owlModelManager.getActiveOntologies().stream().map(ontology -> {
            OWLOntologyID ontID = ontology.getOntologyID();
            @SuppressWarnings("Guava") Optional<IRI> ontIRI = ontID.getOntologyIRI();
            if(ontIRI.isPresent()) {
                return ontIRI.get().toURI().toString();
            } else {
                return null;
            }
        }).collect(Collectors.toList());

        String ontologyLocation = OntologyTranslator.translate(classID);
        if (!ontologies.contains(ontologyLocation)) {
            owlModelManager.loadOntologyFromPhysicalURI(URI.create(OntologyTranslator.whichOntologyToUse(ontologyLocation)));
        }
    }

    public void setView(BasicKnowtatorView view) {
        this.view = view;
    }

    public XmlUtil getXmlUtil() {
        return xmlUtil;
    }
    public TextAnnotationManager getTextAnnotationManager() {
        return textAnnotationManager;
    }

    @SuppressWarnings("unused")
    public OWLModelManager getOwlModelManager() {
        return owlModelManager;
    }
    public List<TextAnnotationListener> getTextAnnotationListeners() {
        return textAnnotationListeners;
    }
    @SuppressWarnings("unused")
    public List<DocumentListener> getDocumentListeners() {
        return documentListeners;
    }
    public OWLWorkspace getOwlWorkspace() {
        return owlWorkspace;
    }

    public void owlSelectionChangedEvent(OWLEntity owlEntity) {
        for (OwlSelectionListener listener : owlSelectionListeners) {
            listener.owlEntitySelectionChanged(owlEntity);
        }
    }

    public void textAnnotationsChangedEvent() {
        for (TextAnnotationListener listener : textAnnotationListeners) {
            listener.textAnnotationsChanged();
        }
    }

    @SuppressWarnings("unused")
    public void textAnnotationsChangedEvent(TextAnnotation newAnnotation) {
        for (TextAnnotationListener listener : textAnnotationListeners) {
            listener.textAnnotationsChanged(newAnnotation);
        }
    }

    public void documentChangedEvent(KnowtatorTextPane textPane) {
        for (DocumentListener listener : documentListeners) {
            listener.documentChanged(textPane);
        }
    }

    public BasicKnowtatorView getKnowtatorView() {
        return view;
    }

    @Override
    public void owlClassSelectionChanged(OWLClass cls) {

    }

    @Override
    public void owlEntitySelectionChanged(OWLEntity owlEntity) {
        if (view.getView() != null) {
            if (view.getView().isSyncronizing()) {
                owlWorkspace.getOWLSelectionModel().setSelectedEntity(owlEntity);
            }
        }
    }

    public static void main(String[] args) {
        OWLModelManagerImpl modelManager = new OWLModelManagerImpl();
        OWLWorkspace workspace = new OWLWorkspace();
        KnowtatorManager knowtatorManager = new KnowtatorManager(modelManager, workspace);
        knowtatorManager.getXmlUtil().read("/file/test_annotations.xml", true);

    }
}
