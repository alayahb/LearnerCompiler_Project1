# LearnerCompiler Project 1

COMP 360 - Programming Languages

## Overview

This project implements a lexical analyzer and recursive-descent parser for the LearnCompiler language.

The program reads a sample program, separates the input into lexemes and tokens, and checks whether the program follows the provided BNF grammar.

## Features

- Accepts input manually or from a file
- Generates lexemes and token types
- Displays each lexeme and its corresponding token
- Uses recursive-descent parsing
- Detects syntax errors
- Reports whether the sample program is valid according to the LearnCompiler grammar

## Language Used

Java

## Grammar

The parser is based on the following grammar:

```text
<program> -> <keyword> <ident> (<keyword><ident>) { <declares> <assign> }

<declares> -> <keyword> <ident> ;
            | <keyword> <ident> ; <declares>

<assign> -> <ident> = <expr>

<expr> -> <ident> {*|/} <expr>
        | <ident>

<keyword> -> float
