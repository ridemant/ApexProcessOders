package repository

import "errors"

// Error específico cuando un cliente ya existe
var ErrOrderExists = errors.New("order already exists")
var ErrCustomerExists = errors.New("customer already exists")
var ErrProductExists = errors.New("product already exists")
