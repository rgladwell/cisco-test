# Ricardo's Cisco Home Technical Test [![Deploy to Heroku](https://www.herokucdn.com/deploy/button.png)](https://heroku.com/deploy)

To execute run:

```sh
$ sbt run
```

You can then retrieve EC2 instances using the following URI template:

```
http://localhost:8080/regions/{region}/instances
```

This endpoint takes the following parameters

| Name | Description
| ---- | ----
| sort | Attribute to sort by (valid values are `name`, `type`, `state`, `az`, `ip` and `privateIp`)

You will need to enter your AWS Key ID and secret key when prompted as your user name and password.

A live instance is running here:

https://sheltered-crag-98598.herokuapp.com
