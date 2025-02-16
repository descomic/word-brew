import os

import spacy
from flask import Flask, request, render_template_string
from random_word import RandomWords
from spacy import Language
from spacy.tokens import Doc

app: Flask = Flask(__name__)
nlp: Language = spacy.load("en_core_web_md")
random_words: RandomWords = RandomWords()


@app.route("/", methods=["GET"])
def hello() -> str:
    print(f"Received request: {request.url}")
    return render_template_string("Word Engine API")


@app.route("/similarity/<word>/<other_word>", methods=["GET"])
def similarity(word, other_word) -> str:
    print(f"Received request: {request.url}")

    nlp_word: Doc = nlp(word)
    nlp_other_word: Doc = nlp(other_word)
    score: float = nlp_word.similarity(nlp_other_word)

    return render_template_string(str(score))


@app.route("/random", methods=["GET"])
def random() -> str:
    print(f"Received request: {request.url}")
    random_word = random_words.get_random_word()
    nlp_word: Doc = nlp(random_word)
    while not nlp_word or not nlp_word.vector_norm:
        random_word = random_words.get_random_word()
        nlp_word = nlp(random_word)
    return render_template_string(random_word)


if __name__ == "__main__":
    host = os.getenv("FLASK_HOST")
    if host is None:
        raise Exception("FLASK_HOST environment variable is not set")
    port = os.getenv("FLASK_PORT")
    if port is None:
        raise Exception("FLASK_PORT environment variable is not set")
    app.run(host=host, port=5000)
