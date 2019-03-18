#!/usr/bin/env bash

openssl req -x509 -newkey rsa:2048 -utf8 -days 3650 -nodes -config client-cert.conf -keyout client-cert.key -out client-cert.crt
openssl pkcs12 -export -inkey client-cert.key -in client-cert.crt -out client-cert.p12 -password pass:mytruststorepassword
keytool -importkeystore -deststorepass mytruststorepassword -destkeypass mytruststorepassword -srckeystore client-cert.p12 -srcstorepass mytruststorepassword -deststoretype pkcs12 -destkeystore client-cert.pkcs12