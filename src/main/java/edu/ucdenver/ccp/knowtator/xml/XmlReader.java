package edu.ucdenver.ccp.knowtator.xml;

import edu.ucdenver.ccp.knowtator.Annotator.Annotator;
import edu.ucdenver.ccp.knowtator.KnowtatorDocumentHandler;
import edu.ucdenver.ccp.knowtator.KnowtatorManager;
import edu.ucdenver.ccp.knowtator.TextAnnotation.TextAnnotationProperties;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static edu.ucdenver.ccp.knowtator.xml.XmlTags.*;

public class XmlReader {
    public static final Logger log = Logger.getLogger(KnowtatorManager.class);

    public static void read(String fileName, KnowtatorManager manager, Boolean fromResources) throws ParserConfigurationException, IOException, SAXException {
        /*
        doc parses the XML into a graph
         */
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(KnowtatorDocumentHandler.getFileInputStream(fileName, fromResources));
        doc.getDocumentElement().normalize();

        List<Node> textSourceNodes = XmlUtil.asList(doc.getElementsByTagName(XmlTags.ANNOTATIONS));
        List<Node> profileNodes = XmlUtil.asList(doc.getElementsByTagName(XmlTags.PROFILE));

        if (textSourceNodes.size() > 0) {
            readAnnotations(textSourceNodes, manager);
        }
        if (profileNodes.size() > 0) {
            readProfiles(profileNodes, manager);
        }
    }

    public static void readAnnotations(List<Node> textSourceNodes, KnowtatorManager manager) throws ParserConfigurationException, IOException, SAXException {
        log.warn("Reading annotations from XML");



        HashMap<String, List<HashMap<String, String>>> textAnnotations = new HashMap<>();

        /*
        Get annotations by document
         */
        for (Node textSourceNode: textSourceNodes) {
            Element textSourceElement = (Element) textSourceNode;
            String textSource = textSourceElement.getAttribute(XmlTags.TEXTSOURCE);
            log.warn(String.format("Text source: %s", textSource));


            HashMap<String, String[]> classMentionToClassIDMap = getClassIDsFromXml(textSourceElement);
            textAnnotations.put(textSource, getAnnotationsFromXml(textSourceElement, classMentionToClassIDMap));
        }

        manager.getTextAnnotationManager().addTextAnnotations(textAnnotations);

    }

    public static void readProfiles(List<Node> profileNodes, KnowtatorManager manager) throws ParserConfigurationException, IOException, SAXException {
        log.warn("Reading profiles from XML");

        /*
        Get annotations by document
         */
        for (Node profileNode: profileNodes) {
            Element profileElement = (Element) profileNode;
            String profileName = profileElement.getAttribute(XmlTags.PROFILE_NAME);
            String profileID = profileElement.getAttribute(XmlTags.PROFILE_ID);

            Annotator newAnnotator = manager.getAnnotatorManager().addNewAnnotator(profileName, profileID);

            getHighlightersFromXml(newAnnotator, profileElement);
        }
    }

    public static void getHighlightersFromXml(Annotator newAnnotator, Element profileElement) {

        for (Node highlighterNode : XmlUtil.asList(profileElement.getElementsByTagName(XmlTags.HIGHLIGHTER))) {
            if (highlighterNode.getNodeType() == Node.ELEMENT_NODE) {
                Element highlighterElement = (Element) highlighterNode;

                String className = highlighterElement.getElementsByTagName(XmlTags.CLASS_NAME).item(0).getTextContent();
                String classID = highlighterElement.getElementsByTagName(XmlTags.CLASS_ID).item(0).getTextContent();
                String color = highlighterElement.getElementsByTagName(XmlTags.COLOR).item(0).getTextContent();

                newAnnotator.addHighlighter(classID, className, color);
            }
        }
    }

    public static HashMap<String, String[]> getClassIDsFromXml(Element textSourceElement) {
        log.warn("Adding annotations to Knowtator");



        /*
        Next parse classes and add the annotations
         */
        HashMap<String, String[]> mentionTracker = new HashMap<>();

        for (Node classNode : XmlUtil.asList(textSourceElement.getElementsByTagName(CLASS_MENTION))) {
            if (classNode.getNodeType() == Node.ELEMENT_NODE) {
                Element classElement = (Element) classNode;

                String fullMention = classElement.getAttribute(CLASS_MENTION_ID);

                String mentionSource = XmlUtil.getMentionSourceFromXML(fullMention);
                int mentionID = XmlUtil.getMentionIDFromXML(fullMention);

                String classID = ((Element) classElement.getElementsByTagName(MENTION_CLASS).item(0)).getAttribute(MENTION_CLASS_ID);
                String className = classElement.getElementsByTagName(MENTION_CLASS).item(0).getTextContent();

                mentionTracker.put(mentionSource + mentionID, new String[] {
                        classID,
                        className
                });
            }
        }

        return mentionTracker;
    }

    public static List<HashMap<String, String>>  getAnnotationsFromXml(Element textSourceElement, HashMap<String, String[]> classMentionToClassIDMap) {

        List<HashMap<String, String>> annotations = new ArrayList<>();

        for (Node annotationNode : XmlUtil.asList(textSourceElement.getElementsByTagName(ANNOTATION))) {
            if (annotationNode.getNodeType() == Node.ELEMENT_NODE) {
                Element annotationElement = (Element) annotationNode;

                String fullMention = ((Element) annotationElement.getElementsByTagName(MENTION).item(0)).getAttribute(MENTION_ID);

                String mentionSource = XmlUtil.getMentionSourceFromXML(fullMention);
                int mentionID = XmlUtil.getMentionIDFromXML(fullMention);

                HashMap<String, String> annotationProperties = new HashMap<String, String>() {
                    {
                        put(TextAnnotationProperties.CLASS_ID, classMentionToClassIDMap.get(mentionSource + mentionID)[0]);
                        put(TextAnnotationProperties.CLASS_NAME, classMentionToClassIDMap.get(mentionSource + mentionID)[1]);
                        put(TextAnnotationProperties.ANNOTATOR, annotationElement.getElementsByTagName(ANNOTATOR).item(0).getTextContent());
                        put(TextAnnotationProperties.ANNOTATOR_ID, ((Element) annotationElement.getElementsByTagName(ANNOTATOR).item(0)).getAttribute(ANNOTATOR_ID));
                        put(TextAnnotationProperties.SPAN, getSpanInfo(annotationElement));
                    }
                };

                annotations.add(annotationProperties);
            }
        }

        return annotations;
    }

    public static String getSpanInfo(Element annotationElement) {
        String textSpans = "";
        for (Node spanNode : XmlUtil.asList(annotationElement.getElementsByTagName(XmlTags.SPAN))) {
            if (spanNode.getNodeType() == Node.ELEMENT_NODE) {
                Element spanElement = (Element) spanNode;
                String spanStart = spanElement.getAttribute(XmlTags.SPAN_START);
                String spanEnd = spanElement.getAttribute(XmlTags.SPAN_END);

                if (textSpans.length() == 0) {
                    textSpans = String.format("%s,%s", spanStart, spanEnd);
                } else {
                    textSpans = String.format("%s;%s,%s", textSpans, spanStart, spanEnd);
                }
            }
        }
        return textSpans;
    }



}
