#!/bin/bash
echo "Cleaning, building and signing the project's jar and jnlp"
echo "to regenerate a self-signed keystore :"
echo "keytool -keystore dispKS -genkey -alias DisplayableCreator -keypass '!R4n!D0m!' -validity 1826 -keysize 1024"
mvn clean;mvn package webstart:jnlp-inline
