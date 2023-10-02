import os;
import sys;
import pandas;
import pickle
from datetime import datetime, date;

import jnius_config
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

# For each SDD & DD get the latest paths and names
dataset = {}; # { projNumber --> {name, dd, sdd, data, version, mapping}}
dataDirs = ['data/HHEAR-Studies', 'data/nhanes', 'data/The-Cancer-Genomic-Atlas'];
latest_paths = ['2023-06-16_clean', '2023-09-12_clean', '2023-06-29_clean'];

for studyNum in range(len(dataDirs)):

    # Operands
    dataDir = dataDirs[studyNum];
    # Get the base version
    latest_path = latest_paths[studyNum];


    latest = datetime.strptime(latest_path.split("_")[0], '%Y-%m-%d');
    latest = date(latest.year,latest.month, latest.day);
    print('Base Path: ');
    print(os.path.join(dataDir, str(latest_path)));

    # Load files
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
            # print(filepath)
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
print(dataset);

# Map SDDs to DD and data
for projNum, studyData in dataset.items():
    # dataset[projNum]['mapping'] = {};
    print('--------------------');
    print("projNum = " + str(projNum));

    if (len(dataset[projNum]['data']) == 0) or (len(studyData['dd']) == 0) or (len(studyData['sdd']) == 0):
        print("Empty skipping");
        print(studyData['sdd']);
        print(studyData['dd']);
        print(studyData['data']);

    else:
        dataset[projNum]['mapping'] = {};
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

# f = open('dataset.pckl', 'rb')
# dataset = pickle.load(f)
# f.close()

# Load java library
print('\n\n')
PythonIOClass = autoclass('io.PythonIO');
pythonIO = PythonIOClass();

print('Starting mapping validation');

# Validate Mappings
for projNum, studyData in dataset.items():
    print('--------------------');
    print(projNum);
    if 'mapping' in studyData:
        for sddPath, [ddPath, dataPath] in studyData['mapping'].items():
            print(sddPath + ' --> ' + ddPath + ' and ' + dataPath);

            report = pythonIO.validatData(sddPath, ddPath, dataPath);
            if report.isValid():
                # print warning if no errors
                if report._warnings.size() > 0:
                    print('Warnings:');
                    for i in range(report._warnings.size()):
                        print(printCellProv(report._warnings.get(i)));
                    sys.exit(1); # stop running

            else: # Print errors if we find any
                print('Errors found:');
                for i in range(report._errors.size()):
                    print(printCellProv(report._errors.get(i)));

                sys.exit(1); # stop running

    print('--------------------');

# validate DD
# Check that Dataset is valid for a DD
for projNum, studyData in dataset.items():
    # for ddPath in studyData['dd']:
    if 'mapping' in studyData:
        for sddPath, [ddPath, dataPath] in studyData['mapping'].items():
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
                    sys.exit(1); # stop running

            else: # Print errors if we find any
                print('Errors found:');
                for i in range(report._errors.size()):
                    print(printCellProv(report._errors.get(i)));

                sys.exit(1); # stop running


            print('----------------------');


# validate SDD
studyCount = 0
for projNum, studyData in dataset.items():
    studyCount = studyCount + 1

    if 'mapping' in studyData:
        for sddPath, [ddPath, dataPath] in studyData['mapping'].items():
            print('----------------------');
            print('studyCount = ' + str(studyCount))
            print(projNum + ': ' + sddPath);

            # Get Report
            report = pythonIO.validatSDD(sddPath);

            if report._warnings.size() > 0:
                print('Warnings:');
                for i in range(report._warnings.size()):
                    print(printCellProv(report._warnings.get(i)));
                sys.exit(1); # stop running

            # Print errors if we find any
            print('Errors found:');
            for i in range(report._errors.size()):
                print(printCellProv(report._errors.get(i)));


            print('----------------------');

            if report._errors.size() > 0:
                sys.exit(1);

print('All Studies Parsed!');
f = open('dataset.pckl', 'wb')
pickle.dump(dataset, f)
f.close()
