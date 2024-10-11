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

func CreateProduct(client *mongo.Client) http.HandlerFunc {
	return func(w http.ResponseWriter, r *http.Request) {
		w.Header().Set("Content-Type", "application/json")

		var product models.Product

		// decodificar el cuerpo de la petición en el struct Product
		if err := json.NewDecoder(r.Body).Decode(&product); err != nil {
			utils.SendResponse(w, "error", http.StatusBadRequest, "Invalid request payload")
			return
		}

		// usar el servicio para crear el producto
		repo := repository.NewProductRepository(client)
		service := services.NewProductService(repo)

		// intentarr crear el producto
		if err := service.CreateProduct(context.Background(), product); err != nil {
			// Si el producto ya existe, retornar un error 409 (Conflict)
			if err == repository.ErrProductExists {
				utils.SendResponse(w, "error", http.StatusConflict, "Product already exists")
			} else {
				// Otros errores del servidor
				utils.SendResponse(w, "error", http.StatusInternalServerError, err.Error())
			}
			return
		}

		// si el producto se creo con exito, retornar un mensaje de exito
		utils.SendResponse(w, "success", http.StatusCreated, "Product created successfully")
	}
}

func GetProductById(client *mongo.Client) http.HandlerFunc {
	return func(w http.ResponseWriter, r *http.Request) {
		w.Header().Set("Content-Type", "application/json")

		// se obitene el productId de la URL
		params := mux.Vars(r)
		productId := params["id"]

		// se usa el servicio para obtener el producto
		repo := repository.NewProductRepository(client)
		service := services.NewProductService(repo)

		product, err := service.GetProductById(context.Background(), productId)
		if err != nil {
			if err == mongo.ErrNoDocuments {
				// Retornar un error 404 si el producto no se encuentra
				utils.SendResponse(w, "error", http.StatusNotFound, "Product not found")
			} else {
				// Otros errores del servidor
				utils.SendResponse(w, "error", http.StatusInternalServerError, err.Error())
			}
			return
		}

		// el producto a JSON con las etiquetas JSON correctas
		w.Header().Set("Content-Type", "application/json")
		w.WriteHeader(http.StatusOK)
		if err := json.NewEncoder(w).Encode(product); err != nil {
			utils.SendResponse(w, "error", http.StatusInternalServerError, "Failed to encode response")
		}
	}
}
