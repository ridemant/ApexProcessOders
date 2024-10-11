package models

// Modelo del Producto (Product)
type Product struct {
	ProductID   string  `bson:"productId" json:"productId"`     // ID del producto
	Name        string  `bson:"name" json:"name"`               // Nombre del producto
	Description string  `bson:"description" json:"description"` // Descripción del producto
	Price       float64 `bson:"price" json:"price"`             // Precio del producto
}
