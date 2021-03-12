#!/usr/bin/env bash

set -e

mkdir -p ./certs/server
mkdir -p ./certs/ca

KEYSTORE_PASSWORD="supersecure"
KEYSTORE_PATH="./certs/server/server-keystore.jks"
CA_CERT="./certs/ca/cacert.pem"
CA_KEY="./certs/ca/cakey.pem"
SIGNING_REQUEST="./certs/server/server.signing-request"
SIGNED_CERT="./certs/server/server.certificate"
KAFKA_HOSTNAME="localhost"

echo "Deleting previous cert/key (if found)"
rm -f "${KEYSTORE_PATH}" "${SIGNING_REQUEST}" "${CA_CERT}" "${CA_KEY}" "${SIGNED_CERT}" "./certs/ca/cacert.srl"

# Create server key store
keytool -keystore "${KEYSTORE_PATH}" -alias localhost -validity 1024 -genkey -keyalg RSA -storetype pkcs12 -noprompt \
-dname "CN=${KAFKA_HOSTNAME}, OU=ID, O=unknown, L=unknown, S=unknown, C=XX" \
-storepass "${KEYSTORE_PASSWORD}"

# Create signing request
keytool -keystore "${KEYSTORE_PATH}" -alias localhost -validity 1024 -certreq -keyalg RSA \
 -storepass "${KEYSTORE_PASSWORD}" -noprompt > "${SIGNING_REQUEST}"

# Create CA
openssl req -x509 -keyout "${CA_KEY}" -newkey rsa:4096 -sha256 -nodes -out "${CA_CERT}" -outform PEM \
-subj "/C=XX/ST=unknown/L=unknown/O=unknown/CN=localhost"

# Signing server request
openssl x509 -req -CA "${CA_CERT}" -CAkey "${CA_KEY}" -CAcreateserial -in "${SIGNING_REQUEST}" \
 -out "${SIGNED_CERT}"

# importing to server store
keytool -keystore "${KEYSTORE_PATH}" -alias CARoot -import -file "${CA_CERT}" -storepass "${KEYSTORE_PASSWORD}" -noprompt
keytool -keystore "${KEYSTORE_PATH}" -alias localhost -import -file "${SIGNED_CERT}" -storepass "${KEYSTORE_PASSWORD}" -noprompt
