import csv
import re

MODEL_PATH = 'logreg_tfidf.joblib'

NOISE_CHARS = re.compile(r'[\"“”‘’`*\\]')

def clean(text):
    text = NOISE_CHARS.sub('', text)
    text = re.sub(r'\s+', ' ', text)
    return text.strip()

def clean_batch(texts):
    return [clean(t) for t in texts]

def load_column(path):
    rows = []
    with open(path, 'r') as fd:
        for row in csv.reader(fd):
            if not row:
                continue
            cleaned = clean(row[0])
            if cleaned:
                rows.append(cleaned)
    return rows

def format_probabilities(p):
    return ["%.2f" % (prob * 100) for prob in p]
