---
name: user-management-integration-test
description: Design for a simple JPA User management system to test database integration.
type: project
---

# User Management Integration Test Design

## Goal
Implement a simple JPA-based User management system to verify database integration, including JPA auditing and optimistic locking.

## Data Model

### BaseEntity (MappedSuperclass)
Provides common auditing and versioning fields for all entities.
- `createdAt`: `LocalDateTime` (Auto-filled by `@CreatedDate`)
- `updatedAt`: `LocalDateTime` (Auto-filled by `@LastModifiedDate`)
- `version`: `Long` (Optimistic locking via `@Version`)

### User Entity
Extends `BaseEntity`.
- `id`: `Long` (Primary Key, Generated)
- `username`: `String` (Unique, Not Null)
- `email`: `String` (Unique, Not Null)
- `fullName`: `String`

### UserRepository
Standard `JpaRepository<User, Long>`.

## Service Layer
`UserService` provides basic CRUD operations wrapping the `UserRepository`.
- `createUser(User user)`
- `getUserById(Long id)`
- `updateUser(Long id, User user)`
- `deleteUser(Long id)`
- `listUsers()`

## API Layer
`UserController` exposes the following REST endpoints:
- `POST /api/users` $\rightarrow$ Create user
- `GET /api/users/{id}` $\rightarrow$ Retrieve user by ID
- `PUT /api/users/{id}` $\rightarrow$ Update user
- `DELETE /api/users/{id}` $\rightarrow$ Delete user
- `GET /api/users` $\rightarrow$ List all users

## Infrastructure
- Enable JPA Auditing using `@EnableJpaAuditing`.
- Use `PostgreSQLDialect` as configured in `application.properties`.