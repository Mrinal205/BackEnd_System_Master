
## System code for API requests.



##### API
[Documentation](API.md)



##### Building

```
mvn clean package
```


##### Non Public Repos

https://devcenter.heroku.com/articles/local-maven-dependencies

```
  mvn deploy:deploy-file -Durl=file:///Users/eanderson/Development/moonassist/backend-system/service/repo/ \
  -Dfile=///Users/eanderson//Development/moonassist/XChange/xchange-core/target/xchange-core-4.3.4-MOONASSIST-SNAPSHOT-sources.jar \
  -DartifactId=xchange-core \
  -Dpackaging=jar \
  -DgroupId=org.known.exchange \
  -Dversion=4.3.4-MOONASSIST-SNAPSHOT
```


##### Running


- Install postgres
```
brew install postgres
```

```
createuser --pwprompt moonassist

```

-- Environment Variables
```
export ENVIRONMENT=local
export PORT=8080 
export DATABASE_URL=postgres://moonassist:asdfasdf@localhost:5432/moonassist 

```

##### Deployment



##### Logging

heroku addons:create papertrail

heroku addons:open papertrail
