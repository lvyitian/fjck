# Fjck

A Brainfuck to JVM bytecode compiler

## Why?

I wanted an excuse to start playing with [ASM](http://asm.ow2.org/) to generating and running bytecode at runtime, and Brainfuck is a straightforward and slightly amusing language to start with.

## What works?

I added a few example applications to the resources, which all appear to work. Anything beyond that is thus far untested.

There is a very simple interpreter as well (which basically just runs the AST directly), though I'll probably just strip it out completely.

## What needs to be done?

Code cleanup - there's some lingering cruft to be dealt with, but nothing too horrible pilling up (yet).

Tests of any kind are thus far non-existent.

There's a simple optimization pass, but there's definately room for more.
