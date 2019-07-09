
# Running Code Locally



##Brew

``https://brew.sh/``



## SDKMAN

``http://sdkman.io/``


## Persistence


Install PostgresSQL

```brew install postgres```


Start PostGres

```
pg_ctl -D /usr/local/var/postgres start
```

Create User and Database

```createuser moonassist```

```createdb moonassist```


##Running

``
export ENVIRONMENT=local export PORT=8080 export DATABASE_URL=postgres://moonassist:moonassist@localhost:5432/moonassist 
``

Creating the tables

``
mci -pl model -P migrate-db
``

```
mvn clean install -DskipTests && mvn -pl server spring-boot:run
```
