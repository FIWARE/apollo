# Notification-Proxy

In order to allow an  [NGSI-LD](https://docbox.etsi.org/isg/cim/open/Latest%20release%20NGSI-LD%20API%20for%20public%20comment.pdf) [broker](https://github.com/FIWARE/catalogue#core-context-broker-components) to receive 
data sent through subscriptions by another broker, the notification-proxy translates notifications into entity creation or update
requests at the NGSI-LD api. For every entity received in the [data-part of a notification](api/api.yaml#L299), the proxy first tries
a ```POST /entities/{entityId}/attrs/``` to update(and overwrite the properties) the entity. If ```404 - NOT FOUND``` is returned by the broker,
the proxy will try to create the entity via ```POST /entities```.

## Deployment

The notification-proxy is provided as a container: https://quay.io/repository/wi_stefan/notification-proxy
Run it via: ```docker run https://quay.io/repository/wi_stefan/notification-proxy``` It will be available at port ```8080``` per default.
All configurations can be provided with the standard mechanisms of the [Micronaut-Framework](https://micronaut.io/), e.g. [environment variables or appliction.yaml file](https://docs.micronaut.io/3.1.3/guide/index.html#configurationProperties).
The following table concentrates on the most important configuration parameters:


| Property                    | Env-Var                     | Description                                       | Default                                                     |
|-----------------------------|-----------------------------|---------------------------------------------------|-------------------------------------------------------------|
| `micronaut.server.port`     | `MICRONAUT_SERVER_PORT`     | Server port to be used for the notfication proxy. | 8080                                                        |
| `micronaut.metrics.enabled` | `MICRONAUT_METRICS_ENABLED` | Enable the metrics gathering                      | true                                                        |
| `general.tenant`            | `GENERAL_TENANT`            | Tenant to be used when forwarding to orion        | null                                                        |
| `http.services.broker.url`  | `HTTP_SERVICES_BROKER_URL`  | Url of the broker to forward to.                  | http://localhost:1027                                       |

## Tenancy

The proxy does not extract any tenancy information out of the notifications. However the tenant to be used can be configured via ```GENERAL_TENANT=myTenant```. 
Thus, if the setup requires multi-tenancy, runnig multiple instances of the proxy with prefiltering of the requests would be the prefered solution.

The setup can look as following:
![tenancy](doc/tenancy.svg)
An API-Gateway handles all incoming traffic and forwards it based on the tenancy information present in the request(f.e. inside an ```Authorization-Header```). 

