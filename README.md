# Open Domain Question Answering over Hundreds of Linked Open Datasets

An open domain Question Answering system over Linked Open Data. 
It is able to answer mainly three kinds of questions: factoid, confirmation, and definition questions. 
The distinctive feature of this system is that it can answer questions over millions of entities, by exploiting 
hundreds of Linked Data sources simultaneously, without having to use any training data.
	
The question answering process comprises three main phases: (i) Question Analysis (QA), (ii) Entities Detection (ED), 
and (iii) Answer Extraction (AE).

The system follows a variety of methods including methods for question cleaning, heuristic based question type identification,
 entity recognition, linking and disambiguation using Linked Data-based methods and pure NLP methods (specifically DBpedia Spotlight
and Stanford CoreNLP), WordNet-based question expansion for tackling the lexical gap (between the input question and the underlying sources),
 and triple scoring for producing the final answer.	

## Demo

A demo application is accesible from the following link: http://83.212.101.193:8080/LODQA/DemoQuestions.

A tutorial video is accesible from the following link: https://youtu.be/bSbKLlQBukk.

 
## Source code

The main implementation of the system is under the following path: /src/main/java/gr/forth/ics/isl/demoExternal

And is organized into 4 main folders: 

(1) LODsyndesis: contains java classes responsible for the interaction with LODsyndesis services, using the providing rest api.

(2) core: contains the java classes for the core implementation of the core components Question Analysis, Entities Detection, Answer extraction.

(3)	evaluation: contains the java classes for evaluating the QA process as well as for statistics generation.

(4) main: contains the java classes for the execution of the QA process, i.e. submit a question in Natural Language and get an answer.

## Installation

The project is organized as a maven project and can be downloaded and installed using the maven commands.
The project can be exploited as a .jar file either (i) as an executable or as a library. 

In order to run the project, the user has to use the .java file in the path:
src/main/java/gr/forth/ics/isl/demoExternal/main/ExternalKnowledgeDemoMain.java

By creating an instance of ExternalKnowledgeDemoMain, the user can exploit the provided function (getAnswerAsJson),
for submitting a question in Natural Language and retrieve an answer in JSON format containing relevant information.
The information include: question type, question entities, answer triple, provenance, confidence score, etc.
 

# On Ahcieving High Quality User Reviews Retrieval in the context of Conversational Faceted Search
A User Review Retrieval model for reviews in Focus (input uris).
The model can be instantiated with the provided rdf in-domain KB (a set of products with their associated reviews/comments).
At run time it accepts as input: 
 1) uris, which can be reffered to products or other real world entities that are associated with a set of user reviews/comments.
 2) Natural Language Posed Question.

The model scores the provided reviews and responds with a sorted list of reviews based on their relevance with the input question.
The scoring procedure is done by a combinatorial model based on Wordnet dictionary and Word2vec model. Additionally, functionallities for
a greedy Statistical Query Expansion technique and a Query Reweightening technique based on a set of user provided words that describe the general context of the domain is also supported and can be used or not by setting the required properties.  

## Demo
Demo request to web service:
http://139.91.183.46:8080/QuestionAnswering/service/find/get?query=Is%20the%20hotel%20staff%20helpful?&target_selection=http://ics.forth.gr/isl/hippalus/%23hotel_monte_hermana_kobe_amalie,http://ics.forth.gr/isl/hippalus/%23hotel_monterey_grasmere_osaka,http://ics.forth.gr/isl/hippalus/%23hotel_monterey_hanzomon,http://ics.forth.gr/isl/hippalus/%23hotel_monterey_osaka

## Source code
The main implementation of the system is under the following path: /src/main/java/gr/forth/ics/isl/demo
Its main components are:
 1) models: folder the holds all the main review retrieval models
 2) main: folder which holds the Review Retrieval model based on user defined properties (based on the models in the previous folder "models").
 3) evaluation: folder which holds the test suits for the experiments as well as the java classes needed to support them.
 
## Installation
The project is organized as a maven project and can be downloaded and installed using the maven commands.
The project can be exploited as a .jar file either as an executable or as a library. 

In order to run the project, the user has to use the .java file in the path:
src/main/java/gr/forth/ics/isl/demo/main/OnFocusRRR.java

By creating an instance of OnFocusRRR, the user can exploit the provided functions (getTopKComments, getTop2Comments),
for submitting a question in Natural Language as well as an ArrayList of string uris of interest and retrieve an answer in JSON format containing relevant information.

The information include:
 1) maxSentences: a JSONArray that contains the maxed scored sentence of each retrieved review sorted with respect to the result list. (used in the example above)
 2) commentIds: a JSONArray that contains  a unique identifier of each retrieved review sorted with respect to the result list.
 3) dates: a JSONArray that contains the publication date of each retrieved review sorted with respect to the result list.
 4) fullReview: a JSONArray that contains the full text of each retrieved review sorted with respect to the result list.
 5) posParts: a JSONArray that contains the positive part of each retrieved review sorted with respect to the result list.
 6) negParts: a JSONArray that contains the negative part  of each retrieved review sorted with respect to the result list.
 7) scores: a JSONArray that contains the score of each retrieved review based on its maxed scored sentence and sorted with respect to the result list.
 8) hotelIds: a JSONArray that contains the hotel id of each retrieved review sorted with respect to the result list.
 9) hotelNames: a JSONArray that contains the hotel name of each retrieved review sorted with respect to the result list.

### Requierements
 - JDK:https://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html
 - Maven:https://maven.apache.org/install.html
 - Wordnet:https://wordnet.princeton.edu/download/current-version
 - Word2vec Pre-trained Model:https://deeplearning4jblob.blob.core.windows.net/resources/wordvectors/GoogleNews-vectors-negative300.bin.gz
 - Minimum RAM requirement: 11 GB

### Note
The jar will retrieve the rdf (.ttl, .owl, .rdf etc.) data from the src/main/resources/warehouse/ folder’s files and the model will use the properties directly from the src/main/resources/configuration/onFocusRRRConfig.properties file. To change these values, replacement of the warehouse and the onFocusRRRConfig.properties content should be done and rebuild the project. 
