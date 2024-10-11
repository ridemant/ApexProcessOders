package services

import (
	"context"
	"errors"
	"orders-api-go/internal/models"
	"orders-api-go/internal/repository"
)

type ProductService struct {
	productRepo *repository.ProductRepository
}

func NewProductService(productRepo *repository.ProductRepository) *ProductService {
	return &ProductService{productRepo: productRepo}
}

// Crear un nuevo producto
func (s *ProductService) CreateProduct(ctx context.Context, product models.Product) error {
	// Validar que el productId y el nombre estén presentes
	if product.ProductID == "" || product.Name == "" {
		return errors.New("product must have a valid ID and name")
	}

	// Verificar si el producto ya existe
	err := s.productRepo.CreateProduct(ctx, product)
	if err != nil {
		if err == repository.ErrProductExists {
			return repository.ErrProductExists // Devolver el error personalizado si ya existe
		}
		return err
	}

	return nil // Producto creado con éxito
}

// Obtener un producto por ID
func (s *ProductService) GetProductById(ctx context.Context, id string) (models.Product, error) {
	if id == "" {
		return models.Product{}, errors.New("product ID is required")
	}
	return s.productRepo.GetProductById(ctx, id)
}
