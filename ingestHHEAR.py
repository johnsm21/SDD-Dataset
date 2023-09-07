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
dataDir = 'data/HHEAR-Studies';
# dataDir = 'data/The-Cancer-Genomic-Atlas';

# For each SDD & DD get the latest paths and names
dataset = {}; # { projNumber --> {name, dd, sdd, data, version, mapping}}
# mapping --> [{ dd --> data, sdd}]

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
            dataset[projNum]['data'] = [];
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

            elif fileType == 'Data':
                dataset[projNum]['data'].append(filepath);

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

# Remove studies without a DD and SDD and data
removedDataset = {};
for projNum, studyData in dataset.items():
    if len(studyData['dd']) > 0 and len(studyData['sdd']) > 0 and len(studyData['data']) > 0:
        removedDataset[projNum] = studyData;
dataset = removedDataset;
removedDataset = None;

# Print what we found
print('Reduced down to ' + str(len(dataset.keys())) + ' studies!')
print(dataset);

# dataDictionary = autoclass('DataDictionary');
# print('dataDictionary.getDDPath()');
# print(dataDictionary.getDDPath());
print('\n\n')

PythonIOClass = autoclass('io.PythonIO');
pythonIO = PythonIOClass();


# Check that SDD Valid
studyCount = 0
for projNum, studyData in dataset.items():
    studyCount = studyCount + 1
    for sddPath in studyData['sdd']:
        print('----------------------');
        print('studyCount = ' + str(studyCount))
        print(projNum + ': ' + sddPath);

        # Get Report
        report = pythonIO.validatSDD(sddPath);
        print('Warnings:');
        for i in range(report._warnings.size()):
            print(printCellProv(report._warnings.get(i)));



        # Print errors if we find any
        print('Errors found:');
        for i in range(report._errors.size()):
            print(printCellProv(report._errors.get(i)));


        print('----------------------');

        if report._errors.size() > 0:
            sys.exit(0);


sys.exit(0);



## right now we have DD: [Data, SDD]
## But there are many SDDs to a DD and Dataset
## were going to SDD: [DD, DATA]
for projNum, studyData in dataset.items():
    dataset[projNum]['mapping'] = {};
    print('--------------------');
    # Generate a mapping between multiple DDs, SDDs, and Data files
    if (len(dataset[projNum]['data']) == 1) and (len(studyData['dd']) == 1) and (len(studyData['sdd']) == 1):
        # we only have one of each so its obvious
        dataset[projNum]['mapping'][studyData['sdd'][0]] = [dataset[projNum]['dd'][0], dataset[projNum]['data'][0]];
        print(projNum + " single case");

    else:
        # we have multiple sdds

        # we have many sdds but only 1 dd and data
        if (len(dataset[projNum]['data']) == 1) and (len(studyData['dd']) == 1):
            for sdd in studyData['sdd']:
                dataset[projNum]['mapping'][sdd] = [dataset[projNum]['dd'][0], dataset[projNum]['data'][0]];

            print(projNum + " many sdds but only 1 dd and data");

        else:
            # each sdd corresponds to a single other file
            if len(dataset[projNum]['data']) == len(studyData['dd']) == len(studyData['sdd']) :
                print(projNum + " each sdd corresponds to a single other file");
                # print(studyData['sdd']);
                # print(studyData['dd']);
                # print(studyData['data']);
                for sdd in studyData['sdd']:
                    # get file name
                    folders = sdd.split('/');
                    basePath = sdd.replace(folders[-1], '');
                    coreName = folders[-1].split('-');
                    if len(coreName) == 6:
                        coreName = coreName[4];
                        halfNum = projNum.split("-");

                        # get actual project number
                        if (len(halfNum) == 2) or (len(halfNum) == 3):

                            if len(halfNum) == 2:
                                halfNum = halfNum[1];

                            # special case for the one combined project (2018-2517_2020-3131) file we will grab it from the SDD name
                            if len(halfNum) == 3:
                                halfNum = folders[-1].split('-')[2];



                            ddName = basePath + halfNum + "_" + coreName + "_DDCB.xlsx";
                            dataName = basePath + halfNum + "_" + coreName + "_DATA.xlsx";

                            # print(ddName);
                            # print(dataName);

                            # check to make sure dd exists
                            if(ddName not in dataset[projNum]['dd']):
                                print("Missing data filename " + ddName);
                                print("Data file list " + str(dataset[projNum]['dd']));
                                sys.exit(1);

                            # check to make sure data exists
                            if(dataName not in dataset[projNum]['data']):
                                print("Missing data filename " + dataName);
                                print("Data file list " + str(dataset[projNum]['data']));
                                sys.exit(1);


                            dataset[projNum]['mapping'][sdd] = [ddName, dataName];


                        else:
                            print("Bad project number " + projNum);
                            sys.exit(1);
                    else:
                        print("Bad file name: " + sdd);
                        sys.exit(1);

            else:

                print('Unknown mapping case');
                print(studyData['sdd']);
                print(studyData['dd']);
                print(studyData['data']);
                sys.exit(1);

    # print(dataset[projNum]['mapping']);
    print('--------------------');

