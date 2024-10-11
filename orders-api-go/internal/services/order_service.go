package services

import (
	"context"
	"errors"
	"orders-api-go/internal/models"
	"orders-api-go/internal/repository"
)

type OrderService struct {
	orderRepo *repository.OrderRepository
}

func NewOrderService(orderRepo *repository.OrderRepository) *OrderService {
	return &OrderService{orderRepo: orderRepo}
}

// Crear un nuevo pedido
func (s *OrderService) CreateOrder(ctx context.Context, order models.Order) error {
	if order.OrderID == "" || order.CustomerID == "" {
		return errors.New("order must have a valid ID and customer")
	}

	err := s.orderRepo.CreateOrder(ctx, order)
	if err != nil {
		if err == repository.ErrOrderExists {
			return repository.ErrOrderExists // Devolver el error personalizado si ya existe
		}
		return err
	}

	return nil // Orden creada con éxito
}

// Obtener una orden por su ID
func (s *OrderService) GetOrderById(ctx context.Context, orderId string) (models.Order, error) {
	if orderId == "" {
		return models.Order{}, errors.New("orderId is required")
	}

	return s.orderRepo.GetOrderById(ctx, orderId)
}
