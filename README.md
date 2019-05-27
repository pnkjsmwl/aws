# K8s AWS poc

### Steps to create PEM files for public and private keys
* >openssl genrsa -out mykey.pem 2048
* >openssl rsa -in mykey.pem -pubout -outform PEM -out public.pem
* >openssl pkcs8 -topk8 -inform PEM -outform PEM -in mykey.pem -out private_key.pem -nocrypt


Prerequesites:
1. start docker in local
2. once docker is up, run docker-compose from authenticator project path :
 i.  docker-compose.mongo.yml,  docker-compose up -f docker-compose.mongo.yml -d
 ii. docker-compose.redis.yml,  docker-compose up -f docker-compose.redis.yml -d

Steps to add users :

POST at http://localhost:9091/user/add
eg 1 
{
  "userName" : "Bhavani",
  "password" : "test123",
  "accountNumber" : "1234",
  "role" : "ACCOUNTS_ROLE"
}
eg 2
{
  "userName" : "Prasad",
  "password" : "test456",
  "accountNumber" : "5678",
  "role" : "PROFILE_ROLE"
}
