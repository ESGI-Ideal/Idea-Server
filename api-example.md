GET http://localhost/[partner, user, ad, article, image]

GET http://localhost/[partner, user, ad, article, image]/{id}

GET http://localhost/image/{id}/file

DELETE http://localhost/[partner, user, ad, article, image]/{id}

POST http://localhost/partner
```
{
	"name": "microsoft",
	"img": 0,
	"description": "todo"
}
```

POST http://localhost/ad
```
{
	"description": "todo",
	"img": 0
}
```

POST http://localhost/user
```
{
	"email": "todo",
	"img": 0,
	"isAdmin": false
}
```

POST http://localhost/article
```
{
	"name": "HDD 10To NVMe2",
	"img": 0,
	"description": "todo",
	"price": 100.50
}
```
