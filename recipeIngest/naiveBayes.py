import pickle

import nltk
from nltk import word_tokenize
from nltk.corpus import stopwords
from nltk import FreqDist, classify, NaiveBayesClassifier
import string
import csv
import re

import util

nltk.download('stopwords')
nltk.download('punkt')
nltk.download("wordnet")
nltk.download("omw-1.4")
nltk.download('words')


# Function to extract features from text and remove stop words, punctuation, and numbers, and apply stemming
def extract_features(text):
    stop_dict = set(stopwords.words('english') + list(string.punctuation))
    words = set(word.lower() for word in word_tokenize(text) if word.isalpha() and word.lower() not in stop_dict)
    features = {}
    for word in words:
        features[word] = True
    return features


if __name__ == '__main__':
    ingredients_ft, steps_ft, description_ft = util.extract_data()

    # ingredient_split = int(len(ingredients_ft) * 0.8)
    # ingredients_ft_train = ingredients_ft[:ingredient_split]
    # ingredients_ft_test = ingredients_ft[ingredient_split:]
    # ingredient_words_s, ingredient_words_ln, ingredient_words_lv = stem_and_lemmatize(ingredients_ft_train)
    #
    # steps_split = int(len(steps_ft) * 0.8)
    # steps_ft_train = steps_ft[:steps_split]
    # steps_ft_test = steps_ft[steps_split:]
    # step_words_s, step_words_ln, step_words_lv = stem_and_lemmatize(steps_ft_train)

    labeled_data = []
    for text in steps_ft:
        labeled_data.append((extract_features(text), 'step'))
    for text in ingredients_ft:
        labeled_data.append((extract_features(text), 'ingredient'))
    for text in description_ft:
        labeled_data.append((extract_features(text), 'description'))

    train_size = int(len(labeled_data) * 0.8)
    train_set, test_set = labeled_data[:train_size], labeled_data[train_size:]

    # Train the Naive Bayes classifier
    classifier = NaiveBayesClassifier.train(train_set)

    # Test the classifier
    accuracy = classify.accuracy(classifier, test_set)
    print(f"Accuracy: {accuracy:.2%}")

    f = open('nltk_nb.pickle', 'wb')
    pickle.dump(classifier, f)
    f.close()

    # Example predictions
    new_text = "Mix flour and eggs to make a dough."
    predicted_category = classifier.classify(extract_features(new_text))
    print(f"Predicted category for '{new_text}': {predicted_category}")