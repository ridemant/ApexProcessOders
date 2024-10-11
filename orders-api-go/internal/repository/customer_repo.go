package repository

import (
	"context"
	"go.mongodb.org/mongo-driver/bson"
	"go.mongodb.org/mongo-driver/mongo"
	"orders-api-go/internal/models"
)

type Customer struct {
	CustomerID string `bson:"customerId"`
	Name       string `bson:"name"`
	Status     string `bson:"status"`
}

type CustomerRepository struct {
	collection *mongo.Collection
}

func NewCustomerRepository(client *mongo.Client) *CustomerRepository {
	collection := GetMongoCollection(client, "apexglobal", "customers")
	return &CustomerRepository{collection: collection}
}

func (r *CustomerRepository) CustomerExists(ctx context.Context, customerId string) (bool, error) {
	var customer models.Customer
	err := r.collection.FindOne(ctx, bson.M{"customerId": customerId}).Decode(&customer)
	if err != nil {
		if err == mongo.ErrNoDocuments {
			return false, nil // No existe el cliente
		}
		return false, err // Otro error
	}
	return true, nil // El cliente ya existe
}

func (r *CustomerRepository) CreateCustomer(ctx context.Context, customer models.Customer) error {
	// Verificar si el cliente ya existe
	exists, err := r.CustomerExists(ctx, customer.CustomerID)
	if err != nil {
		return err
	}
	if exists {
		return ErrCustomerExists // Devolver el error personalizado si ya existe
	}
	_, err = r.collection.InsertOne(ctx, customer)
	return err
}

func (r *CustomerRepository) GetCustomerById(ctx context.Context, customerId string) (models.Customer, error) {
	var customer models.Customer
	err := r.collection.FindOne(ctx, bson.M{"customerId": customerId}).Decode(&customer)
	return customer, err
}
