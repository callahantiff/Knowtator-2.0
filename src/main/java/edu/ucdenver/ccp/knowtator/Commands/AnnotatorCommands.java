package edu.ucdenver.ccp.knowtator.Commands;

import edu.ucdenver.ccp.knowtator.KnowtatorManager;
import edu.ucdenver.ccp.knowtator.ui.KnowtatorIcons;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class AnnotatorCommands {

    private KnowtatorManager manager;

    public AnnotatorCommands(KnowtatorManager manager) {

        this.manager = manager;
    }

    public KnowtatorCommand getAssignHighlighterCommand() {
        return new KnowtatorCommand(manager, "Assign highlighter", KnowtatorIcons.NEW_HIGHLIGHTER_ICON, "Assign highlighter color to selected class") {

            @Override
            public void actionPerformed(ActionEvent e) {
                manager.getAnnotatorManager().getCurrentAnnotator().addHighlighter(manager.getOwlWorkspace().getOWLSelectionModel().getLastSelectedClass());
            }

            //TODO removeProfile
        };
    }

    public KnowtatorCommand getNewAnnotatorCommand() {
        return new KnowtatorCommand(manager, "New Annotator", KnowtatorIcons.NEW_ANNOTATOR_ICON, "Add new annotator profile") {

            @Override
            public void actionPerformed(ActionEvent e) {
                JTextField field1 = new JTextField();
                JTextField field2 = new JTextField();
                Object[] message = {
                        "Annotator name", field1,
                        "Annotator ID", field2,
                };
                int option = JOptionPane.showConfirmDialog(null, message, "Enter annotator name and ID", JOptionPane.OK_CANCEL_OPTION);
                if (option == JOptionPane.OK_OPTION) {
                    String annotator = field1.getText();
                    String annotatorID = field2.getText();
                    manager.getAnnotatorManager().addNewAnnotator(annotator, annotatorID);
                }

            }
        };
    }

    public KnowtatorCommand getSwitchProfileCommand() {
        return new KnowtatorCommand(manager, "Switch Annotator", KnowtatorIcons.SWITCH_PROFILE_ICON, "Switch between annotators") {

            @Override
            public void actionPerformed(ActionEvent e) {
                manager.getAnnotatorManager().switchAnnotator();
            }
        };
    }
}
