import os;
import sys;
import pandas;
import pickle
from datetime import datetime, date;

import jnius_config
jnius_config.set_classpath('.', '/Users/mjohnson/Projects/SDD-Dataset/SDD-Validation/SDD_Validation/target/SDD_Validation-0.0.1-SNAPSHOT-jar-with-dependencies.jar')
from jnius import autoclass;

from utility import *;

PythonIOClass = autoclass('io.PythonIO');
pythonIO = PythonIOClass();

sddPath = '/Users/mjohnson/Projects/SDD-Dataset/data/test/SDD-2016-34-v9.xlsx'

# validate SDD
report = pythonIO.validatSDD(sddPath);
print('--------------------------');
print('Report')
if report._warnings.size() > 0:
    print('Warnings:');
    for i in range(report._warnings.size()):
        print(printCellProv(report._warnings.get(i)));


# Print errors if we find any
if report._errors.size() > 0:
    print('Errors:');
    for i in range(report._errors.size()):
        print(printCellProv(report._errors.get(i)));
print('--------------------------');
