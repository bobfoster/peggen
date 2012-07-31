peggen
======

Yet another PEG parser generator for Java. See http://bford.info/packrat/.

peggen is intended to be (almost) the simplest thing that could possibly work.
It is a simple tool for writing DSLs, with a much smaller footprint than
alternatives we are aware of.

Grammar Syntax
==============
  
Parsers can be written in Bryan Ford's original notation, using <-
to define rules and / for alternatives, or in a slightly more readable
notation using = and |. Here is the same rule written both ways.

    rule <- term1 / term2
  
    rule = term1 | term2

(Yes, you can mix and match, but that would be crazy.)

Rule names are not case-sensitive. If you want to follow the practice
of writing "lexical" rules in CAPS and "parse" rules in lowercase,
that's fine, but it means nothing to peggen. There is no distinction
between lexing and parsing in PEG.

A very general description of rule syntax that does not deal with
whitespace and thus is NOT a PEG grammar is:

    grammar     = rule+
    rule        = rule-head ('<-' | '=') rule-body
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
represented in the output tree if it has more than n children.

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

You generate a parser from your grammar; you feed it some of your language
input as a String or byte array. What do you get out?

You get an array of org.genantics.peggen.Node or null. If null, there
is a separate method to fetch a List of error messages. The array contains
a tree, your Abstract Syntax Tree (AST). The first element in the array
is the head of the tree.

Node is the only "library code" you need. (It probably should be packaged
separately, to minimize the footprint of your application, but I haven't
gotten around to that.) It's actually a struct written in Java. The part
of the definition you care about is:

    public class Node {
	  public String name;
      public int offset;
	  public int length;
	  public Node parent;
	  public Node child;
	  public Node next;
	  ...
	}

Quelle horreur! No getters and setters! And is just the first heresy.

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
* `next` is the next child of the node's parent or null

In a naive grammar, that is one without any ~ annotations, there will be
a node for every rule matched by the parse. ~ annotations (see the
preceding section) have the ability to suppress nodes that aren't
significant to the interpretation of the tree.

For example, here's a little grammar:

    add  = S? mul ('+' mul)*
    mul  = term ('*' term)*
    term = num S? | '(' S? expr ')' S?
    num  = [0-9]+
    S    = [ \n\t\r]+

If you generate a parser from it and feed the parser the input
"3+4*5" you will get back a tree of Nodes with names like this (indentation
indicates parent/child):

    "add"
      "mul"
        "term"
          "num" (3)
      "mul"
        "term"
          "num" (4)
        "term"
          "num" (5)

(S is never matched in the example input, but it's in the example grammar
to remind you that there's nothing special about whitespace in a PEG.)

If you mark up the grammar as follows:

    add~2  = S? mul ('+' mul)*
    mul~2  = term ('*' term)*
    term~  = num S? | '(' S? expr ')' S?
    num    = [0-9]+
    S~     = [ \n\t\r]+

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
  Ford. Packrat parsing is another matter. Rather than accept the time
  and space complexity of packrat, we have implemented a "mini-packrat"
  scheme that provides some of the performance benefits of packrat
  with almost no overhead. It is particularly good at optimizing the
  common idiom:
  
    rule = !term thisrule | term thatrule

  where term is an arbitrarily complex lookahead used to disambiguate
  two alternatives that start with the same, arbitrarily long sequence 
  of symbols.

- We extend Ford's syntax to allow grammar writers to indicate which
  rules that will, perhaps conditionally, be pruned from the
  output tree. A ~ written after a rule name means the rule is never
  included in the tree. A ~n, where n is an integer, means the
  rule is only included in the tree if it would have at least
  n children.
  
  The latter turns out to be very useful in avoiding the blizzard
  of inessential tree nodes that result from expressing operator
  precedence by recursive rules that most of the time are just
  stepping stones on the way to an interesting rule.
  
-  Literals, including quoted strings like 'a' and sets/ranges like
  [0-9a-fA-F], and the . which matches any character, are not rules
  and never appear in the output tree. If you want to know that
  a particular literal was matched, wrap it in a rule.

- PEG grammars are composable (unlike context-free grammars) and
  the ability to include grammar fragments would be useful. However,
  it also makes grammars harder to understand - composing grammars
  can alter the plain meaning of rules you only think you understand -
  so in balance, we left it out.

- No, it wouldn't be hard to make peggen generate grammars in any
  language. Didn't have the need. Feel free to tackle it.

- Yes, it is possible to describe peggen grammars in peggen. In fact,
  I once bootstrapped peggen so that it generated its own parser.
  This was definitely NOT the simplest thing that could work (!)
  so I abandoned it.
  
  Bob Foster
  July 29, 2012
  
  Last modified July 30, 2012