import os;
import sys;
import pandas;
from datetime import datetime, date;

import jnius_config
# jnius_config.set_classpath('.', '/Users/mjohnson/Projects/SDD-Dataset/SDD-Validation/SDD_Validation/target/classes');
jnius_config.set_classpath('.', '/Users/mjohnson/Projects/SDD-Dataset/SDD-Validation/SDD_Validation/target/SDD_Validation-0.0.1-SNAPSHOT-jar-with-dependencies.jar')
from jnius import autoclass;

from utility import *;

#constants
manifestFilename = 'study-manifest.xlsx';
manifestSheet = 'Sheet1'; # tHis is the sheet with projects files list
projNumIndex = 'Project Number';
projNamIndex = 'Project Name';
fileDoiIndex = 'DOI';
fileTypeIndex = 'File';
fileNameIndex = 'File name';

# Operands
# dataDir = 'data/HHEAR-Studies';
dataDir = 'data/The-Cancer-Genomic-Atlas';

# For each SDD & DD get the latest paths and names
dataset = {}; # { projNumber --> {name, dd, sdd, version}}

# Get the latest version
latest = datetime.min
latest_path = None;
for version in os.listdir(dataDir):
    datetime_version = version.split('_')[0];
    if version != '.DS_Store':
        datetime_version = datetime.strptime(datetime_version, '%Y-%m-%d');
        # print(datetime_version);

        # Extract version expecting folder such as version7
        if datetime_version > latest:
            latest = datetime_version;
            latest_path = version;

latest = date(latest.year,latest.month, latest.day);
print('latest');
print(os.path.join(dataDir, str(latest_path)));

ws = pandas.read_excel(os.path.join(dataDir, latest_path, manifestFilename), manifestSheet);
df = ws[[projNumIndex, projNamIndex, fileDoiIndex, fileTypeIndex, fileNameIndex]];
# print(df);

# {name, [dd], [sdd], [cb] version, [doi], readme}
for i in range(df.shape[0]): #iterate over rows

    # Ignore all PH projects
    fileType = str(df.iat[i, 3]).strip();
    if not fileType == 'PH': # PH files are place holders for studies with no data

        # look to see if we have had this project before
        projNum = str(df.iat[i, 0]).strip();
        if projNum not in dataset:
            dataset[projNum] = {};
            dataset[projNum]['name'] = str(df.iat[i, 1]).strip();
            dataset[projNum]['dd'] = [];
            dataset[projNum]['sdd'] = [];
            dataset[projNum]['cb'] = [];
            dataset[projNum]['version'] = latest;
            dataset[projNum]['doi'] = {};

        # Checks that the file exists
        fileName = str(df.iat[i, 4]).strip();
        filepath = os.path.join(dataDir, latest_path, projNum, fileName);
        print(filepath)
        if os.path.isfile(filepath):
            # Add file specific data
            if fileType == 'DD':
                dataset[projNum]['dd'].append(filepath);

            elif fileType == 'SDD':
                dataset[projNum]['sdd'].append(filepath);

            elif fileType == 'Readme':
                dataset[projNum]['readme'] = filepath;

            elif fileType == 'CB':
                dataset[projNum]['cb'].append(filepath);

            else:
                raise Exception('Unknown filetype [' + fileType + '] in ' + fileName);

            # add DOI if we have it
            fileDoi = str(df.iat[i, 2]).strip();
            # print(fileDoi)
            if fileDoi != 'nan':
                dataset[projNum]['doi'][fileName] = fileDoi;
        else:
            raise Exception('Missing file [' + fileName + '] in ' + filepath);


# Print what we found
print('We found ' + str(len(dataset.keys())) + ' studies!')
# print('We found the following datasets:');
# print(dataset);

# Remove studies without a DD and SDD
removedDataset = {};
for projNum, studyData in dataset.items():
    if len(studyData['dd']) > 0 and len(studyData['sdd']) > 0:
        removedDataset[projNum] = studyData;
dataset = removedDataset;
removedDataset = None;

# Print what we found
print('Reduced down to ' + str(len(dataset.keys())) + ' studies!')
print(dataset);

# dataDictionary = autoclass('DataDictionary');
# print('dataDictionary.getDDPath()');
# print(dataDictionary.getDDPath());

PythonIOClass = autoclass('io.PythonIO');
pythonIO = PythonIOClass();

for projNum, studyData in dataset.items():
    toDoList = [];
    # toDoList = ['2016-1407', '2016-1431', '2016-1432', '2016-1438', '2017-2121'];
    if projNum not in toDoList:
        for ddPath in studyData['dd']:
            print('----------------------');
            print(projNum + ': ' + ddPath);

            # Get Report
            report = pythonIO.validatDD(ddPath);
            if report.isValid():
                # print warning if no errors
                if report._warnings.size() > 0:
                    print('Warnings:');
                    for i in range(report._warnings.size()):
                        print(printCellProv(report._warnings.get(i)));

            else: # Print errors if we find any
                print('Errors found:');
                for i in range(report._errors.size()):
                    print(printCellProv(report._errors.get(i)));

                sys.exit(0); # stop running


            print('----------------------');
