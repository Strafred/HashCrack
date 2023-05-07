# 2 lab
## Run:
docker-compose up --build

## Manager API:
### Crack hash:

Endpoint: 
```
POST /manager/api/hash/crack
```

Request body:
```
{
    "hash":"5ca2aa845c8cd5ace6b016841f100d82", 
    "maxLength": 4
}
```

### Get job status:

Endpoint: 
```
GET /manager/api/hash/status
```

Query parameters:
```
requestId (required)
```

Example response:
```
{
    "status": "READY",
    "data": [
        "da"
    ]
}
```