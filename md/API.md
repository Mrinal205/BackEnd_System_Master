
## API Calls



### Users

--- Create User

`POST {{server}}/users`


Request:
```
{
    "name": "Eric Anderson",
    "email": "eric4@moonassist.com",
    "password": "password",
    "affiliateToken": "SomeCrazyTokenValue",
    "newsletter": true
}
```

Response:
```
{
    "userId": "08f49b50-ad1f-45f7-9f54-1a55660e571c"
}
```

Possible Http Responses

206 : Partial Content

400 : Bad Request

409 : Conflict


### Verify Email

`POST {{server}}/users/validate`


Request:
```
{
    "userId": "08f49b50-ad1f-45f7-9f54-1a55660e571c",
    "code": "3245234efga4"
}
```

Response:
```
{
    "userId": "08f49b50-ad1f-45f7-9f54-1a55660e571c",
    "accountId": "TBD",
    "email": "eric4@moonassist.com",
    "password": null,
    "token": "QkSflTgu6hBhiqtNAQ/X4M8UWut68uKnzwR4evufTaJMWC2B+SlgRwkuVliMIvI2aIOP89qdZ+q8a8MUbINXHPJQLfqVccEmSSOoRq0H9A8="
}
```

Possible Http Responses

200 : Success

400 : Bad Request


### Authentication


`POST` `{{server}}/users/authenticate`

Request
```
{
    "email": "eric3@moonassist.com",
    "password": "password",
    "ipAddress": "192.168.0.1",
    "userAgent": "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.84 Safari/537.36"
}
```

Response
```
{
    "userId": "1bc8577d-8314-481a-bcd4-860c58ae07b0",
    "accountId": "df35616c-6343-498d-b335-015db4174a3f",
    "email": "eric3@moonassist.com",
    "ipAddress": "192.168.0.1",
    "password": null,
    "token": "LCMOVZMXEFWSq4uF019dz+CUAhYwKWiLNf+1tdDFRO51wA6Jrf2+ThV2MhZJ6L0/P+ZCHVcN54Z4lYNKUJkhMavbL+0IMYhIr1k6YD0wM66LaCQtqmqTEFUlZdezPFOeK3Nyg+Nq9iKhNlSF6/04wQ==",
    "userAgent": "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.84 Safari/537.36"
}
```

200 : Success

400 : Bad Request



-- Authentication with 2FA

`POST` `{{server}}/users/authenticate`

Response `209`
```
{
    "userId": "6259f826-01e6-442a-bf8e-f6375dc29cb6",
    "accountId": null,
    "email": "eric@moonassist.com",
    "password": null,
    "token": null
}
```

`POST` `{{server}}/2fa/authenticate`

Request
```
{
    "userId": "6259f826-01e6-442a-bf8e-f6375dc29cb6",
    "number": "290973"
}
```

Response
```
{
    "userId": "6259f826-01e6-442a-bf8e-f6375dc29cb6",
    "accountId": "539b1c0a-c170-4317-85fc-66b0f1fbdf5d",
    "email": "eric@moonassist.com",
    "password": null,
    "token": "QkSflTgu6hBhiqtNAQ/X4ExC3t1jSWrCl8topbTWbdL/9AO7Qa35nHP1PSmF1b4tu75Y7orQwTfOfWePYfXZorq1imNPV/FoEdiRI7GbLeY="
}
```

Remove 2FA

`DELETE` `{{server}}/2fa`

Request:
```
{
    "userId": "6259f826-01e6-442a-bf8e-f6375dc29cb6",
    "number": "123456"
}
```

Response:

200 : Success
400 : Bad Request

-- Logout

`DELETE` `{{server}}/users/authenticate`

Response:

200 : Success


-- Change User password

`PATCH` `{{server}}/users/{id}/password`

Request

```
{
    "oldPassword": "{password}",
    "newPassword": "{newPassword}"
}
```

Response:

200 - Success

400 - Bad Request


-- Forgot User password (dispatch email)

`POST` `{{server}}/users/forgotpassword`

Request

```
{
    "email": "{email}"
}
```

200 - Success

400 - Bad Request


-- Forgot User password (confirm code and reset password)

`POST` `{{server}}/users/forgotpassword`

Request

