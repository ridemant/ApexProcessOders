package utils

import (
	"encoding/json"
	"net/http"
)

// ApiResponse es el formato de respuesta estándar para la API
type ApiResponse struct {
	Status  string `json:"status"`
	Code    int    `json:"code"`
	Message string `json:"message"`
}

// SendResponse envía una respuesta estandarizada en formato JSON
func SendResponse(w http.ResponseWriter, status string, code int, message string) {
	w.WriteHeader(code)
	response := ApiResponse{
		Status:  status,
		Code:    code,
		Message: message,
	}
	json.NewEncoder(w).Encode(response)
}
