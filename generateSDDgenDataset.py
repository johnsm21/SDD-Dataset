import pickle;
import os;
import sys;

import jnius_config;
jnius_config.set_classpath('.', '/Users/mjohnson/Projects/SDD-Dataset/SDD-Validation/SDD_Validation/target/SDD_Validation-0.0.1-SNAPSHOT-jar-with-dependencies.jar');
from jnius import autoclass;

datasetFile = 'data/SDD-Dataset/2023-10-30/test.pckl';
outputFile = 'data/SDD-Dataset/2023-10-30/test_sddgen_data.csv';

# Load dataset
f = open(datasetFile, 'rb');
dataset = pickle.load(f);
f.close();


# Load java library
print('\n\n')
DataClass = autoclass('data.Data');
DDClass = autoclass('data.DataDictionary');
SDDClass = autoclass('data.SemanticDataDictionary');
PythonIOClass = autoclass('io.PythonIO');


# Open data file
outputFile = open(outputFile, "w");
outputFile.write('Dataset, Column Name, Data Dictionary Description, Attribute, Attribute Words, Ontologies\n');


pythonIO = PythonIOClass();
for datum in dataset:

    dd = DDClass(datum['ddPath']);
    sdd = SDDClass(datum['sddPath']);

    for var in dd._variables:

        # Find if its an attribute
        for sddVar in sdd._variables:
            # found the match
            if sddVar._name == var._name:
                # found a variable with type assignment
                if len(sddVar._att) > 0:
                    # get the labels
                    labels = pythonIO.getClassLabel(sddVar._att[0]);

                    # Make sure we get a label
                    if len(labels) == 0:
                        outputFile.close();
                        print('No label found for ' + sddVar._att[0]);
                        sys.exit(1);

                    outputFile.write(datum['projNum'] + "," + var._name + ",\"" + var._desc + "\"," + sddVar._att[0] + "," + labels[0] + ",");

                    first = True;
                    for d in datum['ontList']:
                        if first:
                            first = False;
                            outputFile.write(d);
                        else:
                            outputFile.write('\t' + d);

                    outputFile.write('\n');
                    print('----------------------')
                    print(datum['projNum'])
                    print(var._name);
                    print(var._desc);
                    print(sddVar._att[0]);
                    print(labels[0]);
                    print(datum['ontList']);

    # outputFile.close();
    # sys.exit(1);

outputFile.close();
