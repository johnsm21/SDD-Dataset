import os;
import sys;
import pandas;
import pickle;
import random;
random.seed(277419699);
from datetime import datetime, date;

import jnius_config
jnius_config.set_classpath('.', '/Users/mjohnson/Projects/SDD-Dataset/SDD-Validation/SDD_Validation/target/SDD_Validation-0.0.1-SNAPSHOT-jar-with-dependencies.jar')
from jnius import autoclass;

from utility import *;




# Load the dataset
f = open('dataset.pckl', 'rb');
dataset = pickle.load(f);
f.close();

# Load java library
print('\n\n')
PythonIOClass = autoclass('io.PythonIO');
pythonIO = PythonIOClass();

# Analyze the SDDs
# validate SDD
activeDataset = [];
studyCount = 0
for projNum, studyData in dataset.items():
    studyCount = studyCount + 1

    if 'mapping' in studyData:
        datum = {};
        datum['projNum'] = projNum;
        for sddPath, [ddPath, dataPath] in studyData['mapping'].items():
            datum['sddPath'] = sddPath;
            datum['ddPath'] = ddPath;
            datum['dataPath'] = dataPath;

            print('----------------------');
            print('studyCount = ' + str(studyCount))
            print(projNum + ': ' + sddPath);

            analyzer = pythonIO.getAnalyzer(sddPath);
            attCount = analyzer.getAttributeCount();
            ontList = analyzer.getOnts();

            datum['attCount'] = attCount;
            datum['ontList'] = ontList;

            print('Attribute Count: ' + str(attCount));
            print('Ontologies: ' + str(ontList));

            # sys.exit(0);

        activeDataset.append(datum);

# activeDataset = [{'name': 'A', 'attCount': 10}, {'name': 'B', 'attCount': 24}, {'name': 'C', 'attCount': 78}, {'name': 'D', 'attCount': 39}, {'name': 'E', 'attCount': 6}, {'name': 'F', 'attCount': 15}, {'name': 'G', 'attCount': 21}]
# print(activeDataset);

def splitPercent(trainP, testP, dataset, weightPrefix):
    trainP = trainP/100;
    testP = trainP/100;

    trainCount = 0;
    testCount = 0;

    train = [];
    test = [];

    #shuffle dataset
    random.shuffle(dataset);
    random.shuffle(dataset);
    # print(dataset);

    # divide the data
    for datum in dataset:
        # generate stats
        totalCount = trainCount + testCount;

        # always add the first to the training set
        if totalCount == 0:
            # add it to the training set
            train.append(datum);
            trainCount = datum[weightPrefix];

        else:
            actualP = trainCount / totalCount;
            # add the datum to the correct set
            if actualP < trainP:
                # add it to the training set
                train.append(datum);
                trainCount = trainCount + datum[weightPrefix];

            else:

                # add it to the test set
                test.append(datum);
                testCount = testCount + datum[weightPrefix];

    totalCount = trainCount + testCount;
    actTrainP = 100 * (trainCount / totalCount);
    actTestP = 100 * (testCount / totalCount);
    print(str(actTrainP) + ":" + str(actTestP));
    print('Training Count = ' + str(trainCount));
    print('Test Count = ' + str(testCount));
    return [train, test];

[train, test] = splitPercent(70, 30, activeDataset, 'attCount');

# print(train);
# print(test);
