
### Configuring a Service

1. Add a tcp proxy

```bash
curl -i -X POST \
  --url http://127.0.0.1:8080/tcpConfigs \
  --data 'name=example-ssh-proxy' \
  --data 'localPort=2212' \
  --data 'remoteHost=192.168.1.2' \
  --data 'remotePort=22' \
  --data 'baseUrl=https://example.com/tcpproxy/tcp'
```

2. Update a tcp proxy

```bash
curl -i -X PATCH \
  --url http://127.0.0.1:8080/tcpConfigs/{id} \
  --data 'name=example-ssh-proxy' \
  --data 'localPort=2212' \
  --data 'remoteHost=127.0.0.1' \
  --data 'remotePort=22' \
  --data 'baseUrl=https://example.com/tcpproxy/tcp'
```

3. Query all tcp proxy

```bash
curl -i -X GET \
  --url http://127.0.0.1:8080/tcpConfigs
```

4. Delete a tcp proxy

```bash
curl -i -X DELETE \
  --url http://127.0.0.1:8080/tcpConfigs/{id}
```

5. Query all session

```bash
curl -i -X GET \
  --url http://127.0.0.1:8080/sessions
```
