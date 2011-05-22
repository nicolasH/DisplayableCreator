#!/bin/bash
echo "Cleaning, building and signing the project's jar and jnlp"
mvn clean;mvn package webstart:jnlp-inline
