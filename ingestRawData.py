import os;
import pandas;

# Operands
dataDir = 'data';



def cleanString(s):
    return s.lower().strip();

def expandCategory(s):
    cat = s.strip();

    # Start the exapnding
    expandableChars = ['/', '<', '>']; # might need to add '-', maybe we do a glove check?
    for exp in expandableChars:
        if exp in cat:
            fatCat = '';
            isFirst = True;
            for catPart in cat.split(exp):
                if isFirst:
                    fatCat = catPart;
                    isFirst = False;

                else:
                    fatCat = fatCat + ' ' + exp + ' ' + catPart;

            cat = fatCat.strip();
    return cat;

def extractCategories(s):
    # We need to determine what the seperator were using it shoud be ; or ,
    # The number of the seperator should be the number of categories - 1

    # Determine the number of categories
    sub  = s.split('=');
    numCats = len(sub) - 1;

    # Determine the number of comma's
    seperator = '';
    subComma  = len(s.split(',')); # this is number of comma's
    if numCats == subComma:
        seperator = ',';

    # Determine the number of semicolons's
    subSemicolon  = len(s.split(';'));
    if numCats == subSemicolon:
        seperator = ';';

    if seperator == '':
        raise Exception("Couldn't find a seperator for the unit: " + s);

    # Perfom split
    res = s.split(seperator); # ['1=illiterate', ' 2=able to write', ...]

    # Get just the named entities
    cats = [];
    for cat in res:
        cats.append(expandCategory(cat.split('=')[1]));

    return cats;


def expandURI(prefix, prefixUri):
    preSplit = prefixUri.split(':');

    if len(preSplit) != 2:
        raise Exception("Bad URI: " + prefixUri);

    if not preSplit[0] in prefix:
        raise Exception("Missing prefix: " + preSplit[0]);

    return prefix[preSplit[0]] + preSplit[1];


# For each SDD & DD get the latest paths and names
dataset = []; # [{name, dd, sdd, version}]
for study in os.listdir(dataDir): # Get the study
    dataum = {};
    dataum['name'] = study;
    if study != '.DS_Store':
        # Get the latest version
        latest = -1
        for version in os.listdir(os.path.join(dataDir, study)): # Get the version
            if version != '.DS_Store':
                # Extract version expecting folder such as version7
                localVersion = int(version.split('version')[1]);
                if localVersion > latest:
                    latest = localVersion;

        # Stop if we don't have a legal version
        if latest == -1:
            raise Exception("Missing version folder for " + study);

        dataum['version'] = latest;

        # Get the DD path
        dataum['dd'] = os.path.join(dataDir, dataum['name'], 'version' + str(dataum['version']), 'DD', 'DD.xlsx');
        if not os.path.isfile(dataum['dd']):
            raise Exception("Missing path for " + dataum['dd']);

        # get the SDD path
        dataum['sdd'] = os.path.join(dataDir, dataum['name'], 'version' + str(dataum['version']), 'SDD', 'SDD.xlsx');
        if not os.path.isfile(dataum['sdd']):
            raise Exception("Missing path for " + dataum['sdd']);

        dataset.append(dataum);

# Print what we found
print('We found the following datasets:');
print(dataset);



