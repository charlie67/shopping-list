import csv


def extract_data():
    ingredients_ft = []
    with open('logisticRegression/ingredients.csv', 'r') as fd:
        ingredients_reader = csv.reader(fd)

        for row in ingredients_reader:
            ingredients_ft.append(row[0])
    temp = [len(ele) for ele in ingredients_ft]
    res = 0 if len(temp) == 0 else (float(sum(temp)) / len(temp))
    print(f"ingredients_ft length {res}")

    steps_ft = []
    with open('logisticRegression/steps.csv', 'r') as fd:
        steps_reader = csv.reader(fd)

        for row in steps_reader:
            steps_ft.append(row[0])
    temp = [len(ele) for ele in steps_ft]
    res = 0 if len(temp) == 0 else (float(sum(temp)) / len(temp))
    print(f"steps_ft length {res}")

    description_ft = []
    with open('logisticRegression/description.csv', 'r') as fd:
        des_reader = csv.reader(fd)

        for row in des_reader:
            if not row[0] or row[0].isspace():
                description_ft.append(row[0])
    temp = [len(ele) for ele in steps_ft]
    res = 0 if len(temp) == 0 else (float(sum(temp)) / len(temp))
    print(f"description_ft length {res}")

    return ingredients_ft, steps_ft, description_ft
