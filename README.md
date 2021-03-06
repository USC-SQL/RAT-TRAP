# RAT-TRAP: A Tool of Automated Optimization of Resource Inefficient Database Writes for Mobile Applications

Developers strive to build feature-filled apps that are responsive and consume as few resources as possible. Most of these apps make use of local databases to store and access data locally. Prior work has found that local database services have become one of the major drivers of a mobile device’s resource consumption. In this paper we propose an approach to reduce the energy consumption and improve runtime performance of database operations in Android apps by optimizing inefficient database writes. Our approach au- tomatically detects database writes that happen within loops and that will trigger inefficient autocommit behaviors. Our approach then uses additional analyses to identify those that are optimizable and rewrites the code so that it is more efficient. We evaluated our approach on a set of marketplace Android apps and found it could reduce the energy and runtime of events containing the inefficient database writes by 25% to 90% and needed, on average, thirty-six seconds to analyze and transform each app. More algorithmic details of RAT-TRAP can be found in our paper:

```
Remove RATs from Your Code: Automated Optimization of Resource Inefficient Database Writes for Mobile Applications
Yingjun Lyu, Ding Li, William G. J. Halfond
In Proceedings of the ACM SIGSOFT International Symposium on Software Testing and Analysis (ISSTA). April 2018.
```

## Replication Package
All commands were run in Ubuntu 14.04 with Java 1.7 in our evaluation.

Commands: 
1. cd scripts
2. ./printResult.sh

By running the shell script "printResult.sh" in the scripts folder, we can obtain all the statistics presented in the paper. The file [result.txt] generated in the root folder is the output.
The prerun result is in the file [result-origin.txt]. The program [DataCollection.java] used in the script analyzes the various analysis results and summarizes them.

### Experiment 1
- The statistics presented in this experiment are shown in [result.txt] under the line "Results in section 6.3". 
- The static analysis results generated by RAT-TRAP are stored in the file [RAT-TRAP.txt] under the raw_data folder. (Note that RAT and ARAT have different acronyms in the file. They are called HP and AHP in the file.)
- (Optional) To reproduce the results, please download the 1,887 apps provided in [app_list.txt], follow the same folder structure as App1 and generate the classlist.txt for each app (One way to generate the classlist.txt is to use soot and run: java -jar soot-trunk.jar -android-jars path-to/android--1 -src-prec apk -f J -process-dir targetApp.apk -allow-phantom-refs, and then print the names of all the .jimple files to classlist.txt).
After getting the apps, the next step is to go to the scripts folder, and run the script "run.sh". Note that this can take one day to process. It will generate the file [RAT-TRAP.txt] in the raw_data folder. The prerun result is in the file [RAT-TRAP-origin.txt]
- The source code is in the source_code folder. (We prefer to import to Eclipse and build.)


### Experiment 2
- The energy and runtime data is summarized in the sheet [PeformanceEvaluation.ods] under the raw_data folder.
- The figures are in the sheet [result.ods] under the raw_data folder.
- Download the raw data (folder name: AndroidApp) from https://drive.google.com/open?id=1IR-dNtf3pDgAnFDTYSQ9V0_OHYlF84x4 and place it under the raw_data folder.
- Raw data of the measurements is in the folder raw_data/AndroidApp/AppX/data/opt and raw_data/AndroidApp/AppX/data/unopt. Under each folder in opt (optimized version) and unopt (unoptimized version), there are folders 1, 2, 3, 4. They stand for the indexes of input numbers. 
In the logcat.txt, timestamps are shown as I/System.out(XXX): StartActionXXXXX, I/System.out(XXX): EndActionXXXXX, I/System.out(XXX): StartLoopXXXXX and I/System.out(XXX): EndLoopXXXXX
The number after StartLoop/EndLoop/StartAction/EndAction is the timestamp in ms.
- To parse the logcat.txt and the energy file for each input number, go to the scripts folder and run "parse.sh". It will generate a file [energyruntime.txt] in the raw_data folder. It contains the data that is summarized in the sheet [PeformanceEvaluation.ods].
- (Optional) To perform the instrumentation and transformation, go to the scripts folder and run the script "instrument.sh". It will generate the instrumented and transformed apps under the folders 
raw_data/AndroidApp/AppX/opt and raw_data/AndroidApp/AppX/unopt

### Experiment 3
- The statistics presented in this experiment are shown in [result.txt] under the line "Results in section 6.5". 
- (Optional) To generate the raw data, go to the scripts folder and run the script "collectTime.sh". It will generate a file [AnalysisTime.txt] in the raw_data folder. It contains the analysis time that RAT-TRAP takes to analyze and transform each subject app. The prerun result is in the file [AnalysisTime-origin.txt]. Note that this experiment aims at estimating the analysis time of RAT-TRAP. The analysis time obtained from a different machine may not be identical.  
