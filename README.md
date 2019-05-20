# K8s AWS poc

### Steps to create PEM files for public and private keys
* >openssl genrsa -out mykey.pem 2048
* >openssl rsa -in mykey.pem -pubout -outform PEM -out public.pem
* >openssl pkcs8 -topk8 -inform PEM -outform PEM -in mykey.pem -out private_key.pem -nocrypt