```
{
    "email": "{email}",
    "code": "{code}",
    "password": "{password}"
}
```

200 - Success

400 - Bad Request


### Accounts

-- Get Account information by account id

`GET` `{{server}}/accounts/539b1c0a-c170-4317-85fc-66b0f1fbdf5d`

Response :
```
{
    "id": "539b1c0a-c170-4317-85fc-66b0f1fbdf5d",
    "address": {
        "line1": "3904 Foxwood",
        "line2": "Some place",
        "city": "Bakersfield",
        "province": "California",
        "postal": "93306",
        "country": "USA"
    },
    "contact": "01.801.555.1234",
    "exchanges": [
        {
            "id": "9bfc184a-17a3-497d-97b7-57b6cd08c049",
            "exchangeName": "GDAX",
            "apiKey": "**********7890",
            "secret": "**********cret"
        }
    ],
    "loginEvents": [
        {
            "date": "2018-02-26 06:52:17",
            "ipAddress": "192.168.0.1",
            "userAgent": "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.84 Safari/537.36"
        },
        {
            "date": "2017-11-23 04:50:12",
            "ipAddress": "192.168.0.1",
            "userAgent": null
        },
        {
            "date": "2017-11-23 04:47:49",
            "ipAddress": "192.168.0.1",
            "userAgent": null
        },
        {
            "date": "2017-11-23 09:44:27",
            "ipAddress": "127.0.0.1",
            "userAgent": null
        },
        {
            "date": "2017-11-23 02:03:06",
            "ipAddress": "127.0.0.1",
            "userAgent": null
        },
        {
            "date": "2017-11-23 06:52:31",
            "ipAddress": "127.0.0.1",
            "userAgent": null
        },
        {
            "date": "2017-11-23 06:16:05",
            "ipAddress": "127.0.0.1",
            "userAgent": null
        }
    ],
    "twoFactorEnabled": true
}
```

--- Update Account


`PUT` `{{server}}/accounts/539b1c0a-c170-4317-85fc-66b0f1fbdf5d`


Request
```
{
	"personalDetails": {
		"name": "Eric Powers",
	    "dateOfBirth": "2018-01-23",
	    "phone": "01.661.871.5253"
	},
    "address": {
        "line1": "123 Some Street",
        "line2": "Second Line",
        "city": "Los Angels",
        "province": "California",
        "postal": "90310",
        "country": "USA"
    }
}
```



--- Create 2FA

`PUT` `{{server}}/2fa`


Request
```
{
    "userId": "6259f826-01e6-442a-bf8e-f6375dc29cb6",
    "type": "time_based"
}
```

Response ( QR Code URI )
```
otpauth://totp/MoonAssist%3Aeric%40moonassist.com?secret=COVLF5I42SPX4GHHUPNIMZCQY7RRAHLX&issuer=MoonAssist
```

--- Confirm 2FA

`POST` `{{server}}/2fa/confirm`

Request
```
{
    "userId": "4e7ef808-3942-438d-8a98-de898de44683",
    "number": "937370"
}
```


--- Update Address

`PUT` `https://{{server}}/accounts/539b1c0a-c170-4317-85fc-66b0f1fbdf5d/address`


Request:
 ```
 {
    "line1": "12345 Street Lane",
    "line2": "On Lane 2",
    "city": "Park City",
    "province": "Utah",
    "postal": "84032",
    "country": "USA"
}
```

--- Add an Exchange

`POST` `https://{{server}}/accounts/539b1c0a-c170-4317-85fc-66b0f1fbdf5d/exchanges`


 Request:
 ```
 {
    "id": "{id}"
    "exchangeName": "GDAX",
    "apiKey": "1234567890",
    "secret": "This is my secret"
}
```

--- Remove an Exchange

`DELETE` `https://{{server}}/accounts/539b1c0a-c170-4317-85fc-66b0f1fbdf5d/exchanges/{id}`


--- Add an email_subcription

`POST` `https://{{server}}/accounts/539b1c0a-c170-4317-85fc-66b0f1fbdf5d/emailsubscriptions`

 Request:
 ```
 {
    "type": "LOGIN"
}
```

--- Remove an email_subcription

`DELETE` `https://{{server}}/accounts/{accountId}/emailsubscriptions/{emailSubscriptionId}`