print("Done Mapping!");
print('\n\n');

print('Starting mapping validation');

# Validate Mappings
for projNum, studyData in dataset.items():
    print('--------------------');
    print(projNum);
    for sddPath, [ddPath, dataPath] in studyData['mapping'].items():
        print(sddPath + ' --> ' + ddPath + ' and ' + dataPath);

        report = pythonIO.validatData(sddPath, ddPath, dataPath);
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

    print('--------------------');


sys.exit(0);




for projNum, studyData in dataset.items():
    toDoList = [];
    # toDoList = ['2016-1407', '2016-1431', '2016-1432', '2016-1438', '2017-2121'];
    if projNum not in toDoList:
        dataset[projNum]['mapping'] = {};

        # Generate a mapping between multiple DDs, SDDs, and Data files
        if (len(dataset[projNum]['data']) == 1) and (len(studyData['dd']) == 1) and (len(studyData['sdd']) == 1):
            dataset[projNum]['mapping'][studyData['dd'][0]] = [dataset[projNum]['data'][0], dataset[projNum]['sdd'][0]];
        else:
            dataCopy = dataset[projNum]['data'].copy();
            sddCopy = dataset[projNum]['sdd'].copy();
            for ddPath in studyData['dd']:
                dataMapping = [];

                # find the corresponding file
                potDataPath = ddPath.split("DDCB");
                if(len(potDataPath) != 2):
                    print("Bad DD filename " + ddPath);
                    sys.exit(0);
                potDataPath = potDataPath[0] + "DATA.csv";
                sddDataPath = "SDD-" + projNum + ".xlsx";
                print("-------------------")
                print(projNum)
                print(sddDataPath)
                print("-------------------")

                # check to make sure it exists
                if(potDataPath in dataCopy):
                    dataMapping.append(potDataPath);
                    dataCopy.remove(potDataPath);
                else:
                    print("Missing data filename " + potDataPath);
                    print("Data file list " + str(dataset[projNum]['data']));
                    sys.exit(0);

                # check to make sure it exists
                if(sddDataPath in sddCopy):
                    dataMapping.append(sddDataPath);
                    sddCopy.remove(sddDataPath);
                else:
                    print("Missing sdd filename " + sddDataPath);
                    print("SDD file list " + str(dataset[projNum]['sdd']));
                    sys.exit(0);

                dataset[projNum]['mapping'][ddPath] = dataMapping;

            if len(dataCopy) != 0:
                print("Left over data file " + str(dataCopy));
                sys.exit(0);

            if len(sddCopy) != 0:
                print("Left over sdd file " + str(sddCopy));
                sys.exit(0);

print(dataset[projNum]['mapping']);
print("done!");
sys.exit(0);
    # Check that Dataset is valid for a DD
    # for ddPath, maplist in dataset[projNum]['mapping'].items():
    #     report = pythonIO.validatData(ddPath, maplist[0]);
    #     sys.exit(0);


        # for ddPath in studyData['dd']:
        #     print('----------------------');
        #     print(projNum + ': ' + ddPath);
        #
        #     # Get Report
        #     report = pythonIO.validatDD(ddPath);
        #     if report.isValid():
        #         # print warning if no errors
        #         if report._warnings.size() > 0:
        #             print('Warnings:');
        #             for i in range(report._warnings.size()):
        #                 print(printCellProv(report._warnings.get(i)));
        #             sys.exit(0); # stop running
        #
        #     else: # Print errors if we find any
        #         print('Errors found:');
        #         for i in range(report._errors.size()):
        #             print(printCellProv(report._errors.get(i)));
        #
        #         sys.exit(0); # stop running
        #
        #
        #     print('----------------------');

print(dataset);
# validate dataset
# validate sdds
