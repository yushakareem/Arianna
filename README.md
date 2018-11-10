# Arianna+ #

Arianna enables human activity recognition by using a network of ontologies that allow computationally-scalable reasoning. Ontologies are based on Description-Logics which is a decidable fragment of the family of formal logics. Protege is the editor used to create ontologies and SWRL-rules within the ontologies, which are used together for knowledge representation of the environment. Object oriented programming (Java - Kotlin) is used for, 
1. easily linking the ontologies together to form a network, 
2. linking the network of ontologies to a (MySql) database and consuming sensor data from the DB at a set frequency, and finally, 
3. manipulation of knowledge within the network of ontologies (based on the sensors data from DB) and reasoning over them. Hence having, specifically human activity recognition and in general context awareness.

Keeping these three points in mind. The idea is to eventually have a system (Arianna) that can be used by developers to quickly build smart environments, i.e., the developers can deploy physical sensors in the environment; then,
1. build or modify available ontologies using protege, and
2. link the ontologies together to form a network of ontologies.

Afterwhich the developer runs the system and voila! human activity recognition and context-awareness begins. That is, aquisition of sensors data from DB, manipulation of knowledge within the network of ontologies and reasoning based on that knowledge in a multi-threaded way. In essence, the developer is free from developing the third step as mentioned in the first paragraph.

Furthermore, Arianna is intended, to be an explainable AI (Artificial Intelligence/Inference) system and to be a base on which further progress can be made with respect to, nonmonotonic reasoning via integration with fuzzy and/or probabilistic approaches.

## Links for study ##

* Protege [documentation](http://protegeproject.github.io/protege/).
* [Fundamentals](https://github.com/protegeproject/swrlapi/wiki/SWRLLanguageFAQ#Does_SWRL_support_Negation_as_Failure) of SWRL shows the basics of building rules and mentions the idea of negation. Further discussions on negations in SWRL rules can be found [here](http://protege-project.136.n4.nabble.com/Negation-in-SWRL-rules-td4664123.html) and Built-ins for SWRL can be found [here](http://www.daml.org/swrl/proposal/builtins.html).

## Contact for issues ##

kareem.syed.yusha@dibris.unige.it 