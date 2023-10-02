import os
from html.parser import HTMLParser
from bs4 import BeautifulSoup
import xlsxwriter
import sys;

directory = 'data/nhanes-dd/2017-2018/html'
pathOut = 'data/nhanes-dd/2017-2018/parsed'

def makeFolders(path, pathOut):
    base = pathOut + os.path.sep

    for folder in path.split(os.path.sep):
        base = base + os.path.sep + folder

        if not os.path.exists(base):
            os.makedirs(base)

def cleanString(inputStr):
    a_list = inputStr.split()
    return " ".join(a_list)


def parseNhnaes(path):
    file = open(path)
    html_doc = file.read()
    file.close()


    soup = BeautifulSoup(html_doc, 'html.parser')

    codeRaw = soup.find_all("div", class_="pagebreak")
    print(len(codeRaw))

    dd = []
    for dataum in codeRaw:
        dds = dataum.find_all('dd')
        dts = dataum.find_all('dt')

        entry = {}
        for i in range(len(dts)):
            entry[dts[i].getText().replace(':', '').strip()] = cleanString(dds[i].getText())

        # look for code books
        codesString = ''
        table = dataum.find_all("table", class_="values")

        if(len(table) > 0):
            table = table[0].find_all("tbody")[0]
            for row in table.find_all('tr'):
                element = row.find_all('td')
                codesString = codesString + element[0].getText() + '=' + element[1].getText() + '; '

        entry['CODES'] = codesString
        dd.append(entry)

    return dd

def writeInstruct(workbook, bold):
    worksheet = workbook.add_worksheet('Instructions ');
    worksheet.write(0, 0, 'Instructions', bold);
    worksheet.write(1, 0, 'We request  all three tabs "Data Dictionary", "CodeBook" and "Relationships"  be filled out to the best of your ability. These tabs should corrrespond with the CHEAR project data submission.');
    worksheet.write(2, 0, 'Data Dictionary: Please fill out the tab "Data Dictionary" using the tab "Overall-Description"for further clarification of the columns.');
    worksheet.write(3, 0, 'Code Book: Please fill out the tab "Codebook" using the tab "Overall-Description"for further clarification of the columns.');
    worksheet.write(4, 0, 'Relationships: If relationships (i.e. mother- daughter) exist in your data set, please provide a brief description on how they are identified. ');
    worksheet.set_column('A:A', 131.67);

def writeDescrip(workbook, bold):
    worksheet = workbook.add_worksheet('Overall-Description');
    worksheet.set_column('A:A', 17.67);
    worksheet.set_column('B:B', 96);

    worksheet.write(0, 0, 'DATA DICTIONARY', bold);

    worksheet.write(1, 0, 'Column Header', bold);
    worksheet.write(1, 1, 'Description', bold);

    worksheet.write(2, 0, 'varname');
    worksheet.write(2, 1, 'variable name');

    worksheet.write(3, 0, 'primary key');
    worksheet.write(3, 1, 'indicates the identifying linking variable (Yes/No)');

    worksheet.write(4, 0, 'vardesc');
    worksheet.write(4, 1, 'text of the question from the study questionnaire');

    worksheet.write(5, 0, 'question');
    worksheet.write(5, 1, 'text of the question from the study questionnaire');

    worksheet.write(6, 0, 'variable location');
    worksheet.write(6, 1, 'location of question where variable comes from or question # if applicable');

    worksheet.write(7, 0, 'type');
    worksheet.write(7, 1, 'integer (1,2,3,4…) enumerated integer ( integers are coded for non-integer meaning see codebook, ( decimal ( 0.5, 2.5,…) string ( reporter , doctor, lawyer)');

    worksheet.write(8, 0, 'format');
    worksheet.write(8, 1, 'dates (hh:mm); dates (mm/dd/yyyy)');

    worksheet.write(9, 0, 'units');
    worksheet.write(9, 1, 'unit of variable if applicable, otherwise leave blank');

    worksheet.write(10, 0, 'min');
    worksheet.write(10, 1, 'logical minimum value of variable if applicable');

    worksheet.write(11, 0, 'max');
    worksheet.write(11, 1, 'logical maximum value of variable if applicable');

    worksheet.write(12, 0, 'missing values');
    worksheet.write(12, 1, 'defines how a missing value was coded ( . )');

    worksheet.write(13, 0, 'derived variable');
    worksheet.write(13, 1, 'a new variable created from an algorithm using a combination of existing variables ');

    worksheet.write(14, 0, 'resolution');
    worksheet.write(14, 1, '(optional) measurement resolution, the number of decimal places to which a measured value is presented in the data ');

    worksheet.write(15, 0, 'comment');
    worksheet.write(15, 1, '(optional) additional information not included in the vardesc that will further deliniate the variable.');

    worksheet.write(17, 0, 'CODE BOOK', bold);

    worksheet.write(18, 0, 'Column Header', bold);
    worksheet.write(18, 1, 'Description', bold);

    worksheet.write(19, 0, 'varname');
    worksheet.write(19, 1, 'variable name');

    worksheet.write(20, 0, 'vardesc');
    worksheet.write(20, 1, 'text of the question from the study questionnaire');

    worksheet.write(21, 0, 'codes');
    worksheet.write(21, 1, 'response codes for categorical variables');

    worksheet.write(22, 0, 'definition');
    worksheet.write(22, 1, 'meaning of the response code');

    worksheet.write(23, 0, 'comment');
    worksheet.write(23, 1, 'additional information on meaning of response code');

