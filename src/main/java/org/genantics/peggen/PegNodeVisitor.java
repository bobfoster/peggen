/*******************************************************************************
 * Copyright (c) 2003-2012 Bob Foster.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Bob Foster - initial API and implementation
 *******************************************************************************/
 
package org.genantics.peggen;

import java.util.HashMap;

/**
 * @author Bob Foster
 */
public abstract class PegNodeVisitor {

	public static final int GRAMMAR = 0;
	public static final int DEFINITION = 1;
	public static final int EXPRESSION = 2;
	public static final int SEQUENCE = 3;
	public static final int PREFIX = 4;
	public static final int SUFFIX = 5;
	public static final int SUPPRESSPRIMARY = 6;
	public static final int PRIMARY = 7;
	public static final int IDENTIFIER = 8;
	public static final int LITERAL = 9;
	public static final int CLASS = 10;
	public static final int RANGE = 11;
	public static final int CHAR = 12;
	public static final int AND = 13;
	public static final int NOT = 14;
	public static final int QUESTION = 15;
	public static final int STAR = 16;
	public static final int PLUS = 17;
	public static final int DOT = 18;
	public static final int SUPPRESS = 19;
	public static final int DEFSUPPRESS = 20;
	public static final int TERM = 21;
	public static final int ERROR = 22;
  public static final int BNFDEFINITION = 23;
  public static final int SPECIALIDENTIFIER = 24;

	public static final HashMap PEGMAP = new HashMap();
	static {
		PEGMAP.put("Grammar", new Integer(GRAMMAR));
		PEGMAP.put("Definition", new Integer(DEFINITION));
		PEGMAP.put("Expression", new Integer(EXPRESSION));
		PEGMAP.put("Sequence", new Integer(SEQUENCE));
		PEGMAP.put("Prefix", new Integer(PREFIX));
		PEGMAP.put("Suffix", new Integer(SUFFIX));
		PEGMAP.put("SuppressPrimary", new Integer(SUPPRESSPRIMARY));
		PEGMAP.put("Primary", new Integer(PRIMARY));
		PEGMAP.put("Identifier", new Integer(IDENTIFIER));
		PEGMAP.put("Literal", new Integer(LITERAL));
		PEGMAP.put("Class", new Integer(CLASS));
		PEGMAP.put("Range", new Integer(RANGE));
		PEGMAP.put("Char", new Integer(CHAR));
		PEGMAP.put("AND", new Integer(AND));
		PEGMAP.put("NOT", new Integer(NOT));
		PEGMAP.put("QUESTION", new Integer(QUESTION));
		PEGMAP.put("STAR", new Integer(STAR));
		PEGMAP.put("PLUS", new Integer(PLUS));
		PEGMAP.put("DOT", new Integer(DOT));
		PEGMAP.put("SUPPRESS", new Integer(SUPPRESS));
		PEGMAP.put("DEFSUPPRESS", new Integer(DEFSUPPRESS));
		PEGMAP.put("Term", new Integer(TERM));
		PEGMAP.put("Error", new Integer(ERROR));
		PEGMAP.put("BNFDefinition", new Integer(BNFDEFINITION));
		PEGMAP.put("SpecialIdentifier", new Integer(SPECIALIDENTIFIER));
	}
	
	public void visitChildren(Node p) {
		for (Node child = p.child; child != null; child = child.next)
			visit(child);
	}
	
	public void visit(Node p) {
		switch (((Integer) PEGMAP.get(p.name)).intValue()) {
			case GRAMMAR :
				visitGrammar(p);
				break;
			case DEFINITION :
      case BNFDEFINITION:
				visitDefinition(p);
				break;
			case EXPRESSION :
				visitExpression(p);
				break;
			case SEQUENCE :
				visitSequence(p);
				break;
			case PREFIX :
				visitPrefix(p);
				break;
			case SUFFIX :
				visitSuffix(p);
				break;
			case SUPPRESSPRIMARY :
				visitSuppressPrimary(p);
				break;
			case PRIMARY :
				visitPrimary(p);
				break;
			case IDENTIFIER :
      case SPECIALIDENTIFIER :
				visitIdentifier(p);
				break;
			case LITERAL :
				visitLiteral(p);
				break;
			case CLASS :
				visitClass(p);
				break;
			case RANGE :
				visitRange(p);
				break;
			case CHAR :
				visitChar(p);
				break;
			case AND :
				visitAND(p);
				break;
			case NOT :
				visitNOT(p);
				break;
			case QUESTION :
				visitQUESTION(p);
				break;
			case STAR :
				visitSTAR(p);
				break;
			case PLUS :
				visitPLUS(p);
				break;
			case DOT :
				visitDOT(p);
				break;
			case SUPPRESS :
				visitSUPPRESS(p);
				break;
			case DEFSUPPRESS :
				visitDEFSUPPRESS(p);
				break;
			case TERM :
				visitTerm(p);
				break;
			case ERROR :
				visitError(p);
				break;
		}
	}
	void visitGrammar(Node node) {
	}
	void visitDefinition(Node node) {
	}
	void visitExpression(Node node) {
	}
	void visitSequence(Node node) {
	}
	void visitPrefix(Node node) {
	}
	void visitSuffix(Node node) {
	}
	void visitSuppressPrimary(Node node) {
	}
	void visitPrimary(Node node) {
	}
	void visitIdentifier(Node node) {
	}
	void visitLiteral(Node node) {
	}
	void visitClass(Node node) {
	}
	void visitRange(Node node) {
	}
	void visitChar(Node node) {
	}
	void visitAND(Node node) {
	}
	void visitNOT(Node node) {
	}
	void visitQUESTION(Node node) {
	}
	void visitSTAR(Node node) {
	}
	void visitPLUS(Node node) {
	}
	void visitDOT(Node node) {
	}
	void visitSUPPRESS(Node node) {
	}
	void visitDEFSUPPRESS(Node node) {
	}
	void visitTerm(Node node) {
	}
	void visitError(Node node) {
	}
}

