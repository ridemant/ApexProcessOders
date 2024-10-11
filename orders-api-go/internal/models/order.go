package models

import "go.mongodb.org/mongo-driver/bson/primitive"

// Modelo del Pedido (Order)
type Order struct {
	ID         primitive.ObjectID `bson:"_id,omitempty" json:"_id,omitempty"` // ObjectId generado por MongoDB
	OrderID    string             `bson:"orderId" json:"orderId"`             // ID del pedido
	CustomerID string             `bson:"customerId" json:"customerId"`       // ID del cliente
	Products   []Product          `bson:"products" json:"products"`           // Lista de productos asociados
}
