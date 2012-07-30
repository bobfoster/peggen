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

/**
 * @author Bob Foster
 */
public class PegUtil {

	// Char <- '\\' [nrt'"\[\]\\]
	// / '\\' [0-9A-Fa-f][0-9A-Fa-f][0-9A-Fa-f][0-9A-Fa-f] # Added
	// / '\\' [0-2][0-7][0-7]
	// / '\\' [0-7][0-7]?
	// / !'\\' .
	public static char decodeChar(String rep) {
		char c = rep.charAt(0);
		if (c != '\\') return c;
		char x;
		c = rep.charAt(1);
		switch (c) {
			case 'n':
				x = '\n';
				break;
			case 'r':
				x = '\r';
				break;
			case 't':
				x = '\t';
				break;
			case '\'':
				x = '\'';
				break;
			case '"':
				x = '"';
				break;
			case '[':
			case ']':
			case '\\':
				x = c;
				break;
			default:
			{
				int n = 0;
				if (rep.length() == 5) {
					for (int i = 1; i < 5; i++) {
						c = rep.charAt(i);
						char lo;
						if ('0' <= c && c <= '9')
							lo = '0';
						else if ('a' <= c && c <= 'f')
							lo = 'a' - 10;
						else
							lo = 'A' - 10;
						n = n*16 + c - lo;
					}
				}
				else {
					for (int i = 1; i < rep.length(); i++)
						n = n*8 + rep.charAt(i) - '0';
				}
				x = (char) n;
			}
		}
		return x;
	}
}

