# Spring app 

Simple application demostrating the automatic application instrumentation. 


## PreReqs 
### Create an application insights instance 
  az extension add -n application-insights
  az group create -n appinsightsdemo -l westeurope
  az monitor app-insights component create --app appinsightsdemo --location westeurope --kind web -g    --retention-time=30  
### Get instrumentation Key 
az monitor app-insights component show  --app appinsightsdemo -g  appinsightsdemo  --query instrumentationKey
## Build 
docker build . -t springinsights:latest 
## Run 
### Configuration 
The App insigths demo image expects two environment variables. 
 * APPLICATIONINSIGHTS_CONNECTION_STRING 
 * POSTGRES_HOST
 * POSTGRES_USER
 * POSTGRES_PASS
 * POSTGRES_DB

### Running with docker compose 
if you just want to test this without spinning up things like aks or a managed postgres service then you can simple run the the docker compose file. 
Set the env variable in the docker-compose.yaml APPLICATIONINSIGHTS_CONNECTION_STRING
 * APPLICATIONINSIGHTS_CONNECTION_STRING=InstrumentationKey=xxxxxxxx  
 * docker-compose up
 
To generate traffic simply hit the URL 
* http://localhost:8080/api/v1/users/page?page=1
* http://localhost:8080/
* http://localhost:8080/profile
#### View collected metric 

Navigate to the application insights  in azure and select "service map".  