# Load DD
# dataset update # [{name, dd, sdd, version,
#       variable [{name, description, type, unit []}] }]
dd_sheet = 'DATA DICTIONARY';
varName = ['varname'];
desName = ['vardesc'];
typName = ['type'];
uniName = ['units'];
for dataum in dataset:
    print('Parsing ' + dataum['name'] + ' data dictionary');
    ws = pandas.read_excel(dataum['dd'], dd_sheet);

    # find a good match for each column from our know good list
    varColIndex = "";
    desColIndex = "";
    typColIndex = "";
    unitColIndex = "";
    for header in ws.columns:
        cleanHeader = cleanString(header);
        if cleanHeader in varName:
            varColIndex = header;
        else:
            if cleanHeader in desName:
                desColIndex = header;
            else:
                if cleanHeader in typName:
                    typColIndex = header;
                else:
                    if cleanHeader in uniName:
                        unitColIndex = header;

    # Make sure we found a valid match
    if (varColIndex == "") or (desColIndex == "") or (typColIndex == "") or (unitColIndex == ""):
        print(header);
        print('varColIndex = ' + varColIndex + ', desColIndex = ' + desColIndex + ', typColIndex = ' + typColIndex + ', unitColIndex = ' + unitColIndex);
        raise Exception('Missing column mapping');


    df = ws[[varColIndex, desColIndex, typColIndex, unitColIndex]]

    dataum['variable'] = [];
    for i in range(df.shape[0]): #iterate over rows
        ddentry = {};
        ddentry['name'] = str(df.iat[i, 0]).strip();
        ddentry['description'] = str(df.iat[i, 1]).strip();
        ddentry['type'] = cleanString(str(df.iat[i, 2]));

        unit = str(df.iat[i, 3]).strip();
        if unit == 'nan':
            unit = '';

        if ddentry['type'] == 'categorical':
            ddentry['unit'] = extractCategories(unit);

        else:
            ddentry['unit'] = [unit];

        dataum['variable'].append(ddentry);

print('Completed the DD parsing...');
print(dataset);


# Load SDD
# dataset update # [{name, dd, sdd, version,
#       variable [{name, description, type, unit [],
#           attribute, attributeOf, unitClass, time, inRelationTo,
#           wasDerivedFrom, wasGeneratedBy}],
#       virtual[{name, entity, role, relation, inRelationTo}]
#}]
# prefix['sio'] = URI

prefix = {};

prefix_sheet = 'Prefixes';
preName = ['prefix'];
uriName = ['uri'];

