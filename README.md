
# Project Title
This is a sample project of an Opendaylight Project
describing the most common tasks:
like adding data, adding rpc



## Acknowledgements

 - [MDSAL and Flow run a message when entering ODL Controller](https://wiki-archive.opendaylight.org/view/OpenDaylight_Controller:MD-SAL:MD-SAL_App_Tutorial)
 - [Example for app development in ODL](https://docs.opendaylight.org/en/latest/developer-guides/developing-apps-on-the-opendaylight-controller.html)
 - [Basic communication between ODL Controller components](https://github.com/opendaylight/controller/blob/master/docs/dev-guide.rst)


## Environment Variables

Java 11/maven 3.8.6

## Deployment

To deploy this project run

```bash
mvn clean install
cd karaf/target/assembly/
./bin/karaf
```



## Demo

enter another terminal, enter this query

```bash
curl --location --request POST 'http://localhost:8181/rests/operations/example:hello-world'   --header 'Authorization: Basic YWRtaW46YWRtaW4='   --header 'Content-Type: application/json'   --data-raw '{
  "input": {
    "name": "linhpt21"
  }
}'
```
output 

```bash 
"
{"example:output":{"greeting":"Hello linhpt21"}}
"
```

enter this  query
```bash
curl --location --request POST 'http://localhost:8181/rests/operations/example:add-name'   --header 'Authorization: Basic YWRtaW46YWRtaW4='   --header 'Content-Type: application/json'   --data-raw '{
  "input": {
    "name": "linhpt21",
	"value": "94"
  }
}'
```
output:
```bash
{"example:output":{"result":"Name added successfully"}}
```

enter this query 
```bash 
curl --location --request POST 'http://localhost:8181/rests/operations/example:get-name'   --header 'Authorization: Basic YWRtaW46YWRtaW4='   --header 'Content-Type: application/json'   --data-raw '{
  "input": {
    "name": "linhpt21"
  }
}'
```

output:
```bash
{"example:output":{"value":"94"}}l
```

enter this url in web browser: to get all data 
```bash
http://SERVER_IP:8181/rests/data
```
SERVER_IP: the Ip of server 

enter this url in web browser: to get all rpc 

```bash
http://SERVER_IP:8181/rests/operations
```
