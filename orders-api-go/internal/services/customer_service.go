package services

import (
	"context"
	"errors"
	"orders-api-go/internal/models"
	"orders-api-go/internal/repository"
)

type CustomerService struct {
	customerRepo *repository.CustomerRepository
}

// NewCustomerService crea una nueva instancia del servicio de clientes
func NewCustomerService(customerRepo *repository.CustomerRepository) *CustomerService {
	return &CustomerService{customerRepo: customerRepo}
}

// CreateCustomer intenta crear un cliente si no existe
func (s *CustomerService) CreateCustomer(ctx context.Context, customer models.Customer) error {
	// Validar que el customerId y el nombre estén presentes
	if customer.CustomerID == "" || customer.Name == "" {
		return errors.New("customer must have a valid ID and name")
	}

	// Verificar si el cliente ya existe
	err := s.customerRepo.CreateCustomer(ctx, customer)
	if err != nil {
		if err == repository.ErrCustomerExists {
			return repository.ErrCustomerExists // Devolver el error personalizado si ya existe
		}
		return err
	}

	return nil // Cliente creado con éxito
}

func (s *CustomerService) GetCustomerById(ctx context.Context, customerId string) (models.Customer, error) {
	if customerId == "" {
		return models.Customer{}, errors.New("customerId is required")
	}

	return s.customerRepo.GetCustomerById(ctx, customerId)
}
