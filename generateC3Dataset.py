import pickle;
import sys;

import jnius_config;
jnius_config.set_classpath('.', '/Users/mjohnson/Projects/SDD-Dataset/SDD-Validation/SDD_Validation/target/SDD_Validation-0.0.1-SNAPSHOT-jar-with-dependencies.jar');
from jnius import autoclass;
## This script takes the C1 train and test sets, pull mappings and orginal descriptions and generates a validation set.
# parameters
n = 5;
outputPath = 'c3RawDataset.pckl';
trainRatio = 0.5;

trainSet = [];
validSet = [];
tesSet = [];

ddKey = 'dd-path';
sddKey = 'sdd-path';
ontKey = 'ontology-list';
varKey = 'variables';
colKey = 'column-name';
dspKey = 'description';
ctKey = 'column-type';
cbKey = 'codebook';
daKey = 'data-example';
# [
#     dd-sdd-mapping{
#         dd-path
#         sdd-path
#         ontology-list []
#         variables[
#             {
#               column-name
#               description
#               column-type []
#               codebook {}
#               data-example []
#             }
#          ]
#     }
# ]

def parseUniqueTableValues(data, n):
    tables = {};
    # sheets = data._data.keySet();
    for sheet in data._data.keySet():
        tableSheet = {};
        table = data._data.get(sheet);
        for var in table.keySet():
            tableSheet[var]=[];
            colList = table.get(var);
            for datum in colList:
                # print(datum)
                # empty string = false
                if datum:
                    # add the data if its unique
                    if datum not in tableSheet[var]:
                        tableSheet[var].append(datum);

                        # Check to see if we can stop early
                        if len(tableSheet[var]) == n:
                            break;
        tables[sheet] = tableSheet;
    return tables;


# converts a dds java map into python dict
def parseDDCodebook(dd):
    codebook = {};
    javaCodebook = dd._cb._codebook;
    for var in javaCodebook.keySet():
        codeMap = {};
        codeJavaMap = javaCodebook.get(var);
        for code in codeJavaMap.keySet():
            codeMap[code] = codeJavaMap.get(code);

        codebook[var] = codeMap;

    return codebook;


# Takes in a column name, an SDD and returns an SDDVariable list
def getSDDVariable(colname, sdd):
    varList = []
    for sddVar in sdd._variables:
        if colname == sddVar._name:
            varList.append(sddVar);
    return varList;

# Takes in a column name, an DD and returns an DDVariable list
def getDDVariable(colname, dd):
    varList = []
    for ddVar in dd._variables:
        if colname == ddVar._name:
            varList.append(ddVar);
    return varList;

# Load data and n example data
def importData(dataPath, n):
    # Load constants
    global ddKey;
    global sddKey;
    global ontKey;
    global varKey;
    global colKey;
    global dspKey;
    global ctKey;
    global cbKey;
    global daKey;

    # Load classes
    DDClass = autoclass('data.DataDictionary');
    SDDClass = autoclass('data.SemanticDataDictionary');
    DataClass = autoclass('data.Data');


    # load old data format
    f = open(dataPath, 'rb');
    data = pickle.load(f);
    f.close();
    del f;

    # load and combine data
    dataset = [];
    for dataMapping in data:
        # print('---------dataMapping----------');
        # print(dataMapping);
        # print('-------------------');

        # Intialize metadata
        mapping = {};
        mapping[ddKey] = dataMapping['ddPath'];
        mapping[sddKey] = dataMapping['sddPath'];
        mapping[ontKey] = dataMapping['ontList'];

        # Load and parse DD and SDD
        dd = DDClass(mapping[ddKey]);
        sdd = SDDClass(mapping[sddKey]);

        # parse the table
        table = parseUniqueTableValues(DataClass(dataMapping['dataPath']), n);

        # parse the codebook
        ## dd = DDClass('data/HHEAR-Studies/2023-06-16_clean/2017-2121/2121_EPI_DDCB.xlsx');
        codebook = parseDDCodebook(dd);

        # Pull DD, SDD, and table data
        mapping[varKey] = [];
        # for var in dd._variables:
        for var in sdd._variables:
            # only operate over explicit variables
            if not var.getName().startswith('??'):
                datum = {};
                datum[colKey] = var._name;

                # Get DD Description
                ddVarList = getDDVariable(datum[colKey], dd);

                # check if we didn't find the right number of columns
                if len(ddVarList) != 1:
                    if len(ddVarList) < 1:
                        print('Error: No dd variable found for ' + datum[colKey]);

                    else:
                        print('Error: Multiple dd variables found for ' + datum[colKey]);

                    print('dd path: ' + dataMapping['ddPath']);
                    print('sdd path: ' + dataMapping['sddPath']);
                    sys.exit(1);
                datum[dspKey] = ddVarList[0]._desc;
                del ddVarList;

                # Get CTA mappings
                # convert from java to python list
                javaCTLIst = var._att;
                datum[ctKey] = [];
                for ct in javaCTLIst:
                    datum[ctKey].append(ct);
                del javaCTLIst;

                # check to make sure we have at least one cta mapping
                if len(datum[ctKey]) < 1:
                    print('Error: Missing cta mapping for ' + datum[colKey]);
                    print('dd path: ' + dataMapping['ddPath']);
                    print('sdd path: ' + dataMapping['sddPath']);
                    sys.exit(1);

                # Get the codebook for the variable or get example data
                if datum[colKey] in codebook:
                    # a codebook exists so copy it
                    datum[cbKey] = codebook[datum[colKey]];

                else:
                    # codebook doesn't exist so grab the example data
                    for sheetName, sheet in  table.items():
                        if datum[colKey] in sheet:
                            datum[daKey] = sheet[datum[colKey]];
                            # We can stop searching once we find it
                            break;

                mapping[varKey].append(datum);

        dataset.append(mapping);

    return dataset;

def splitDataset(dataset, trainRatio, testCount):
    trainSet = [];
    validSet = [];
    trainCount = 0;
    validCount = 0;
    for mapping in dataset:
        # generate stats
        totalCount = trainCount + validCount + testCount;

        # always add the first to the training set
        if totalCount == 0:
            # add it to the training set
            trainSet.append(mapping);
            trainCount = len(mapping[varKey]);

        else:
            actualRatio = trainCount / totalCount;
            # add the datum to the correct set
            if actualRatio < trainRatio:
                # add it to the training set
                trainSet.append(mapping);
                trainCount = trainCount + len(mapping[varKey]);

            else:

                # add it to the test set
                validSet.append(mapping);
                validCount = validCount + len(mapping[varKey]);

    totalCount = trainCount + validCount + testCount;
    print('trainCount = ' + str(trainCount));
    print('validCount = ' + str(validCount));
    print('totalCount = ' + str(totalCount));
    print('train ratio = ' + str(trainCount/totalCount));
    print('valid ratio = ' + str(validCount/totalCount));
    print('test ratio = ' + str(testCount/totalCount));

    return (trainSet, validSet)

# Load datasets
trainSet = importData('train.pckl', n);
testSet = importData('test.pckl', n);
# print(trainSet);

# get mapping count for testSet
testCount = 0;
for mapping in testSet:
    testCount = testCount + len(mapping[varKey]);

# Split train into train and validation by percentage
(trainSet, validSet) = splitDataset(trainSet, trainRatio, testCount);

# Saving Dataset
f = open(outputPath, 'wb');
pickle.dump((trainSet, validSet, testSet), f);
f.close();
