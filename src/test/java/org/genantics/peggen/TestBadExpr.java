/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.genantics.peggen;

import java.io.*;
import java.util.List;
import junit.framework.TestCase;

/**
 *
 * @author Bob Foster
 */
public class TestBadExpr extends TestCase {
  
  private Node[] tree;
  private List   errs;

  private void parse(String pegName) {
    PegGen gen = new PegGen();
    File inFile = new File("./src/test/resources/org/genantics/peggen/"+pegName+".peg");
    assertTrue(inFile.exists());
    try {
      Reader reader = null;
      try {
        char[] buf = new char[(int) inFile.length()];
        reader = new FileReader(inFile);
        reader.read(buf);
        Parser parser = new Parser();
        tree = parser.parseGrammar(buf, 0, buf.length);
        errs = parser.getErrors();
      } catch (Exception e) {
        e.printStackTrace();
        assertTrue(false);
      } finally {
        if (reader != null)
          reader.close();
      }
    } catch (IOException e) {
      e.printStackTrace();
      assertTrue(false);
    }
  }
  
  private void printErrs() {
    for (Object obj : errs)
      System.err.println(obj.toString());
  }
  
  public void testBug() {
    parse("expr-bnf-bug");
    assertTrue(tree != null);
    assertTrue(errs == null);
  }
  
  public void testSyntax() {
    parse("expr-bnf-syntax");
    assertTrue(tree == null);
    assertTrue(errs.size() == 3);
    printErrs();
  }
  
  public void testUndef() {
    parse("expr-bnf-undef");
    assertTrue(tree == null);
    assertTrue(errs.size() == 6);
    printErrs();
  }  
}
