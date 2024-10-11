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

func CreateOrder(client *mongo.Client) http.HandlerFunc {
	return func(w http.ResponseWriter, r *http.Request) {
		w.Header().Set("Content-Type", "application/json")

		var order models.Order

		// Decodificar el cuerpo de la petición en el struct Order
		if err := json.NewDecoder(r.Body).Decode(&order); err != nil {
			utils.SendResponse(w, "error", http.StatusBadRequest, "Invalid request payload")
			return
		}

		// Usar el servicio para crear la orden
		repo := repository.NewOrderRepository(client)
		service := services.NewOrderService(repo)

		// Intentar crear la orden
		if err := service.CreateOrder(context.Background(), order); err != nil {
			if err == repository.ErrOrderExists {
				utils.SendResponse(w, "error", http.StatusConflict, "Order already exists")
			} else {
				utils.SendResponse(w, "error", http.StatusInternalServerError, err.Error())
			}
			return
		}

		// Si la orden se creo con exito, retornar un mensaje de éxito
		utils.SendResponse(w, "success", http.StatusCreated, "Order created successfully")
	}
}

func GetOrderById(client *mongo.Client) http.HandlerFunc {
	return func(w http.ResponseWriter, r *http.Request) {
		w.Header().Set("Content-Type", "application/json")

		// Obtener el orderId de la URL
		params := mux.Vars(r)
		orderId := params["id"]

		// Usar el servicio para obtener la orden
		repo := repository.NewOrderRepository(client)
		service := services.NewOrderService(repo)

		order, err := service.GetOrderById(context.Background(), orderId)
		if err != nil {
			if err == mongo.ErrNoDocuments {
				utils.SendResponse(w, "error", http.StatusNotFound, "Order not found")
			} else {
				utils.SendResponse(w, "error", http.StatusInternalServerError, err.Error())
			}
			return
		}

		// Si la orden se encontró, retornarla en formato JSON
		w.WriteHeader(http.StatusOK)
		json.NewEncoder(w).Encode(order)
	}
}
