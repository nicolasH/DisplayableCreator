#!/bin/bash

rm index.html
cat header.html>>index.html
multimarkdown index.md >> index.html
cat footer.html>>index.html
