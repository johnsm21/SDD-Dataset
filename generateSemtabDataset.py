import pickle
import os
import sys

import jnius_config
jnius_config.set_classpath('.', '/Users/mjohnson/Projects/SDD-Dataset/SDD-Validation/SDD_Validation/target/SDD_Validation-0.0.1-SNAPSHOT-jar-with-dependencies.jar')
from jnius import autoclass;

datasetFile = 'data/SDD-Dataset/2023-10-30/test.pckl';
outputFolder = 'data/SDD-Dataset/2023-10-30/'

# Load dataset
f = open(datasetFile, 'rb');
dataset = pickle.load(f);
f.close();

# Load java library
print('\n\n')
DataClass = autoclass('data.Data');
DDClass = autoclass('data.DataDictionary');
SDDClass = autoclass('data.SemanticDataDictionary');

# Load Alignment Dictionary
dictPath = os.path.join(outputFolder, "sdd2wikidata.csv");
dictFile = open(dictPath, "r");
dict = {};
isFirst = True;
for line in dictFile:
    # print(cells);
    if isFirst:
        isFirst = False;

    else:
        cells = line.split(",");
        if len(cells) > 1:
            dict[cells[0]] = cells[1];

dictFile.close();
# print(dict);

# setup file directory
folders = os.listdir(outputFolder);
num = 0;
for item in folders:
    if "output-" in item:
        num = num + 1;
outputName = "output-" + f"{num:02}";
outputPath = os.path.join(outputFolder, outputName)
os.mkdir(outputPath);

tablePath = os.path.join(outputPath, "tables");
targetPath = os.path.join(outputPath, "target");
validPath = os.path.join(outputPath, "valid");

os.mkdir(tablePath);
os.mkdir(targetPath);
os.mkdir(validPath);

# Open target file
targetFilePath = os.path.join(targetPath, "cta_target.csv");
targetFile = open(targetFilePath, "w")

for datum in dataset:
    print('----------------------')
    print(datum['projNum'])
    print(datum['sddPath'])
    print(datum['ddPath'])
    print(datum['dataPath'])
    print(datum['attCount'])
    print(datum['ontList'])


    # Parse artifacts
    data = DataClass(datum['dataPath']);
    dd = DDClass(datum['ddPath']);
    sdd = SDDClass(datum['sddPath']);

    # Generate new tables
    tableName = data.getTableName() + ".csv";
    data.enrichData(dd);
    data.inflateData();
    tableFilePath = os.path.join(tablePath, tableName);
    data.writeToCSV(tableFilePath);

    # Generate CTA targets
    result = sdd.generateCTATarget(data);
    # print(result);
    for line in result:
        cells = line.split(",");
        if len(cells) != 3:
            targetFile.close();
            print('Bad CTA Target: ' + line);
            sys.exit(1);

        if not cells[2] in dict:
            targetFile.close();
            print('Missing Mapping: ' + cells[2]);
            sys.exit(1);

        targetFile.write(cells[0] + "," + cells[1] + "," + dict[cells[2]] + "\n");


    print('---------------------- \n')

    # break;

targetFile.close();
