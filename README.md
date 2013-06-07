scalcium
========

A Scala based rewrite of our Concept Mapping (CM2) subsystem, with the following important differences:

+ Written entirely in Scala, a functional/object oriented language which has richer constructs compared to Java, hence the code is terser, and the functional style is better suited for the CM2 algorithms.
+ Uses OpenNLP language models for tokenization, phrase chunking and POS tagging, thus folding in linguistics into our text processing. This layer can be optionally switched to use LingPipe if desired (and if allowed by the license).
+ Has a very intuitive base API inspired by NLTK (the famous Python Natural Language Tool Kit). We wrap complex calls to OpenNLP and LingPipe to expose NLTK like semantics to the rest of scalcium.
+ Uses an easier to understand (in my opinion) pipeline strategy where the decision of which component to turn on and off, and even the behavior of each component, is completely driven by configuration (cm2.cf switches).
+ The pipeline itself is a list of Functions, which are a first class concept in Scala, rather than Java objects which wrapped methods.
+ Algorithms are completely disjoint from the pipeline, enabling individual algorithms to be unit tested. My intent is to have unit tests for every component in the system so regression testing new features is more reliable and painless. The pipeline calls the algorithms through a well defined API.
+ Tokenization of incoming steps happens in steps, where the text is decomposed into paragraphs, then sentences, then phrases. At each step, algorithms can be called to populate a simple Doc object. The linear flow is well-suited to iterative changes/improvements common at Healthline.

