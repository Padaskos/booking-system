# Booking System

Booking System is a personal full-stack training project built to improve my skills with Java, Spring Boot, Angular, databases, testing, and Git.

The application will allow users to book shared resources such as meeting rooms, laptops, cameras, and cars. Planned functionality includes resource management, user accounts, authentication, bookings, availability checks, and maintenance periods.

## Project purpose

I am both the product owner and developer of this project. The goal is to practise the full development process:

- Analyzing user stories
- Translating requirements into technical tasks
- Writing and testing the implementation
- Working with feature branches and pull requests
- Receiving feedback through code reviews
- Building a complete backend and frontend application

The project is intended for learning and is not a production application.

## Technologies

### Backend

- Java
- Spring Boot
- Maven
- PostgreSQL
- Flyway
- Docker

### Frontend

- Angular — planned

## Project structure

```text
booking-system/
├── backend/       # Spring Boot application
├── frontend/      # Angular application (planned)
├── docs/          # User stories
└── compose.yaml   # Local database
```

The user-story backlog is available in:

```text
docs/user-stories.md
```

## Running the project

Start the PostgreSQL database from the project root:

```bash
docker compose up -d
```

Run the backend:

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

Run the tests:

```powershell
cd backend
.\mvnw.cmd clean verify
```

The backend runs at:

```text
http://localhost:8080
```

## Development workflow

Each user story is developed on a separate feature branch. When a story is ready, I open a pull request into `main` so that senior developers can review my work and provide feedback.

## Use of AI

AI was used to help brainstorm the project idea, create and refine the user stories, and explain development concepts.

I write the application code myself and make the final implementation decisions. AI is used as a supporting learning tool rather than to generate the complete project.
