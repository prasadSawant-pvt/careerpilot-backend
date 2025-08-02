# CAREERPILOT

*Empowering Careers Through Intelligent, Personalized Growth*

![last-commit](https://img.shields.io/github/last-commit/prasadSawant-pvt/CareerPilot?style=flat&logo=git&logoColor=white&color=0080ff)
![repo-top-language](https://img.shields.io/github/languages/top/prasadSawant-pvt/CareerPilot?style=flat&color=0080ff)
![repo-language-count](https://img.shields.io/github/languages/count/prasadSawant-pvt/CareerPilot?style=flat&color=0080ff)

## 🚀 Built with

![Java](https://img.shields.io/badge/Java-007396.svg?style=flat&logo=Java&logoColor=white)
![Spring](https://img.shields.io/badge/Spring-000000.svg?style=flat&logo=Spring&logoColor=white)
![XML](https://img.shields.io/badge/XML-005FAD.svg?style=flat&logo=XML&logoColor=white)
![JSON](https://img.shields.io/badge/JSON-000000.svg?style=flat&logo=JSON&logoColor=white)
![Markdown](https://img.shields.io/badge/Markdown-000000.svg?style=flat&logo=Markdown&logoColor=white)

---

## 📚 Table of Contents

- [Overview](#overview)
- [Getting Started](#getting-started)
  - [Prerequisites](#prerequisites)
  - [Installation](#installation)
  - [Usage](#usage)
  - [Testing](#testing)

---

## 🧩 Overview

CareerPilot is an all-in-one backend platform designed to facilitate career development through AI-powered insights and robust, scalable microservices built in Spring Boot.

### 🔥 Why CareerPilot?

This project empowers developers to create intelligent, user-centric career prep tools.

- 🤖 **AI Integration:** Seamless Claude model integration for personalized roadmap generation.
- 🔗 **Robust API Layer:** RESTful APIs for managing user input, roadmap generation, and interview question delivery.
- 🔒 **Secure & Scalable:** Follows secure coding practices with modular design using Spring Boot and MongoDB.

---

## 🚀 Getting Started

### 🔧 Prerequisites

- **Programming Language:** Java  
- **Build Tool:** Maven
- **Database:** MongoDB (Atlas or self-hosted)
- **Groq API Key**: For AI model integration

## 🔧 Configuration

### Environment Variables

Create a `.env` file in the root directory based on `.env.example` and set the following environment variables:

#### MongoDB Configuration
- `MONGODB_URI`: MongoDB connection string (e.g., `mongodb+srv://username:password@cluster.mongodb.net/database`)
- `MONGODB_DATABASE`: Database name (default: `pathprep-ai`)
- `MONGODB_SSL_ENABLED`: Enable/disable SSL (default: `true`)
- `MONGODB_CONNECTION_TIMEOUT`: Connection timeout in ms (default: `30000`)
- `MONGODB_SOCKET_TIMEOUT`: Socket timeout in ms (default: `60000`)

#### Groq AI Configuration
- `GROQ_API_KEY`: Your Groq API key
- `GROQ_BASE_URL`: Groq API base URL (default: `https://api.groq.com/openai/v1`)
- `GROQ_TIMEOUT`: Request timeout (default: `30s`)
- `GROQ_MAX_RETRIES`: Maximum retry attempts (default: `3`)

#### Server Configuration
- `SERVER_PORT`: Server port (default: `8080`)
- `SERVER_SERVLET_CONTEXT_PATH`: Base path for API (default: `/api`)

### Running with Docker

1. Build the Docker image:
   ```bash
   docker build -t careerpilot-backend .
   ```

2. Run the container with environment variables:
   ```bash
   docker run -d \
     -p 8080:8080 \
     --env-file .env \
     --name careerpilot-backend \
     careerpilot-backend
   ```

### Deployment to Railway

1. Push your code to a Git repository
2. Create a new project on Railway
3. Connect your repository
4. Add all environment variables from `.env.example` to Railway's environment variables
5. Deploy!

### Local Development

1. Copy `.env.example` to `.env` and update the values
2. Run the application:
   ```bash
   ./mvnw spring-boot:run
   ```

## 🔒 Security Note

Never commit sensitive information like API keys or database credentials to version control. The `.env` file is included in `.gitignore` by default.

---

### 📦 Installation

Build from source and install dependencies:

```sh
# Clone the repository
git clone https://github.com/prasadSawant-pvt/CareerPilot

# Navigate to the project directory
cd CareerPilot

# Install dependencies using Maven
mvn install

#Start the backend server:
mvn spring-boot:run


---

Let me know if you’d like to add environment variables, API endpoints, or architecture diagrams.
