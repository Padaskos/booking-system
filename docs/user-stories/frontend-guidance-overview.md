# Frontend Guidance Overview

This document gives a short overview of the backend for the Angular guidance meeting.

A DTO (**Data Transfer Object**) defines the data exchanged between the Angular frontend and Spring backend. The TypeScript interfaces below represent the existing Java request and response DTOs.

## 1. User flows

- **Register and login**  
  `Register → Login → User details and session cookie`

- **Browse resources**  
  `Resource overview → Resource details`

- **Create a booking**  
  `Resource details → Check availability → Create booking`

- **Manage personal bookings**  
  `My bookings → Upcoming/previous bookings → Cancel booking`

- **Manage resources as administrator**  
  `Admin overview → Create/update resource → Activate/deactivate`

- **Manage bookings as administrator — incomplete**  
  `Select resource → View bookings → Cancel booking`

---

## 2. Backend endpoint overview

| Feature | Endpoint | Access | Request | Response |
|---|---|---|---|---|
| Register | `POST /api/auth/register` | Public | `CreateUserRequest` | Empty — `201` |
| Login | `POST /api/auth/login` | Public | `LoginRequest` | `UserDetailsResponse` |
| List resources | `GET /api/resources` | Authenticated | Filters and pagination | `Page<ResourceResponse>` |
| Resource details | `GET /api/resources/{id}` | Authenticated | Resource ID | `DetailedResourceResponse` |
| Create resource | `POST /api/resources` | ADMIN | `CreateResourceRequest` | Empty — `201` |
| List inactive resources | `GET /api/resources/inactive` | ADMIN | Filters and pagination | `Page<ResourceResponse>` |
| Update resource | `PUT /api/resources/{id}` | ADMIN | `CreateResourceRequest` | `DetailedResourceResponse` |
| Change resource status | `PATCH /api/resources/{id}/status` | ADMIN | `UpdateStatusRequest` | Empty |
| Check availability | `GET /api/bookings/availability/{resourceId}` | Authenticated | Start and end time | `AvailabilityResponse` |
| Create booking | `POST /api/bookings` | Authenticated | `CreateBookingRequest` | Empty — `201` |
| View my bookings | `GET /api/bookings/me` | Authenticated | Period and pagination | `Page<BookingDetailsResponse>` |
| Cancel booking | `DELETE /api/bookings/{bookingId}` | Owner or ADMIN | Booking ID | Empty — `204` |
| View resource bookings | Missing | ADMIN | Resource ID and pagination | Not implemented |

---

## 3. Frontend data contracts

These interfaces describe the JSON data sent between Angular and Spring.

UUID and date-time values are represented as strings in Angular.

### Authentication

```typescript
interface CreateUserRequest {
  name: string;
  email: string;
  password: string;
}

interface LoginRequest {
  email: string;
  password: string;
}

interface UserDetailsResponse {
  id: string;
  name: string;
  email: string;
  role: UserRole;
}

type UserRole = 'USER' | 'ADMIN';
```

Validation:

- Name is required and has a maximum of 100 characters.
- Email is required, must be valid, and has a maximum of 254 characters.
- Password is required and must contain between 10 and 72 characters.

### Resources

```typescript
type ResourceType = 'ROOM' | 'LAPTOP' | 'CAMERA' | 'CAR';

interface CreateResourceRequest {
  name: string;
  description?: string;
  location?: string;
  type: ResourceType;
}

interface ResourceResponse {
  id: string;
  name: string;
  description?: string;
  location?: string;
  type: ResourceType;
}

interface DetailedResourceResponse {
  id: string;
  name: string;
  description?: string;
  location?: string;
  type: ResourceType;
  active: boolean;
  creationTimestamp: string;
  updatedTimestamp: string;
}

interface UpdateStatusRequest {
  active: boolean;
}
```

Validation:

- Name is required, cannot be blank, and has a maximum of 100 characters.
- Description is optional and has a maximum of 500 characters.
- Location is optional and has a maximum of 100 characters.
- Resource type is required.
- Active status is required when changing the resource status.

### Bookings

```typescript
type BookingPeriod = 'UPCOMING' | 'PREVIOUS';

interface CreateBookingRequest {
  resourceId: string;
  startTime: string;
  endTime: string;
  bookingDescription: string;
}

interface BookingDetailsResponse {
  bookingId: string;
  resourceId: string;
  resourceName: string;
  startTime: string;
  endTime: string;
  bookingDescription: string;
}

interface AvailabilityResponse {
  available: boolean;
}
```

Validation:

- Resource ID is required.
- Start and end times are required.
- Start time must occur before end time.
- A booking cannot begin in the past.
- Booking description is required and has a maximum of 500 characters.

### Paginated responses

Endpoints returning `Page<T>` provide a response containing the requested data and pagination information.

Conceptual structure:

```typescript
interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}
```

---

## 4. Session authentication

- Login is handled through Spring Security.
- Successful login creates a server-side session.
- The browser receives a `JSESSIONID` cookie.
- The cookie identifies the authenticated user during later requests.
- Login returns the user’s ID, name, email, and role.
- Public access is limited to registration and login.
- Other endpoints require an authenticated session.
- Administrator operations use `@PreAuthorize("hasRole('ADMIN')")`.
- CSRF is currently disabled.
- An Angular development proxy can forward `/api` requests to Spring Boot.
- With the development proxy, Angular should not need to manage the session cookie manually.

---

## 5. Expected frontend pages

| Page | Information and actions | Backend support |
|---|---|---|
| Login | Enter credentials and log in | Complete |
| Registration | Create a user account | Complete |
| Resource overview | Search, filter, sort and browse resources | Complete |
| Resource details | View details, check availability and create a booking | Complete |
| My bookings | View upcoming/previous bookings and cancel bookings | Complete |
| Admin resource overview | Create, update, activate and deactivate resources | Complete |
| Admin booking overview | View bookings for a selected resource and cancel them | Incomplete |

---

## 6. Backend gaps affecting the frontend

### Current authenticated user

**Recommended before implementing authentication-state restoration**

Missing endpoint:

```http
GET /api/auth/me
```

After refreshing Angular, the backend session may still exist while Angular has lost its in-memory user information.

This endpoint should:

- Return `UserDetailsResponse` for the authenticated user.
- Return `401 Unauthorized` when no valid session exists.

This does not block the initial login page.

### Logout

**Recommended before completing authentication**

Missing endpoint:

```http
POST /api/auth/logout
```

This endpoint should:

- Invalidate the server-side session.
- Clear the authentication.
- Return `204 No Content`.

### Administrator booking overview

**Required for the planned admin booking page**

An administrator cannot currently retrieve all bookings for a selected resource.

A paginated administrator-only endpoint is needed. The response may also need safe information about the user who created each booking.

### Error responses

Error response bodies are currently inconsistent:

- Some errors return a text message.
- Some errors return an empty body.

This does not block initial frontend development, but Angular needs a temporary strategy for displaying errors.

### Administrator account setup

There is currently no endpoint for creating or promoting an administrator.

For development and testing, an administrator must be created through an agreed seed or manual setup process.

---

## 7. Questions for the senior

1. How should the Angular features be structured using components, facade services, and API services?
2. What responsibilities belong in each of those layers?
3. Where should the authenticated user state be stored?
4. How should authenticated and administrator routes be protected?
5. Which UI component library and forms approach should be used?
6. Which vertical frontend flow should be implemented first?