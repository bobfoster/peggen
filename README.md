peggen
======

Yet another PEG parser generator for Java. See http://bford.info/packrat/.

peggen is a tool for writing DSLs.

PEG Grammar Syntax
==================
  
PEG parsers can be written in Bryan Ford's original notation, using <-
to define rules and / for alternatives, or in a slightly more readable
notation using = and |. Here is the same rule written both ways.

    rule <- term1 / term2
  
    rule = term1 | term2

(Yes, you can mix and match, but that would be crazy.)

There is actually a third definition operator, `::=`, used to write BNF-style
grammars. We will discuss `::=` separately.

Rule names are not case-sensitive. If you want to follow the practice
of writing "lexical" rules in CAPS and "parse" rules in lowercase,
that's fine, but it means nothing to peggen. There is no distinction
between lexing and parsing in PEG.

A very general description of rule syntax that does not deal with
whitespace and thus is NOT a PEG grammar is:

    grammar     = rule+
    rule        = rule-head ('<-' | '=' | '::=') rule-body
    rule-head   = rule-name qualifier?
    rule-name   = identifier
    rule=body   = alternative (('/' | '|') alternative)*
    alternative = term+
    term        = ('!' | '&')? unit
    unit        = atom  ('?' | '*' | '+')?
    atom        = '.' | literal | set | identifier | '(' rule-body ')'

The . atom matches any single character. The idom !. matches no
character and hence only matches the end of input. One !. appears
at the end of the first rule of most grammars.

A literal is one or more characters in single quotes, like 'this'.

A set is a sequence of characters and ranges enclosed in [ ] brackets, like [_0-9a-fA-F].

An identifier appearing in a rule body always identifies a rule in the grammar.

The ?, * and + postfix operators mean optional, zero or more and one or more,
respectively.

The prefix operators ! and & are PEG lookahead operators:

  !term attempts to match the term. If the match succeeds, ! fails; if it
  fails, ! succeeds. In both cases, the input position is reset to its
  position on encountering the !.
  
  &term attempts to match the term. If the match succeeds, & succeeds; if
  it fails, & fails. In both cases, the input position is reset to its
  position on encountering the &.

An alternative is simply one term written after another, with whitespace
(not shown in the grammar above!) as needed to separate identifiers.

A rule body is evaluated by attempting to match the first alternative. If
that fails, the second alternative is attempted. And so on until either
an alternative succeeds, in which case the rule succeeds and further
alternatives are not evaluated, or all alternatives fail, in which case
the rule fails. RULES NEVER BACKTRACK. (If you come to peggen from
regular expressions or other parser generators or, worst of all, a
classical education in the Chomsky hierarchy, this will be the hardest
concept to internalize.)

A rule qualifier is ~ or ~n, where ~ means the rule is never represented
in the output tree, and ~n, where n is an integer, means the rule is only
represented in the output tree if it has n or more children.

A rule may be written on any number of lines.

Whitespace (not shown in the grammar above!) may appear between any two
operators or terms.

\# (not shown in the grammar above!) begins a comment that continues
until the end of the line. \# comments are treated as whitespace.

The first rule in a grammar is the start rule. If your real start rule
is somewhere in the middle, add another rule that calls it and put
that rule at the beginning.

The best way to come to grips with peggen is to look at some examples
that work. E.g., see the calc project also on GitHub.

What Comes Out The Other End
============================

Generate a parser from your grammar; feed it language
input as a String or byte array.

You get back an array of org.genantics.peggen.Node or null. If null, there
is a separate method to fetch a List of error messages. The array contains
a tree, your Abstract Syntax Tree (AST). The first element in the array
is the head of the tree.

Node is the only "library code" you need. It is packaged separately, in
its own little peggen-node jar, to minimize the footprint of your application.
Node is actually a struct written in Java. The part of the definition you care 
about is:

    public class Node {
	  public String name;
      public int offset;
	  public int length;
	  public Node parent;
	  public Node child;
	  public Node next;
	  ...
	}

* `name` is the name of the rule that matched and produced this node.
  name fields are always set with String literals. If you remember
  your Java specification, String literals are interned by the JVM
  and it is safe to compare them with == (to other String literals),
  though you don't have to.
* `offset` and `length` are the zero-based position and length
  of the characters in the string input that matched the rule.
  You will need the input string to recover any String values you need
  to interpret the tree.
