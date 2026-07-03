# Events Microservice

Microservicio por capas para reserva de entradas de eventos.

## Datos principales

- Paquete base: `com.pucetec.events`
- Java: 21
- Framework: Spring Boot + Kotlin
- Base de datos: H2 en memoria
- Seguridad: OAuth2 Resource Server con AWS Cognito

## Estructura

```text
src/main/kotlin/com/pucetec/events/
├── config/
├── controllers/
├── dto/
├── entities/
├── exceptions/
├── mappers/
├── repositories/
└── services/
```

## Ejecutar

```bash
./gradlew bootRun
```

En Windows:

```bash
.\gradlew.bat bootRun
```

El servicio queda en:

```text
http://localhost:8080
```

## Tests

```bash
./gradlew test
```

En Windows:

```bash
.\gradlew.bat test
```

La capa `services` tiene tests unitarios para los caminos felices y todas las excepciones de negocio.

## Seguridad

Endpoint publico:

```text
GET /api/events
GET /api/events/{id}
```

Todo lo demas requiere:

```text
Authorization: Bearer <access_token>
```

El `issuer-uri` configurado es:

```text
https://cognito-idp.us-east-1.amazonaws.com/us-east-1_yzwNALI2A
```

## Obtener token Cognito

1. Abrir la Hosted UI:

```text
https://us-east-1yzwnali2a.auth.us-east-1.amazoncognito.com/login?client_id=3gv2oqe4niko3s47srn1kitsk6&response_type=code&scope=email+openid+phone&redirect_uri=https%3A%2F%2Fd84l1y8p4kdic.cloudfront.net
```

2. Copiar el `code` de la URL de redireccion.

3. Canjear el code:

```bash
curl --location 'https://us-east-1yzwnali2a.auth.us-east-1.amazoncognito.com/oauth2/token' \
  --header 'Content-Type: application/x-www-form-urlencoded' \
  --data-urlencode 'grant_type=authorization_code' \
  --data-urlencode 'client_id=3gv2oqe4niko3s47srn1kitsk6' \
  --data-urlencode 'client_secret=14qdd388f1j6fge52el3l5r2ouvcg5sperlno3701t2jj1chgeiu' \
  --data-urlencode 'code=<PEGA_TU_CODE_AQUI>' \
  --data-urlencode 'redirect_uri=https://d84l1y8p4kdic.cloudfront.net'
```

4. Copiar el `access_token` y pegarlo en la variable `access_token` de la coleccion Postman.

## Postman

Importa:

```text
events.postman_collection.json
```

Variables incluidas:

- `base_url`: `http://localhost:8080`
- `access_token`: pegar aqui el token valido de Cognito
- `auth_code`: pegar aqui el code de Cognito si se quiere probar el intercambio desde Postman

## Evidencias

La carpeta `evidencias/` contiene una lista de capturas requeridas para entregar.
