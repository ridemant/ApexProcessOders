package endpoints

import (
	"context"
	"encoding/json"
	"github.com/gorilla/mux"
	"go.mongodb.org/mongo-driver/mongo"
	"net/http"
	"orders-api-go/internal/models"
	"orders-api-go/internal/repository"
	"orders-api-go/internal/services"
	"orders-api-go/internal/utils"
)

func CreateCustomer(client *mongo.Client) http.HandlerFunc {
	return func(w http.ResponseWriter, r *http.Request) {
		w.Header().Set("Content-Type", "application/json")

		var customer models.Customer

		// Decodificar el cuerpo de la petición en el struct Customer
		if err := json.NewDecoder(r.Body).Decode(&customer); err != nil {
			utils.SendResponse(w, "error", http.StatusBadRequest, "Invalid request payload")
			return
		}

		// Usar el servicio para crear el cliente
		repo := repository.NewCustomerRepository(client)
		service := services.NewCustomerService(repo)

		// Intentar crear el cliente
		if err := service.CreateCustomer(context.Background(), customer); err != nil {
			// Si el cliente ya existe, retornar un error 409 (Conflict)
			if err == repository.ErrCustomerExists {
				utils.SendResponse(w, "error", http.StatusConflict, "Customer already exists")
			} else {
				// Otros errores del servidor
				utils.SendResponse(w, "error", http.StatusInternalServerError, err.Error())
			}
			return
		}

		// Si el cliente se creó con éxito, retornar un mensaje de éxito
		utils.SendResponse(w, "success", http.StatusCreated, "Customer created successfully")
	}
}

func GetCustomerById(client *mongo.Client) http.HandlerFunc {
	return func(w http.ResponseWriter, r *http.Request) {
		w.Header().Set("Content-Type", "application/json")

		// Obtener el customerId de la URL
		params := mux.Vars(r)
		customerId := params["id"]

		// Usar el servicio para obtener el cliente
		repo := repository.NewCustomerRepository(client)
		service := services.NewCustomerService(repo)

		customer, err := service.GetCustomerById(context.Background(), customerId)
		if err != nil {
			if err == mongo.ErrNoDocuments {
				// Retornar un error 404 si el cliente no se encuentra
				utils.SendResponse(w, "error", http.StatusNotFound, "Customer not found")
			} else {
				// Otros errores del servidor
				utils.SendResponse(w, "error", http.StatusInternalServerError, err.Error())
			}
			return
		}

		// Si el cliente se encontró, retornarlo en formato JSON
		w.WriteHeader(http.StatusOK)
		json.NewEncoder(w).Encode(customer)
	}
}
