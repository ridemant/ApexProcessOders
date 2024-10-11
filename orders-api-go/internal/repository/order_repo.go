package repository

import (
	"context"
	"go.mongodb.org/mongo-driver/bson"
	"go.mongodb.org/mongo-driver/mongo"
	"orders-api-go/internal/models"
)

type Order struct {
	OrderID    string    `bson:"orderId"`
	CustomerID string    `bson:"customerId"`
	Products   []Product `bson:"products"`
}

type OrderRepository struct {
	collection *mongo.Collection
}

func NewOrderRepository(client *mongo.Client) *OrderRepository {
	collection := GetMongoCollection(client, "apexglobal", "orders")
	return &OrderRepository{collection: collection}
}

func (r *OrderRepository) OrderExists(ctx context.Context, orderId string) (bool, error) {
	var order models.Order
	err := r.collection.FindOne(ctx, bson.M{"orderId": orderId}).Decode(&order)
	if err != nil {
		if err == mongo.ErrNoDocuments {
			return false, nil // No existe la orden
		}
		return false, err // Otro error
	}
	return true, nil // La orden ya existe
}

// Crear una nueva orden en MongoDB
func (r *OrderRepository) CreateOrder(ctx context.Context, order models.Order) error {
	// Verificar si la orden ya existe
	exists, err := r.OrderExists(ctx, order.OrderID)
	if err != nil {
		return err
	}
	if exists {
		return ErrOrderExists // Devolver el error personalizado si ya existe
	}
	_, err = r.collection.InsertOne(ctx, order)
	return err
}

// Obtener una orden por su ID desde MongoDB
func (r *OrderRepository) GetOrderById(ctx context.Context, orderId string) (models.Order, error) {
	var order models.Order
	err := r.collection.FindOne(ctx, bson.M{"orderId": orderId}).Decode(&order)
	return order, err
}
