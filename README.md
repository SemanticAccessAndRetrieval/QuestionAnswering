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
 