* `parent` is the parent node, null for the head node.
* `child` is the first child of the current node or null
* `next` is the next sibling of the current node or null

In a naive grammar, that is one without any ~ annotations, there will be
a node for every rule matched by the parse. ~ annotations (see the
preceding section) have the ability to suppress nodes that aren't
significant to the interpretation of the tree.

For example, here's a little grammar:

    gram = add !.
    add  = mul ('+' mul)*
    mul  = term ('*' term)*
    term = num | '(' add ')'
    num  = [0-9]+ '.' [0-9]+

(The gram rule's sole purpose is to ensure that the grammar matches the
entire input, not just a leading substring of it. The name of the rule
is not significant, but its position is. peggen starts parsing from the first rule.)

If you generate a parser from this and feed the parser the input
"3+4*5" you will get back a tree of Nodes with names like this (indentation
indicates parent/child):

    "gram"
      "add"
        "mul"
          "term"
            "num" (3)
        "mul"
          "term"
            "num" (4)
          "term"
            "num" (5)

You will note there is considerable clutter in the tree. The gram and term nodes
add no information at all, and a mul with only one child signifies nothing.

If you mark up the grammar as follows:

    gram ~   = add !.
    add  ~2  = mul ('+' mul)*
    mul  ~2  = term ('*' term)*
    term ~   = num | '(' add ')'
    num      = [0-9]+ '.' [0-9]+

and repeat the experiment, you will get back a tree like this:

    "add"
      "num" (3)
      "mul"
        "num" (4)
        "num" (5)

Which is the tree you would draw on the blackboard as:

        +
       / \
      3   *
         / \
        4   5

Interpreting parse trees is quite simple, though not at all object-oriented.
Usually, one writes a visitor with a method for each non-~ rule in the
grammar, and a dispatch rule that keys off the names of the nodes. For the
above grammar there are only three rules.

    double eval(Node node) {
      if (node.name == "add")  // yes, this is valid*
        return evalAdd(node);
      if (node.name == "mul")
        return evalMul(node);
      if (node.name == "num")
        return evalNum(node);
    }
    double evalAdd(Node node) {
      double n = eval(node.child);
      for (Node child = node.child.next; child != null; child = child.next)
    	n += eval(child);
      return n;
    }
    double evalMul(Node node) {
      double n = eval(node.child);
      for (Node child = node.child.next; child != null; child = child.next)
    	n *= eval(child);
      return n;
    }
    double evalNum(Node node) {
      String num = inString.substring(node.offset, node.offset+node.length);
      return Double.parseDouble(num);
    }

\* Java literal strings are interned. Node names are always assigned values
  from literals.

Note that the original input, as a String or char array, is needed to
extract literals (or in DSLs, identifiers). A Node only has offsets into 
this String or array.

(In the above, we wrote the ~ annotation in a separate column for neatness.
This whitespace is, of course, not significant. Any of these would be valid:

    add ~ 2 = mul ('+' mul)*
    add  ~2 = mul ('+' mul)*
    add~2 = mul ('+' mul)*
    add~2=mul('+'mul)*

but it is very important you be able to read your grammars!)
    
Dealing With Whitespace
=======================

The grammar above allows no whitespace between numbers and operators.
In a PEG grammar, whitespace between any two symbols could be allowed
by modifying the grammar as follows:

	gram ~   = S add !.
    add  ~2  = mul ('+' S mul)* !.
    mul  ~2  = term ('*' S term)*
    term ~   = num S | '(' S add ')' S
    num      = [0-9]+ '.' [0-9]+
    S    ~   = [ \t\r\n]*

Since the added S rule is marked with ~, it will not
appear in the output tree, which will remain as discussed above.

An alternative approach, suggested by Bryan Ford, is to separate
rules into two classes, only one of which deals with whitespace.
For example:

	GRAM   ~   = S add !.
    add    ~2  = mul (PLUS mul)* !.
    mul    ~2  = term (TIMES term)*
    term   ~   = NUM | LPAREN add RPAREN
    num        = [0-9]+ '.' [0-9]+
    S      ~   = [ \t\r\n]*
    PLUS   ~   = '+' S
    TIMES  ~   = '*' S
    NUM    ~   = num S
    LPAREN ~   = '(' S
    RPAREN ~   = ')' S

