package edu.ucdenver.ccp.knowtator.owl;

import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.model.find.OWLEntityFinder;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.search.EntitySearcher;

import java.util.Collection;

@SuppressWarnings("unused")
public class OWLAPIDataExtractor {

    private OWLModelManager man;
    private OWLEntityFinder entityFinder;

    private OWLAnnotationProperty nameSpaceLabel;
    private OWLAnnotationProperty idLabel;

    private String classNameSpace;
    private String classID;
    private String className;

    public OWLAPIDataExtractor(OWLModelManager man) {
        this.man = man;
        this.entityFinder = man.getOWLEntityFinder();

        nameSpaceLabel = entityFinder.getOWLAnnotationProperty("has_obo_namespace");
        idLabel = entityFinder.getOWLAnnotationProperty("id");
    }

    private Collection<OWLAnnotation> getOWLObjectAnnotations(OWLEntity ent) {
        if (ent != null) {
            return EntitySearcher.getAnnotations(ent.getIRI(), man.getActiveOntology());
        }
        else {
            return null;
        }
    }

    private Collection<OWLAnnotationProperty> getOWLAnnotationProperties(OWLOntology ont) {
        return ont.getAnnotationPropertiesInSignature();
    }

    public void extractOWLObjectData(OWLEntity ent){

        for (OWLAnnotation anno : getOWLObjectAnnotations(ent)){
            if (anno.getProperty() == nameSpaceLabel) {
                if (anno.getValue() instanceof OWLLiteral) {
                    classNameSpace = ((OWLLiteral) anno.getValue()).getLiteral();
                }
            } else if (anno.getProperty() == idLabel) {
                if (anno.getValue() instanceof OWLLiteral) {
                    classID = ((OWLLiteral) anno.getValue()).getLiteral();
                }
            } if (anno.getProperty().isLabel()) {
                if (anno.getValue() instanceof OWLLiteral) {
                    className = ((OWLLiteral) anno.getValue()).getLiteral();
                }
            }
        }
    }

    public String getClassNameSpace() {
        return classNameSpace;
    }

    public String getClassID() {
        return classID;
    }

    public String getClassName() {
        return className;
    }
}