### Balance

`GET` `{{server}}/balances/{exchange}/{symbol},{symbol}

Response:
```
{
    "valuesMap": {
        "BTC": {
            "available": 1.27689024e-8,
            "total": 1.27689024e-8,
            "reserved": 0
        },
        "LTC": {
            "available": 0,
            "total": 0,
            "reserved": 0
        }
    }
}
```

### All Balances

`GET` `{{server}}/balances/{exchange}

Response:
```
{
    "valuesMap": {
        "BTC": {
            "available": 1.27689024e-8,
            "total": 1.27689024e-8,
            "reserved": 0
        },
        "EUR": {
            "available": 26.4106912306569,
            "total": 26.4106912306569,
            "reserved": 0
        },
        "ETC": {
            "available": 0,
            "total": 0,
            "reserved": 0
        },
        "BCH": {
            "available": 0,
            "total": 0,
            "reserved": 0
        },
        "ETH": {
            "available": 0.12563984,
            "total": 0.12563984,
            "reserved": 0
        },
        "LTC": {
            "available": 0,
            "total": 0,
            "reserved": 0
        }
    }
}
```


### Orders

`POST` `{{server}}/orders`

`HEADERS`
`Content-Type: application/json`

Request:
```
{
  "symbolPair": "BTC/USD",
	"offerType": "ASK",
	"orderType": "LIMIT",
	"exchangeName": "BINANCE",
	"price": 13420.43,
	"amount": 4324
}
```

Response:
```
  {
      "id": "b35c8734-a4a6-4f54-bd08-db60225a1aa6",
      "offerType": "ASK",
      "orderType": "LIMIT",
      "exchangeName": "GDAX",
      "price": 13420.43,
      "amount": 4324
  }
```


GET `{{server}}/orders/BINANCE?status=OPEN`

Response:
```
  open: [{
      "id": "b35c8734-a4a6-4f54-bd08-db60225a1aa6",
      "offerType": "ASK",
      "orderType": "LIMIT",
      "exchangeName": "BINANCE",
      "price": 13420.43,
      "amount": 4324
  }],
  closed: []
```

`DELETE` `{{server}}/orders/{id}`

`DELETE` `{{server}}/orders/BINANCE/ETH-BTC/{exchangeOrderId}`


### Favorite markets

`GET` `{{server}}/favorites/markets`

`HEADERS`
`Content-Type: application/json`

Response:
```
 [ 
    {
      "favoriteMarketId": "a35c7563-a4a6-4f54-bd08-db60225a1aa6",
      "exchangeId": "j98c7563-a4a6-4f54-bd08-wq60225a1ht3",
      "symbolPair": "BTC/USD"
    },
    {
      "favoriteMarketId": "a35c7563-a4a6-4f54-bd08-db60225a1aa7",
      "exchangeId": "r98c7563-a4a6-4f54-bd08-wq60225a1ht8",
      "symbolPair": "BTC/USD"
    }
  ]
  
```


`POST` `{{server}}/favorites/markets`

`HEADERS`
`Content-Type: application/json`

Request:
```
{
  "exchangeId": "r98c7563-a4a6-4f54-bd08-wq60225a1ht8",
  "symbolPair": "BTC/USD"
}
```

Response:
```
{
  "favoriteMarketId": "a35c7563-a4a6-4f54-bd08-db60225a1aa7",
  "exchangeId": "r98c7563-a4a6-4f54-bd08-wq60225a1ht8",
  "symbolPair": "BTC/USD"
}
```

`DELETE` `{{server}}/favorites/markets/a35c7563-a4a6-4f54-bd08-db60225a1aa7`


### Fetch Historical NetWorth

'GET' '{{server}}/balances/networth/historical?currency=USD'

```
{
    "usd": [
        {
            "value": 77.75,
            "date": 1536526385576
        },
        {
            "value": 77.75,
            "date": 1536526391691
        },
        {
            "value": 77.76,
            "date": 1536526473451
        },
        {
            "value": 77.76,
            "date": 1536526481783
        },
        {
            "value": 77.76,
            "date": 1536526585373
        },
        {
            "value": 77.76,
            "date": 1536526592805
        }
     ]
}
```