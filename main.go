package main

import (
	"encoding/json"
	"fmt"
	"log"

	"github.com/confluentinc/confluent-kafka-go/kafka"
	"gopkg.in/gomail.v2"
)

type OrderEvent struct {
	OrderEmail      string  `json:"orderEmail"`
	OrderNumber     string  `json:"orderNumber"`
	OrderStatus     string  `json:"orderStatus"`
	TotalOrderValue float64 `json:"totalOrderValue"`
}

func main() {

	broker := "localhost:9092"
	groupID := "goNotification"
	topic := "notificationId"

	configMap := &kafka.ConfigMap{
		"bootstrap.servers": broker,
		"group.id":          groupID,
		// "auto.offset.reset":  "earliest",
		// "enable.auto.commit": "true",
	}

	consumer, err := kafka.NewConsumer(configMap)
	if err != nil {
		log.Fatal(err)
	}

	// Subscribe to the Kafka topic
	err = consumer.SubscribeTopics([]string{topic}, nil)
	if err != nil {
		log.Fatal(err)
	}

	fmt.Println("Order Email Service is running...")

	for {
		msg, err := consumer.ReadMessage(-1)
		if err == nil {
			go processOrderEvent(msg.Value)
		} else {
			fmt.Printf("Error: %v (%v)\n", err, msg)
		}
	}
}

func processOrderEvent(message []byte) {
	var orderEvent OrderEvent
	err := json.Unmarshal(message, &orderEvent)
	if err != nil {
		fmt.Println("Error decoding JSON:", err)
		return
	}

	fmt.Printf("Order ID: %s, User Email: %s, Status: %s , Order Value: %f\n", orderEvent.OrderNumber, orderEvent.OrderEmail, orderEvent.OrderStatus, orderEvent.TotalOrderValue)
	sendEmail(orderEvent)

}

func sendEmail(orderEvent OrderEvent) {

	senderEmail := "rohan.dsouza.7076@gmail.com"
	appPassword := "drpezddyckzecgdq"

	m := gomail.NewMessage()
	m.SetHeader("From", senderEmail)
	m.SetHeader("To", orderEvent.OrderEmail)
	m.SetHeader("Subject", "Order Status")
	htmlBody := fmt.Sprintf("<html><body><h2>Your Order Details</h2><p>Order ID: %s</p><p>Order Value: %f</p><p>Status: %s</p></body></html>",
		orderEvent.OrderNumber, orderEvent.TotalOrderValue, orderEvent.OrderStatus)
	m.SetBody("text/html", htmlBody)

	d := gomail.NewDialer("smtp.gmail.com", 587, senderEmail, appPassword)
	d.TLSConfig = nil

	if err := d.DialAndSend(m); err != nil {
		log.Fatal(err)
	} else {
		fmt.Println("Email sent successfully.")
	}
}
