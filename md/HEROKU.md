#Heroku


## Helpful commands

Kill all database connections
```heroku pg:killall DATABASE_URL```


## API Token (For deployment through CI)
heroku auth:token


### Maintenance Mode

heroku login
heroku maintenance:on --app APP_NAME