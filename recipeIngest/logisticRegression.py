import csv
import random
import string

import numpy as np
from sklearn.feature_extraction.text import CountVectorizer
from sklearn.model_selection import train_test_split
from sklearn.linear_model import LogisticRegression
from sklearn.pipeline import make_pipeline
from sklearn.metrics import accuracy_score

def format_probabilities(p):
    return ["%.2f" % (prob * 100) for prob in p]

if __name__ == '__main__':
    ingredients_ft = []
    with open('ingredients.csv', 'r') as fd:
        ingredients_reader = csv.reader(fd)

        for row in ingredients_reader:
            ingredients_ft.append(row[0])
    temp = [len(ele) for ele in ingredients_ft]
    res = 0 if len(temp) == 0 else (float(sum(temp)) / len(temp))
    print(f"ingredients_ft length {res}")

    steps_ft = []
    with open('steps.csv', 'r') as fd:
        steps_reader = csv.reader(fd)

        for row in steps_reader:
            steps_ft.append(row[0])
    temp = [len(ele) for ele in steps_ft]
    res = 0 if len(temp) == 0 else (float(sum(temp)) / len(temp))
    print(f"steps_ft length {res}")

    # Combine step and ingredient texts
    all_texts = steps_ft + ingredients_ft
    labels = ['step'] * len(steps_ft) + ['ingredient'] * len(ingredients_ft)

    # Split the data into training and testing sets
    train_texts, test_texts, train_labels, test_labels = train_test_split(all_texts, labels, test_size=0.2,
                                                                          random_state=42)

    # Create a pipeline with CountVectorizer and LogisticRegression
    model = make_pipeline(CountVectorizer(stop_words='english'), LogisticRegression(max_iter=1000))

    # Train the model
    model.fit(train_texts, train_labels)

    # Get unique class labels and their indices
    unique_labels = model.named_steps['logisticregression'].classes_

    # Test the model
    predictions = model.predict(test_texts)
    accuracy = accuracy_score(test_labels, predictions)
    print(f"Accuracy: {accuracy:.2%}")

    # Example predictions with probability estimates
    new_text = ["This recipe is a great way to use up leftover chicken. It is easy to prepare and very tasty as well. I usually serve it with Spanish rice and Mexican-style canned corn.",
                "100 cups tomato soup",
                "mix the soup and the soap into a fine paste"]
    for n in range(len(new_text)):
        probabilities = np.array(model.predict_proba([new_text[n]]))
        formatted_probabilities = format_probabilities(probabilities.flatten())

        print(f"Probability estimates for '{new_text[n]}': {formatted_probabilities}")
        for label, prob in zip(unique_labels, probabilities[0]):
            print(f"Probability for class '{label}': {prob:.2%}")