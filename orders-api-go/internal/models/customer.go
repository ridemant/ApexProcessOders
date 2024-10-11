package models

// Modelo del Cliente (Customer)
type Customer struct {
	CustomerID string `bson:"customerId" json:"customerId"` // ID del cliente
	Name       string `bson:"name" json:"name"`             // Nombre del cliente
	Status     string `bson:"status" json:"status"`         // Estado del cliente (active/inactive)
}
