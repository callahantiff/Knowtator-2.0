/*
 * The contents of this file are subject to the Mozilla Public
 * License Version 1.1 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 *
 * The Original Code is Knowtator.
 *
 * The Initial Developer of the Original Code is University of Colorado.  
 * Copyright (C) 2005 - 2008.  All Rights Reserved.
 *
 * Knowtator was developed by the Center for Computational Pharmacology
 * (http://compbio.uchcs.edu) at the University of Colorado Health 
 *  Sciences Center School of Medicine with support from the National 
 *  Library of Medicine.  
 *
 * Current information about Knowtator can be obtained at 
 * http://knowtator.sourceforge.net/
 *
 * Contributor(s):
 *   Philip V. Ogren <philip@ogren.info> (Original Author)
 */

package edu.ucdenver.ccp.knowtator.iaa.html;

import edu.ucdenver.ccp.knowtator.TextAnnotation.TextAnnotation;
import edu.ucdenver.ccp.knowtator.iaa.matcher.Matcher;
import edu.ucdenver.ccp.knowtator.iaa.AnnotationSpanIndex;
import edu.ucdenver.ccp.knowtator.iaa.IAA;

import java.io.File;
import java.io.PrintStream;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static edu.ucdenver.ccp.knowtator.TextAnnotation.TextAnnotationProperties.CLASS_NAME;

@SuppressWarnings("unused")
public class SpanMatcherHTML {

	public static void printIAA(IAA iaa, Matcher matcher, File directory, int numberOfDocs,
                                Map<TextAnnotation, String> annotationTexts, Map<TextAnnotation, String> annotationTextNames) throws Exception {
		NumberFormat percentageFormat = NumberFormat.getPercentInstance();
		percentageFormat.setMinimumFractionDigits(2);

		String fileName = matcher.getName();

		PrintStream html = new PrintStream(new File(directory, fileName + ".html"));

		html.println(IAA2HTML.initHTML(matcher.getName(), matcher.getDescription()));
		html.println("<h2>" + iaa.getSetNames().size() + "-way IAA Results</h2>");

		html.println("<table border=1><tr>" + "<td><b>Type</b></td>" + "<td><b>IAA</b></td>"
				+ "<td><b>matches</b></td>" + "<td><b>non-matches</b></td>" + "<td><b>confused class assignments</tr>");

		Set<String> classes = iaa.getAnnotationClasses();
		Set<String> sets = iaa.getSetNames();

		Map<String, Set<TextAnnotation>> allwayMatches = iaa.getAllwayMatches();
		Map<String, Set<TextAnnotation>> allwayNonmatches = iaa.getAllwayNonmatches();

		Map<TextAnnotation, Set<TextAnnotation>> matchSets = iaa.getAllwayMatchSets();

		Set<TextAnnotation> allwayMatchesSingleSet = IAA2HTML.getSingleSet(allwayMatches);
		Set<TextAnnotation> allwayNonmatchesSingleSet = IAA2HTML.getSingleSet(allwayNonmatches);

		AnnotationSpanIndex spanIndex = new AnnotationSpanIndex(allwayNonmatchesSingleSet);

		int totalAllwayMatches = allwayMatchesSingleSet.size();
		int totalAllwayNonmatches = allwayNonmatchesSingleSet.size();

		double iaaScore = (double) totalAllwayMatches / ((double) totalAllwayMatches + (double) totalAllwayNonmatches);

		html.println("<tr><td><b>All classes</b></td>" + "<td>" + percentageFormat.format(iaaScore) + "</td>" + "<td>"
				+ totalAllwayMatches + "</td>" + "<td>" + totalAllwayNonmatches + "</td></tr>");

		Map<String, Set<TextAnnotation>> sortedAllwayMatches = IAA2HTML.sortByType(classes, allwayMatchesSingleSet);
		Map<String, Set<TextAnnotation>> sortedAllwayNonmatches = IAA2HTML.sortByType(classes, allwayNonmatchesSingleSet);

		java.util.List<String> sortedTypes = new ArrayList<>(classes);
		Collections.sort(sortedTypes);

		for (String type : sortedTypes) {
			int classMatches = sortedAllwayMatches.get(type).size();
			int classNonmatches = sortedAllwayNonmatches.get(type).size();

			iaaScore = (double) classMatches / ((double) classMatches + (double) classNonmatches);

			html.println("<tr><td>" + type + "</td>" + "<td>" + percentageFormat.format(iaaScore) + "</td>" + "<td>"
					+ classMatches + "</td>" + "<td>" + classNonmatches + "</td>");
			Map<String, int[]> confusionCounts = errorMatrix(sortedAllwayMatches.get(type), matchSets);
			html.println("<td>");
			for (String confusedClass : confusionCounts.keySet()) {
				html.println("  " + confusedClass + "=" + confusionCounts.get(confusedClass)[0]);
			}
			html.println("</td>");
		}
		html.println("</table>");

		html.println("<br>IAA calculated on " + numberOfDocs + " documents.");
		html.println("<br>all annotations = matches + non-matches");
		html.println("<br>IAA = matches / all annotations");

		IAA2HTML.printMatchData(html, sets, fileName, directory, allwayMatches, annotationTexts, annotationTextNames,
				classes, iaa);

		IAA2HTML.printNonmatchData(html, sets, fileName, directory, allwayNonmatches, spanIndex, annotationTexts,
				annotationTextNames, classes, iaa);

		Map<String, Map<String, Set<TextAnnotation>>> pairwiseMatches = iaa.getPairwiseMatches();
		Map<String, Map<String, Set<TextAnnotation>>> pairwiseNonmatches = iaa.getPairwiseNonmatches();

		IAA2HTML.printPairwiseAgreement(html, sets, pairwiseMatches, pairwiseNonmatches, percentageFormat);

		html.flush();
		html.close();
	}

	public static Map<String, int[]> errorMatrix(Set<TextAnnotation> matches, Map<TextAnnotation, Set<TextAnnotation>> matchSets) {
		Map<String, int[]> counts = new HashMap<>();

		for (TextAnnotation match : matches) {
			Set<TextAnnotation> matchedTextAnnotations = matchSets.get(match);
			for (TextAnnotation matchedTextAnnotation : matchedTextAnnotations) {
				if (!matchedTextAnnotation.equals(match)) {
					String annotationClass = matchedTextAnnotation.getProperty(CLASS_NAME);
					if (!counts.containsKey(annotationClass)) {
						counts.put(annotationClass, new int[1]);
					}
					counts.get(annotationClass)[0]++;
				}
			}
		}
		return counts;
	}

}
