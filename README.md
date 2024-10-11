# Sistema de Procesamiento de Pedidos

Este proyecto implementa un sistema de procesamiento de pedidos basado en Kafka, Redis y MongoDB. El sistema está compuesto por varios servicios que trabajan en conjunto para procesar pedidos, validar clientes y productos, y manejar reintentos en caso de que alguno de los servicios externos no esté disponible. 

## Arquitectura

El siguiente diagrama muestra la arquitectura del sistema:

![Arquitectura](https://i.ibb.co/7kBsCYN/arq.jpg)

- **Producer**: El servicio Producer en Java envía los pedidos al tópico `orders` de Kafka.
- **Consumer**: El servicio Consumer, también en Java, consume los pedidos desde Kafka y realiza las validaciones.
  - Si una validación falla (por ejemplo, cliente no existe o está inactivo, o producto no existe), se envía el mensaje al tópico `orders-failed`.
  - Si un servicio externo no está disponible, se activa el mecanismo de reintentos usando Redis y se guarda el pedido en el tópico `orders-retry`.
  - Si el pedido es válido, se envía al **API externo** (escrito en Go) para ser guardado en MongoDB.
- **Redis**: Se usa para manejar reintentos (`orders:retry`), guardar el estado de los pedidos (`orders:data`) y realizar locks distribuidos para evitar que múltiples instancias procesen el mismo pedido simultáneamente (`orders:lock`).
- **MongoDB**: Almacena los pedidos procesados correctamente a través del API externo.

## Flujo de Procesamiento

1. **Lock en Redis**: Al consumir un pedido, se intenta obtener un lock en Redis para asegurarse de que no se procese el mismo pedido en múltiples instancias.
2. **Validación de Cliente y Productos**: 
   - Se consulta el API externo para validar si el cliente existe y si su estado es "activo".
   - Se verifica si los productos en el pedido existen en el sistema.
3. **Manejo de Errores y Reintentos**:
   - Si alguno de los servicios externos (como la API de clientes o productos) no está disponible, se activa el mecanismo de reintentos. 
   - El estado de los reintentos se guarda en Redis y, si fallan todos los intentos, se envía un mensaje al tópico `orders-retry` de Kafka.
   - Si uno de los productos o el cliente no es válido, se envía un mensaje al tópico `orders-failed`.
4. **Persistencia en MongoDB**: Si todas las validaciones son exitosas, el pedido se envía al API externo que lo guarda en MongoDB.

## Cálculo de Reintentos Exponenciales
Para realizar el cálculo del reintento exponencial basado en la configuración proporcionada en la anotación @Retryable, tenemos la siguiente lógica:

**maxAttempts** = 5: El número máximo de intentos es 5 (esto incluye el primer intento más 4 reintentos).

**delay** = 2000: El retraso inicial entre el primer y segundo intento es de 2000 milisegundos (2 segundos).

**multiplier** = 2: El tiempo de espera se duplica después de cada reintento.

Si el reintento falla después de un número máximo de intentos, el mensaje se envía al tópico `orders-retry`.

## Documentación del API Externo

### **Customers**
1. **POST /customers**
   - Crea un nuevo cliente en el sistema.
   - **Request Body**:
     ```json
     {
       "customerId": "customer-1",
       "name": "Customer 1",
       "status": "active"
     }
     ```
   - **Response**:
     - **Status 201**: Cliente creado correctamente.
     - **Status 400**: Error en la solicitud.

2. **GET /customers/{customerId}**
   - Obtiene los detalles de un cliente por su ID.
   - **Request Parameters**:
     - `customerId`: ID del cliente.
   - **Response**:
     - **Status 200**: Retorna los detalles del cliente.
     - **Status 404**: Cliente no encontrado.

---

### **Products**
1. **POST /products**
   - Crea un nuevo producto en el sistema.
   - **Request Body**:
     ```json
     {
       "productId": "product-2",
       "name": "Product 2",
       "description": "Product 2",
       "price": 999.99
     }
     ```
   - **Response**:
     - **Status 201**: Producto creado correctamente.
     - **Status 400**: Error en la solicitud.

2. **GET /products/{productId}**
   - Obtiene los detalles de un producto por su ID.
   - **Request Parameters**:
     - `productId`: ID del producto.
   - **Response**:
     - **Status 200**: Retorna los detalles del producto.
     - **Status 404**: Producto no encontrado.

---

### **Orders**
1. **POST /orders**
   - Crea una nueva orden en el sistema.
   - **Request Body**:
     ```json
     {
       "orderId": "order-4e",
       "customerId": "customer-456",
       "products": [
         {
           "productId": "product-6565",
           "name": "Laptop",
           "price": 999.99
         },
         {
           "productId": "product-790",
           "name": "Mouse",
           "price": 49.99
         }
       ]
     }
     ```
   - **Response**:
     - **Status 201**: Orden creada correctamente.
     - **Status 400**: Error en la solicitud.

2. **GET /orders/{orderId}**
   - Obtiene los detalles de una orden por su ID.
   - **Request Parameters**:
     - `orderId`: ID de la orden.
   - **Response**:
     - **Status 200**: Retorna los detalles de la orden.
     - **Status 404**: Orden no encontrada.

---

## Pruebas Unitarias Implementadas

- **Cuando el cliente no existe**: Verifica que se devuelva un error si el cliente no está registrado en el sistema.
- **Cuando el estado del cliente no es activo**: Asegura que la orden sea rechazada si el cliente no está en estado activo.
- **Cuando un producto no existe**: Simula un error cuando uno de los productos en la orden no está registrado.
- **Cuando el servicio externo no está disponible**: Se activa el mecanismo de reintentos basado en Redis. Si los reintentos fallan, se envía un mensaje al tópico `orders-retry`. Si una validación interna falla, se envía un mensaje al tópico `orders-failed`. Finalmente, si todo es correcto, se crea la orden y se guarda en la base de datos MongoDB.


