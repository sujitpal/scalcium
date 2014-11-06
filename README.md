scalcium
========

Started out as a Scala based rewrite of our existing Concept Mapping (CM2) subsystem, abandoned when the project was started independently in-house, then turned into a sandbox in which to try out interesting NLP ideas. Some highlights of this project:

+ Uses OpenNLP/LingPipe/Stanford CoreNLP language models for tokenization, phrase chunking and POS tagging.
+ Has a very intuitive base API inspired by NLTK (the famous Python Natural Language Tool Kit). We wrap complex calls to OpenNLP and LingPipe to expose NLTK like semantics to the rest of scalcium.
+ Uses a pipeline based on UIMAFit.
+ Algorithms are completely decoupled from the pipeline, enabling individual algorithms to be unit tested.

