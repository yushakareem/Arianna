# Arianna+

Arianna enables human activity recognition by using a network of ontologies that allow computationally-scalable reasoning. Ontologies are based on Description-Logics which is a decidable fragment of the family of formal logics. Protege is the editor used to create ontologies and SWRL-rules within the ontologies, which are used together for knowledge representation of the environment. Object oriented programming (Java - Kotlin) is used for, 
1. easily linking the ontologies together to form a network, 
2. linking the network of ontologies to a (MySql) database and consuming sensor data from the DB at a set frequency, and finally, 
3. manipulation of knowledge within the network of ontologies (based on the sensor data from DB) and reasoning over them. Hence having specifically human activity recognition and in general context awareness.

Keeping these three points in mind. The idea is to eventually have a system (Arianna) that can be used by developers to quickly build smart environments, i.e., the developers can (1) deploy physical sensors in the environment, (2) build or modify available ontologies using protege, and finally (3) link the ontologies together to form the network of ontologies. Afterwhich the developer runs the system and voila!, human activity recognition and context-awareness begins, i.e., sensors data aquisition, manipulation of knowledge within the network of ontologies based on the sensor data, multi-threading of ontologies is all taken care of by Arianna behind the scenes. 

Furthermore, Arianna becomes a base on which further work can be done with respect to taking uncertainty into account, via integration of fuzzy or probabilistic approaches.