sdd_sheet = 'Dictionary Mapping';
colName = ['column'];
attName = ['attribute'];
attOfName = ['attributeof'];
uniName = ['unit'];
timName = ['time'];
eniName = ['entity'];
rolName = ['role'];
relName = ['relation'];
inRelName = ['inrelationto'];
derName = ['wasderivedfrom'];
genName = ['wasgeneratedby'];
for dataum in dataset:
    print('Parsing ' + dataum['name'] + ' semantic data dictionary');

    # Get the prefixes
    ws = pandas.read_excel(dataum['sdd'], prefix_sheet);

    # Get the SDD
    # find a good match for each column from our know good list
    preColIndex = "";
    uriColIndex = "";
    for header in ws.columns:
        cleanHeader = cleanString(header);
        if cleanHeader in preName:
            preColIndex = header;
        else:
            if cleanHeader in uriName:
                uriColIndex = header;

    # Make sure we found a valid match
    if (preColIndex == "") or (uriColIndex == ""):
        print(header);
        print('preColIndex = ' + preColIndex + ', uriColIndex = ' + uriColIndex);
        raise Exception('Missing column mapping in sdd prefix');

    df = ws[[preColIndex, uriColIndex]]
    for i in range(df.shape[0]): #iterate over rows
        pre = str(df.iat[i, 0]).strip();
        uri = str(df.iat[i, 1]).strip();

        if pre in prefix:
            if not prefix[pre] == uri:
                print('Master prefix ' + pre + ' -> ' + prefix[pre]);
                print('New prefix ' + pre + ' -> ' + uri);
                raise Exception('Overloaded prefix map!');
        else:
            prefix[pre] = uri;
    print('Finished Updating prefix map...');
    print(prefix);

    # Get the prefixes
    ws = pandas.read_excel(dataum['sdd'], sdd_sheet);

    colColIndex = "";
    attColIndex = "";
    attOfColIndex = "";
    uniColIndex = "";
    timColIndex = "";
    eniColIndex = "";
    rolColIndex = "";
    relColIndex = "";
    inRelColIndex = "";
    derColIndex = "";
    genColIndex = "";
    for header in ws.columns:
        cleanHeader = cleanString(header);
        if cleanHeader in colName:
            colColIndex = header;
        else:
            if cleanHeader in attName:
                attColIndex = header;
            else:
                if cleanHeader in attOfName:
                    attOfColIndex = header;
                else:
                    if cleanHeader in uniName:
                        uniColIndex = header;
                    else:
                        if cleanHeader in timName:
                            timColIndex = header;
                        else:
                            if cleanHeader in eniName:
                                eniColIndex = header;
                            else:
                                if cleanHeader in rolName:
                                    rolColIndex = header;
                                else:
                                    if cleanHeader in relName:
                                        relColIndex = header;
                                    else:
                                        if cleanHeader in inRelName:
                                            inRelColIndex = header;
                                        else:
                                            if cleanHeader in derName:
                                                derColIndex = header;
                                            else:
                                                if cleanHeader in genName:
                                                    genColIndex = header;

    # Make sure we found a valid match
    if (colColIndex == "") or (attColIndex == "") or (attOfColIndex == "") or (uniColIndex == "") or (timColIndex == "") or (eniColIndex == "") or (rolColIndex == "") or (relColIndex == "") or (inRelColIndex == "") or (derColIndex == "") or (genColIndex == ""):
        print(header);
        print('colColIndex = ' + colColIndex + ', attColIndex = ' + attColIndex +
            ', attOfColIndex = ' + attOfColIndex + ', uniColIndex = ' + uniColIndex +
            ', timColIndex = ' + timColIndex + ', eniColIndex = ' + eniColIndex +
            ', rolColIndex = ' + rolColIndex + ', relColIndex = ' + relColIndex +
            ', inRelColIndex = ' + inRelColIndex + ', derColIndex = ' + derColIndex +
            ', genColIndex = ' + genColIndex );
        raise Exception('Missing SDD column mapping');

    # Grab the columns we need
    df = ws[[colColIndex, attColIndex, attOfColIndex, uniColIndex, timColIndex,
        eniColIndex, rolColIndex, relColIndex, inRelColIndex, derColIndex,
            genColIndex]]
    print(df);


    for i in range(df.shape[0]): # iterate over rows
        name = str(df.iat[i, 0]).strip();

        if name.startswith('??'): # virtual column
            print('skip');
        else: # Regular column
            # Look for the correct variable
            workingVar = None;
            for var in dataum['variable']:
                if var['name'] == name:
                    workingVar = var;
                    break; # we can leaave early if we find it

            if workingVar == None:
                raise Exception('Column name missmatch, we couldn\'t find ' + name + ' in the DD');

            attribute = str(df.iat[i, 1]).strip();
            if attribute == 'nan':
                workingVar['attribute'] = '';
            else:
                workingVar['attribute'] = expandURI(prefix, attribute);

            attributeOf = str(df.iat[i, 2]).strip();
            if attributeOf == 'nan':
                raise Exception('SDD missing attributeOf definition column name: ' + name);
            else:
                if attributeOf.startswith('??'):
                    workingVar['attributeOf'] = attributeOf;

                else:
                    raise Exception('SDD has bad attributeOf, column name: ' + name + ', attributeOf: ' + attributeOf);

            unitClass = str(df.iat[i, 3]).strip();
            if unitClass == 'nan':
                workingVar['unitClass'] = '';
            else:
                if len(unitClass.split(':')) == 2:  # this is uri
                    workingVar['unitClass'] = expandURI(prefix, unitClass);

                else:   # this is a text unit
                    workingVar['unitClass'] = unitClass;

            time = str(df.iat[i, 4]).strip();
            if time == 'nan':
                workingVar['time'] = '';
            else:
                workingVar['time'] = time;

            inRelationTo = str(df.iat[i, 8]).strip();
            if inRelationTo == 'nan':
                workingVar['inRelationTo'] = '';
            else:
                workingVar['inRelationTo'] = inRelationTo;

            wasDerivedFrom = str(df.iat[i, 9]).strip();
            if wasDerivedFrom == 'nan':
                workingVar['wasDerivedFrom'] = '';
            else:
                workingVar['wasDerivedFrom'] = wasDerivedFrom;

            wasGeneratedBy = str(df.iat[i, 10]).strip();
            if wasGeneratedBy == 'nan':
                workingVar['wasGeneratedBy'] = '';
            else:
                workingVar['wasGeneratedBy'] = expandURI(prefix, wasGeneratedBy);


print('Completed the SDD parsing...');
print(dataset);




# Generate Ontology class


# Dataset check
# ensure that all DD have SDD values
# ensure that all virtual columns within data are defined

# Todo
# Spell check
