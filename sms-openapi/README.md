# ReDoc HTML Generation

## Prerequisites
* Node Installed
* NPM Installed

## Commands
To Generate HTML Doc 

`mvn package`

` npx redoc-cli bundle  ./target/classes/openapi/sms-openapi.yaml -t ./custom-template.hbs -o sms-openapi.html
`

