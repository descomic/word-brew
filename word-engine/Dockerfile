# syntax=docker/dockerfile:1
FROM python:3.12 as base

WORKDIR /app

RUN apt-get update && apt-get install -y build-essential && rm -rf /var/lib/apt/lists/*

RUN pip install -U pip setuptools wheel

FROM base as requirements

COPY requirements.txt requirements.txt
RUN pip install --no-cache-dir -r requirements.txt

FROM requirements as spacy

RUN python -m spacy download en_core_web_md

FROM spacy as runtime

COPY app.py app.py
