/*******************************************************************************
 * Copyright (C) 2003-2012 Bob Foster. All rights reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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

	/**
	 * Get the text of a node, stripping any trailing whitespace and comment.
	 */
	public static String strip(char[] in, Node node) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < node.length; i++) {
			char c = in[node.offset + i];
			if (c == ' ' || c == '\r' | c == '\n' || c == '\t' || c == '#') break;
			sb.append(c);
		}
		return sb.toString();
	}
	
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

