import os;

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


def expandURI(prefix, prefixUri, prefixCount):
    preSplit = prefixUri.split(':');

    if len(preSplit) != 2:
        raise Exception("Bad URI: " + prefixUri);

    if not preSplit[0] in prefix:
        raise Exception("Missing prefix: " + preSplit[0]);

    prefixCount[preSplit[0]] = prefixCount[preSplit[0]] + 1;
    return prefix[preSplit[0]] + preSplit[1];


def getCSVFile(path):
    files = [];
    for file in os.listdir(path):
        filepath = os.path.join(path, file);
        print(filepath);
        if os.path.isfile(filepath):
            if not file.startswith("~$") and file.endswith(".xlsx"):
                files.append(filepath);

    if len(files) == 0:
        raise Exception("Couldn't find any csv files at " + str(path));

    if len(files) > 1:
        raise Exception("Found multiple files " + str(files));

    return files[0];

def getListOfVars(strData):
    vars = strData.split(',');
    return [x.strip() for x in vars];

def printCellProv(cellProv):
    return '(' + cellProv._sheetName + ', ' + str(cellProv._rowIndex) + ', ' + str(cellProv._colIndex) + '): ' + cellProv._annotation
