package repository

import (
	"context"
	"go.mongodb.org/mongo-driver/mongo"
	"go.mongodb.org/mongo-driver/mongo/options"
	"log"
	"time"
)

var MongoClient *mongo.Client

func InitializeMongoDB(uri string) *mongo.Client {
	ctx, cancel := context.WithTimeout(context.Background(), 10*time.Second)
	defer cancel()

	clientOptions := options.Client().ApplyURI(uri)
	client, err := mongo.Connect(ctx, clientOptions)
	if err != nil {
		log.Fatal(err)
	}

	err = client.Ping(ctx, nil)
	if err != nil {
		log.Fatal(err)
	}

	log.Println("Connected to MongoDB!")
	MongoClient = client
	return client
}

func GetMongoCollection(client *mongo.Client, dbName string, collectionName string) *mongo.Collection {
	return client.Database(dbName).Collection(collectionName)
}
