# Contract-Note-Generation-and-Delivery-Service


A serverless system for generating, digitally signing, and delivering contract notes using Kafka, AWS Lambda/Azure Functions, and cloud-native services.





Features

Kafka-Driven Workflow: Event-triggered processing of financial transactions.

Dynamic PDF Generation: Create compliant contract notes using JasperReports/iText.

Digital Signing: Supports PKCS#11 (USB tokens) and PKCS#12 (software-based signing).

Secure Storage: Metadata in MSSQL, signed PDFs in cloud storage (S3/Azure Blob).

Email Delivery: Send personalized emails with attachments via SMTP services.

Serverless Architecture: Auto-scaling with AWS Lambda/Azure Functions.

Technologies

Backend: Spring Boot 3.1, Java 17

Messaging: Apache Kafka

PDF Generation: JasperReports, iText

Digital Signing: BouncyCastle, PKCS#11/PKCS#12

Database: MSSQL, DynamoDB/Cosmos DB (metadata)

Cloud: AWS Lambda, Azure Functions, S3/Blob Storage

Email: Amazon SES, SMTP

Installation

Prerequisites

Java 17

Maven 3.8+

Kafka cluster (Confluent/self-hosted)

Cloud provider account (AWS/Azure)

Clone the repository:

git clone https://github.com/yourusername/contract-note-system.git

Configure environment variables (see Configuration).

Build the project:

mvn clean install

Configuration

Update application.yml or set environment variables:

KAFKA_BROKERS: your-kafka-brokers
SCHEMA_REGISTRY_URL: http://schema-registry:8081
DATABASE_URL: jdbc:sqlserver://your-db-host:1433;databaseName=YourDB
SMTP_USERNAME: your-email@domain.com
SMTP_PASSWORD: your-email-password
PKCS11_LIBRARY_PATH: /path/to/pkcs11.dll  # For hardware tokens
SIGN_TYPE: soft  # or "hard" for USB tokens

Usage

Endpoints

Generate PDF Contract NotePOST /service/generate-pdfRequest body: Transaction details (JSON).

Health CheckGET /healthReturns Health-Check Success......

Running the Application

mvn spring-boot:run

API Documentation

Endpoint

Method

Description

/service/generate-pdf

POST

Generate and sign a contract note

/service/health

GET

Check service status

Contributing

Fork the repository.

Create a feature branch: git checkout -b feature/your-feature.

Commit changes: git commit -m 'Add your feature'.

Push to the branch: git push origin feature/your-feature.

Open a pull request.

License

Distributed under the MIT License. See LICENSE for details.

Acknowledgements

Guidance: Jeevan Lal (Geojit Technologies)

Technical Support: Unnie Varghese Puthengadi, Varghese Paul

Infrastructure: Geojit Technologies

