import pickle;
import sys;

import jnius_config;
jnius_config.set_classpath('.', '/Users/mjohnson/Projects/SDD-Dataset/SDD-Validation/SDD_Validation/target/SDD_Validation-0.0.1-SNAPSHOT-jar-with-dependencies.jar');
from jnius import autoclass;

# Load classes
DDClass = autoclass('data.DataDictionary');


f = open('train.pckl', 'rb');
train = pickle.load(f);
f.close();



# {'projNum': '2017-2121', 'sddPath': 'data/HHEAR-Studies/2023-06-16_clean/2017-2121/SDD-2017-2121-PD.xlsx',
# 'ddPath': 'data/HHEAR-Studies/2023-06-16_clean/2017-2121/2121_EPI_DDCB.xlsx',
# 'dataPath': 'data/HHEAR-Studies/2023-06-16_clean/2017-2121/2121_EPI_DATA.xlsx', 'attCount': 19,
# 'ontList': ['http://localhost/hhear1_9', 'http://localhost/ncit', 'http://localhost/sio']}

dd = DDClass('data/HHEAR-Studies/2023-06-16_clean/2017-2121/2121_EPI_DDCB.xlsx');
for var in dd._variables:
    print(var._name)
    print(var._desc)


print('CodeBook');
codebook = dd._cb._codebook;
for cbVar in codebook.keySet():
    print('Variable: ' + cbVar);
    codeMap = codebook.get(cbVar);
    for code in codeMap.keySet():
        print(code + ' --->' + codeMap.get(code));



sys.exit(2)
# I have table, dd, and sdd access
for datum in train:
    print('-------------------')
    print(datum)
    dd = DDClass(datum['ddPath']);
    print(dd);

    for var in dd._variables:
        print(var._name)
        print(var._desc)
    for cbKey in dd._cb._codebook.keySet():
        print(cbKey)

    print('-------------------')

    sys.exit(2)
