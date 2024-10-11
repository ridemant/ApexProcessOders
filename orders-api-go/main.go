package main

import (
	"log"
	"net/http"
	"orders-api-go/internal/endpoints"
	"orders-api-go/internal/repository"

	"github.com/gorilla/mux"
)

func main() {
	// Inicializa la conexión a MongoDB
	client := repository.InitializeMongoDB("mongodb://root:X7tM9nL4PqY6ZdB2@62.84.178.185:27017/orders?authSource=admin&ssl=false")

	// Inicia un enrutador
	router := mux.NewRouter()

	// Rutas para Orders
	router.HandleFunc("/orders", endpoints.CreateOrder(client)).Methods("POST")
	router.HandleFunc("/orders/{id}", endpoints.GetOrderById(client)).Methods("GET")

	// Rutas para Products
	router.HandleFunc("/products", endpoints.CreateProduct(client)).Methods("POST")
	router.HandleFunc("/products/{id}", endpoints.GetProductById(client)).Methods("GET")

	// Rutas para Customers
	router.HandleFunc("/customers", endpoints.CreateCustomer(client)).Methods("POST")
	router.HandleFunc("/customers/{id}", endpoints.GetCustomerById(client)).Methods("GET")

	// Iniciar servidor HTTP
	log.Fatal(http.ListenAndServe(":8080", router))
}
