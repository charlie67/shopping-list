import nltk
import spacy
from nltk.stem import WordNetLemmatizer

import util

def is_verb(token):
    return token in {'VERB', 'AUX'}

def is_noun(token):
    return token.pos_ == 'NOUN'

if __name__ == '__main__':
    nltk.download('averaged_perceptron_tagger')
    nlp = spacy.load("en_core_web_sm")

    ingredients_ft, steps_ft, description_ft = util.extract_data()

    lemmatizer = WordNetLemmatizer()

    verbs = set()

    for text in steps_ft:
        doc = nlp(text)
        word_verbs = [token.lemma_ for token in doc if not token.is_punct and token.text[0] != '&'
                      and token.text[0] != '-' and token.text[0] != '.' and not token.text[0].isdigit()
                      and not token.is_digit and is_verb(token.pos_)]
        verbs.update(word_verbs)

    nouns = set()

    for text in ingredients_ft:
        doc = nlp(text)
        word_verbs = [token.lemma_ for token in doc if not token.is_punct and token.text[0] != '&'
                      and token.text[0] != '-' and token.text[0] != '.'
                      and not token.is_digit and is_noun(token.pos_)]
        nouns.update(word_verbs)

    string = []

