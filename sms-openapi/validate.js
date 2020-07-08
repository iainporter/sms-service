var validator = require('oas-validator');
const fs = require('fs');
const path = require('path');
const yaml = require('yaml');

const inputSpec =  path.join(__dirname, process.env.YAML_PATH);

const input = yaml.parse(fs.readFileSync(inputSpec,'utf8'),{schema:'core'});

const options = { resolve: true, preserveMiro: false, source: inputSpec };

validator.validate(input, options, function(err, options){
    if(err) {
        console.log(err);
        process.exit(1);
    }
    console.log('OpenAPI Spec Validated!');
    process.exit(0);
});
