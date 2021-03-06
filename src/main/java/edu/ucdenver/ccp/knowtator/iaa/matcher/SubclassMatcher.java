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
package edu.ucdenver.ccp.knowtator.iaa.matcher;

import edu.ucdenver.ccp.knowtator.TextAnnotation.TextAnnotation;
import edu.ucdenver.ccp.knowtator.iaa.IAA;
import edu.ucdenver.ccp.knowtator.xml.XmlTags;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * This matcher is very similar to ClassMatcher.
 * 
 * @author Compaq_Owner
 * 
 */

@SuppressWarnings({"JavadocReference", "JavaDoc", "unused"})
public class SubclassMatcher implements Matcher {

	public String className;

	public Set<String> subclassNames;

	public ClassHierarchy hierarchy;

	public SubclassMatcher(ClassHierarchy hierarchy) {
		this.hierarchy = hierarchy;
	}

	/**
	 * This method will return a match from ClassMatcher. If one does not exist,
	 * then match for
	 * 
	 * 
	 * 
	 * Otherwise, null is returned.
	 * 
	 * @param textAnnotation
	 * @param compareSetName
	 * @param excludeTextAnnotations
	 * @param iaa
	 * @param matchResult
	 *            will be set to NONTRIVIAL_MATCH, NONTRIVIAL_NONMATCH, or
	 *            TRIVIAL_NONMATCH. Trivial non-matches occur when the
	 *            annotation is not of the class specified by setIAAClass or a
	 *            subclass of it. Trivial non-matches should be ignored and not
	 *            counted in any IAA metrics.
	 * @see edu.uchsc.ccp.iaa.matcher.Matcher#match(TextAnnotation, String, Set,
	 *      IAA, MatchResult)
	 * @see edu.uchsc.ccp.iaa.matcher.MatchResult#NONTRIVIAL_MATCH
	 * @see edu.uchsc.ccp.iaa.matcher.MatchResult#NONTRIVIAL_NONMATCH
	 * @see edu.uchsc.ccp.iaa.matcher.MatchResult#TRIVIAL_NONMATCH
	 * @see edu.uchsc.ccp.iaa.Annotation#getShortestAnnotation(Collection)
	 * @see #setIAAClass(String)
	 */
	public TextAnnotation match(TextAnnotation textAnnotation, String compareSetName, Set<TextAnnotation> excludeTextAnnotations, IAA iaa,
								MatchResult matchResult) {

		String annotationClassName = textAnnotation.getProperty(XmlTags.MENTION_CLASS);
		if (!subclassNames.contains(annotationClassName)) {
			matchResult.setResult(MatchResult.TRIVIAL_NONMATCH);
			return null;
		}

		TextAnnotation classMatch = ClassMatcher.match(textAnnotation, compareSetName, iaa, excludeTextAnnotations);
		if (classMatch != null) {
			matchResult.setResult(MatchResult.NONTRIVIAL_MATCH);
			return classMatch;
		}

		Set<TextAnnotation> candidateTextAnnotations = new HashSet<>();
		for (String subclassName : subclassNames) {
			candidateTextAnnotations.addAll(iaa.getAnnotationsOfClass(subclassName, compareSetName));
		}

		Set<TextAnnotation> exactlyOverlappingTextAnnotations = new HashSet<>(iaa.getExactlyOverlappingAnnotations(
                textAnnotation, compareSetName));
		exactlyOverlappingTextAnnotations.retainAll(candidateTextAnnotations);
		exactlyOverlappingTextAnnotations.removeAll(excludeTextAnnotations);
		if (exactlyOverlappingTextAnnotations.size() > 0) {
			matchResult.setResult(MatchResult.NONTRIVIAL_MATCH);
			return exactlyOverlappingTextAnnotations.iterator().next();
		}

		Set<TextAnnotation> overlappingTextAnnotations = new HashSet<>(iaa.getOverlappingAnnotations(textAnnotation,
				compareSetName));
		overlappingTextAnnotations.retainAll(candidateTextAnnotations);
		overlappingTextAnnotations.removeAll(excludeTextAnnotations);
		if (overlappingTextAnnotations.size() > 0) {
			matchResult.setResult(MatchResult.NONTRIVIAL_MATCH);
			if (overlappingTextAnnotations.size() == 1)
				return overlappingTextAnnotations.iterator().next();
			return TextAnnotation.getShortestAnnotation(overlappingTextAnnotations);
		}

		matchResult.setResult(MatchResult.NONTRIVIAL_NONMATCH);
		return null;
	}

	/**
	 * Sets the class of the
	 * 
	 * @param className
	 */
    public void setIAAClass(String className) {
		this.className = className;
		subclassNames = hierarchy.getSubclasses(className);
	}

	public String getIAAClass() {
		return className;
	}

	public String getName() {
		return "Subclass matcher for class '" + className + "'";
	}

	public String getDescription() {
		return "Two annotations match if their class assignments are equal to '" + className + "' or a subclass of '"
				+ className + "' and their spans overlap.";
	}

	public boolean returnsTrivials() {
		return true;
	}

	public Set<String> getSubclasses() {
		return hierarchy.getSubclasses(className);
	}
}