def writeDD(workbook, bold, dd):
    worksheet = workbook.add_worksheet('DATA DICTIONARY');
    worksheet.set_column('A:A', 21.67);
    worksheet.set_column('B:B', 21.67);
    worksheet.set_column('C:C', 21.67);
    worksheet.set_column('D:D', 21.67);
    worksheet.set_column('E:E', 21.67);
    worksheet.set_column('F:F', 21.67);
    worksheet.set_column('G:G', 21.67);
    worksheet.set_column('H:H', 21.67);
    worksheet.set_column('I:I', 21.67);
    worksheet.set_column('J:J', 21.67);
    worksheet.set_column('K:K', 21.67);
    worksheet.set_column('L:L', 21.67);
    worksheet.set_column('M:M', 21.67);

    worksheet.write(0, 0, 'VARNAME', bold);
    worksheet.write(0, 1, 'PRIMARY KEY', bold);
    worksheet.write(0, 2, 'VARDESC', bold);
    worksheet.write(0, 3, 'QUESTION', bold);
    worksheet.write(0, 4, 'VARIABLE LOCATION', bold);
    worksheet.write(0, 5, 'TYPE', bold);
    worksheet.write(0, 6, 'Format', bold);
    worksheet.write(0, 7, 'UNITS', bold);
    worksheet.write(0, 8, 'MIN', bold);
    worksheet.write(0, 9, 'MAX', bold);
    worksheet.write(0, 10, 'missing values', bold);
    worksheet.write(0, 11, 'RESOLUTION', bold);
    worksheet.write(0, 12, 'Comment', bold);

    row = 1
    for d in dd:
        worksheet.write(row, 0, d['Variable Name']);
        if 'English Text' in d:
            worksheet.write(row, 2, d['English Text']);
        else:
            if 'English Instructions' in d:
                worksheet.write(row, 2, d['English Instructions']);
            else:
                print("Missing dd description");
                print(d);
                sys.exit(1);
        if (len(d['CODES']) > 0) and 'Range of Values' not in d['CODES']:
            worksheet.write(row, 5, 'categorical');
        row = row + 1

def writeCode(workbook, bold, dd):
    worksheet = workbook.add_worksheet('Codebook');
    worksheet.set_column('A:A', 28.5);
    worksheet.set_column('B:B', 28.5);
    worksheet.set_column('C:C', 28.5);
    worksheet.set_column('D:D', 28.5);
    worksheet.set_column('E:E', 28.5);
    worksheet.set_column('F:F', 28.5);
    worksheet.set_column('G:G', 28.5);
    worksheet.set_column('H:H', 28.5);
    worksheet.set_column('I:I', 28.5);
    worksheet.set_row(0, 30);

    worksheet.write(0, 0, 'VARNAME', bold);
    worksheet.write(0, 1, 'VARDESC', bold);
    worksheet.write(0, 2, 'CODES', bold);
    worksheet.write(0, 3, 'DEFINITION', bold);
    worksheet.write(0, 4, 'SCALE: 1=continuous, 2=categorical 3 =ordinal', bold);
    worksheet.write(0, 5, 'COMMENT', bold);
    worksheet.write(0, 6, 'Derived variable? 1=yes, 2=no', bold);
    worksheet.write(0, 7, 'If derived, list source variables', bold);
    worksheet.write(0, 8, 'If derived, provide algorithm for creating variable', bold);

    row = 1
    for d in dd:
        if (len(d['CODES']) > 0) and 'Range of Values' not in d['CODES']:
            codes = d['CODES'].split(';');
            for code in codes:
                if code != ' ' and code != '':
                    mapping = code.split('=');
                    if len(mapping) != 2:
                        print("Bad Code:");
                        print(d);
                        print(codes);
                        sys.exit(1);

                    worksheet.write(row, 0, d['Variable Name']);
                    worksheet.write(row, 2, mapping[0].strip());
                    worksheet.write(row, 3, mapping[1].strip());
                    worksheet.write(row, 4, '2');
                    row = row + 1;

def writeRelation(workbook, bold):
    worksheet = workbook.add_worksheet('Relationship');
    worksheet.set_column('A:A', 131.50);
    worksheet.set_row(0, 238);
    worksheet.write(0, 0, 'Please Provide a brief description of the  variables used to link the relationship.', bold);

def generateDD(path, ddfile, dd):
    workbook = xlsxwriter.Workbook(ddfile, {'strings_to_numbers': True});
    bold = workbook.add_format({'bold': True});
    writeInstruct(workbook, bold);
    writeDescrip(workbook, bold);
    writeDD(workbook, bold, dd);
    writeCode(workbook, bold, dd);
    writeRelation(workbook, bold);
    workbook.close();


def generateExcel(path, ddfile, dd):
    workbook = xlsxwriter.Workbook(ddfile)
    worksheet = workbook.add_worksheet()
    header = ['Variable Name', 'SAS Label', 'English Text', 'English Instructions', 'Target', 'CODES']

    for i in range(len(header)):
        worksheet.write(0, i, header[i])

    row = 1
    for d in dd:
        for col in range(len(header)):
            if header[col] in d:
                worksheet.write(row, col, d[header[col]])
        row = row + 1

    workbook.close()



for subdir, dirs, files in os.walk(directory):
    for file in files:
        if file.endswith(".html"):
            print('subdir = ' + subdir);
            print('pathOut = ' + pathOut);
            newOut = subdir.replace(directory, '');
            print('subdir.replace(directory, '') = ' + newOut );
            makeFolders(newOut, pathOut)
            ddout = parseNhnaes(os.path.join(subdir, file))
            ddfile = os.path.join(pathOut + os.path.sep + newOut + os.path.sep + 'DD-' + file.replace('.html', '.xlsx'))
            generateDD(os.path.join(subdir, file), ddfile, ddout);
            # sys.exit(1);
            # generateExcel(os.path.join(subdir, file), ddfile, ddout)
