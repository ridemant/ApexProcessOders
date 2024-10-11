package repository

import (
	"context"
	"go.mongodb.org/mongo-driver/bson"
	"go.mongodb.org/mongo-driver/mongo"
	"orders-api-go/internal/models"
)

type Product struct {
	ProductID   string  `bson:"productId"`
	Name        string  `bson:"name"`
	Description string  `bson:"description"`
	Price       float64 `bson:"price"`
}

type ProductRepository struct {
	collection *mongo.Collection
}

func NewProductRepository(client *mongo.Client) *ProductRepository {
	collection := GetMongoCollection(client, "apexglobal", "products")
	return &ProductRepository{collection: collection}
}

func (r *ProductRepository) ProductExists(ctx context.Context, productId string) (bool, error) {
	var product models.Product
	err := r.collection.FindOne(ctx, bson.M{"productId": productId}).Decode(&product)
	if err != nil {
		if err == mongo.ErrNoDocuments {
			return false, nil // No existe el producto
		}
		return false, err // Otro error
	}
	return true, nil // El producto ya existe
}

// Crear un nuevo producto
func (r *ProductRepository) CreateProduct(ctx context.Context, product models.Product) error {
	// Verificar si el producto ya existe
	exists, err := r.ProductExists(ctx, product.ProductID)
	if err != nil {
		return err
	}
	if exists {
		return ErrProductExists // Devolver el error personalizado si ya existe
	}
	_, err = r.collection.InsertOne(ctx, product)
	return err
}

func (r *ProductRepository) GetProductById(ctx context.Context, id string) (models.Product, error) {
	var product models.Product
	err := r.collection.FindOne(ctx, bson.M{"productId": id}).Decode(&product)
	return product, err
}
