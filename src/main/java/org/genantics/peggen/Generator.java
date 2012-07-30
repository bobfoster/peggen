/*******************************************************************************
 * Copyright (C) 2003-2012 Bob Foster. All rights reserved.
 * 
 * This software is provided under the terms of the Apache License, Version 2.0
 * A copy of the license is available at http://www.apache.org/licenses/LICENSE-2.0.html.
 * 
 * Contributors:
 * 
 *    Bob Foster, initial API and implementation.
 *******************************************************************************/
 
package org.genantics.peggen;

import java.io.PrintWriter;

/**
 * @author Bob Foster
 */
public interface Generator {

	void generate(Node grammar, char[] in, PrintWriter writer, String packageName, String className, String startRule, String tab);
}