Both approaches have drawbacks. The first grammar is cluttered with
whitespace annotation and is error-prone. The second nearly
doubles the size of the example grammar while making it somewhat less readable.
More complex grammars can be even more muddled.

Error Handling
==============

The first thing to say about error handling is, peggen isn't very good
at it. When a grammar doesn't parse, peggen and its generated grammars
produce one "Syntax error" with a pointer to the position in the input
where the error was detected. When a grammar uses undefined rules,
peggen produces one error message per undefined rule, indicating the
position at which it was first used.

It is possible to improve, at least, the number of errors detected,
by using the special $Error rule. $Error always matches and, as a
side effect, records the current input position in a list of syntax
errors. $Error should always be the last alternative in a rule that
is appropriate for error recovery. For example, if the grammar has
statements terminated by semicolon, like this,

    body  = (statement ';')*
    
one possible recovery might be:

    body  = (statement ';' | $Error (!';' .)* ';')*

But, in general, recovery will be more complex. The above, for
example, will stop in the middle of "a;b".

Warning: This has not been well-tested!

Known Bugs
==========

- I noticed a number of anomalies in string escapes. What seems to work reliably
  is using set expressions, e.g., [\t] instead of '\t'.

- I have a grammar for which two rules (out of hundreds) that are defined are reported
  as undefined.
  
These bugs are making me rethink the abandoned bootstrapped parser.

Miscellaneous Notes
===================

- No attempt has been made to peephole optimize the generated code.
  It follows simple templates that implement PEG rules and terms, so we
  have high confidence code generation is correct.
  
- It is not possible to insert Java code in the generated parser.
  All parsers generate a tree of org.genantics.peggen.Node. Applications
  interpret the tree to evaluate the parse. Node is the only "library code"
  you will want or need.
  
  Because of this, you can actually read peggen grammars and, because
  you don't have code in them, you don't have to debug generated parsers.

- Left-recursive rules are not supported. We are aware of OMeta and
  other parsers that have shoehorned special cases of left recursion
  into PEG, but no.
  
- There are, as far as we know, no restrictions on PEG as defined by
  Ford. Packrat parsing is another matter. Rather than accept the memory
  overhead of packrat, we have implemented a "mini-packrat"
  scheme that provides some of the performance benefits of packrat
  with almost no overhead. It is particularly good at optimizing the
  common idiom:
  
    `rule = !term thisrule | term thatrule`

  where term is an arbitrarily complex lookahead used to disambiguate
  two alternatives that start with the same sequence.

- We extend Ford's syntax to allow grammar writers to indicate
  rules that are to be pruned from the
  output tree. A ~ written after a rule name definition means the rule is never
  included in the tree. A ~n, where n is an integer, means the
  rule is only included in the tree if it would have at least
  n children.
  
  The latter turns out to be very useful in avoiding the blizzard
  of inessential tree nodes that result from expressing operator
  precedence by nested rules.
  
-  Literals, including quoted strings like 'a' and sets/ranges like
  [0-9a-fA-F], and the . which matches any character, are not rules
  and never appear in the output tree. If you want to know that
  a particular literal was matched, wrap it in a rule.

- PEG grammars are composable (unlike context-free grammars) and
  the ability to include grammar fragments would be useful. However,
  it also makes grammars harder to understand - composing grammars
  can alter the plain meaning of rules you only think you understand -
  so in balance, we left it out.

- Yes, it is possible to describe the pure PEG subset of peggen grammars in peggen.
  In fact,
  I once bootstrapped peggen so that it generated its own parser.
  That was definitely NOT the simplest thing that could work (!)
  so I abandoned it.
  
  Building peggen
  ===============
  
  The three peggen projects must be built in this order:
  
       peggen-node
       peggen
       peggen-maven-plugin
  
  Using
  
  ```
  mvn clean install
  ```
  
  Thanks to whomever told me ~~I was an idiot~~ that peggen-node and peggen-maven-plugin
  had circular dependencies. Unfortunately, it all built for me because I didn't start 
  with a clean .m2, so I ~~was a moron and~~ didn't fix it until just now. Sorry.
  
  The fix removed what was intended to be a working example that somehow got jumbled
  into peggen-node. Will restore as peggen-calc.
  
  Bob Foster
  July 29, 2012
  
  Last modified January 4, 2016
