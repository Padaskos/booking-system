# Booking System — Functional Requirements

## 1. Purpose

The Booking System allows people to reserve shared resources for a specific period.

Examples of resources include:

- Meeting rooms
- Laptops
- Cameras
- Company cars

The system should make it clear which resources are available and prevent the same resource from being assigned to multiple people at the same time.

This document describes the expected business behaviour. Technical decisions about the API, database, security implementation, and user interface are made during development.

---

## 2. Business objective

The organisation needs a central place where shared resources can be registered, found, and reserved.

The system should:

- Give users an overview of available resources.
- Allow users to make and manage reservations.
- Allow administrators to manage the available resources.
- Prevent conflicting reservations.
- Preserve relevant resource and booking history.

---

## 3. Domain terminology

### Visitor

A person who has not yet logged in.\
A visitor can create an account and log in but cannot make a booking.

### User

A registered person who can view and reserve resources.\
An administrator is also a user but has additional permissions.

### Administrator

A user responsible for managing resources and their availability. \
An administrator can register, update, activate, deactivate, and block resources for maintenance.

### Resource

An item or location that can be reserved.\
Examples are rooms, laptops, cameras, and cars.\
A resource can be active or inactive:

- An active resource can be displayed and reserved.
- An inactive resource remains known to the system but cannot be reserved.

### Booking

A reservation connecting a user to a resource for a specific period.\
A booking may be active or cancelled.

### Maintenance period

A period during which a resource is intentionally unavailable,\
for example because it is being repaired or inspected.

### Availability

A resource is available when:

- It is active.
- It is not already booked during the requested period.
- It is not blocked for maintenance during the requested period.

---

## 4. Minimum usable implementation

The first usable version must support the complete basic booking process:

1. An administrator can register and maintain resources.
2. A visitor can create an account.
3. A registered user can log in.
4. A user can find and inspect active resources.
5. A user can check whether a resource is available.
6. A user can reserve an available resource.
7. A user can view and cancel their own bookings.
8. Conflicting bookings are prevented.

The system is considered minimally usable only when this complete flow works.

---

## 5. Scope and priorities

The MoSCoW method is used to describe priorities.

### Must have

Required for the minimum usable implementation:

- Creating and maintaining resources
- Activating and deactivating resources
- Registering user accounts
- Logging in
- Separating user and administrator permissions
- Finding and viewing resources
- Checking availability
- Creating bookings
- Viewing personal bookings
- Cancelling bookings
- Preventing conflicting bookings

### Should have

Important, but not required for the first usable version:

- Maintenance periods for resources
- Filtering and sorting larger lists
- Preserving cancelled bookings as history

### Could have

Possible future improvements:

- Booking reminders
- Booking approval
- Waiting lists
- Recurring bookings
- User notifications
- Saved resource searches
- Administrator reporting

### Won’t have for now

Explicitly excluded from the current project:

- Payments
- Cancellation fees
- Third-party login
- Multi-factor authentication
- Multiple organisations
- Native mobile applications

---

# 6. Functional areas

## 6.1 Resource catalogue

Administrators maintain the resources that users can reserve.

Every resource has:

- A name
- An optional description
- A type
- An optional location
- An active status

Initially supported resource types are:

- Room
- Laptop
- Camera
- Car

Users can search the catalogue and inspect individual resources.

## 6.2 User accounts and access

Visitors can create a personal account.\
Users must log in before they can make or manage bookings. \
Administrative actions are only available to administrators.\
A newly registered account is a regular user account.

## 6.3 Resource availability

Availability is determined for a requested period.\
A resource is unavailable when it is:

- Inactive
- Already booked during an overlapping period
- Blocked for maintenance

Periods that only touch at their start or end do not conflict.\
For example, a booking ending at 12:00 does not conflict with another booking starting at 12:00.

## 6.4 Bookings

An authenticated user can reserve an available resource for a future period.\
A user can view and cancel their own bookings. Cancelling a booking makes the period available again.\
Bookings are retained so that their history is not lost.

## 6.5 Maintenance

Administrators can block a resource during maintenance.\
A maintenance period cannot conflict with an existing active booking.\
Users cannot make bookings that overlap maintenance.

---

# 7. Business rules

## Resources

- A resource must have a name and type.
- A new resource is active by default.
- Inactive resources remain in the system.
- Inactive resources cannot be booked.
- Only administrators can change resource information or status.

## Accounts

- Every account must have a name, email address, and password.
- Email addresses identify accounts and must be unique regardless of letter casing.
- Passwords must be handled securely.
- New accounts receive the regular user role.

## Bookings

- A booking must have a start and end time.
- The start must occur before the end.
- A booking cannot begin in the past.
- A user can only book active and available resources.
- A cancelled booking no longer affects availability.
- Two overlapping bookings for the same resource cannot both be confirmed.

---

# 8. User stories

## Resource catalogue

### US-001 — Register a resource

**Priority:** Must have

**As an administrator,**  
I want to register a bookable resource,  
so that users can reserve it.

#### Acceptance criteria

- A resource has a name, type, optional description, and optional location.
- Supported types are room, laptop, camera, and car.
- A new resource is active by default.
- Incomplete or invalid information is rejected.
- A successfully registered resource remains available in the system.

---

### US-002 — Find resources

**Priority:** Must have

**As a user,**  
I want to search and browse available resources,  
so that I can find a suitable resource.

#### Acceptance criteria

- Active resources can be viewed.
- Resources can be searched by name.
- Resources can be filtered by type and location.
- Search and filter options can be combined.
- Results can be divided into manageable pages.
- Results can be ordered by name or location.
- An empty result is displayed without causing an error.

---

### US-003 — View resource details

**Priority:** Must have

**As a user,**  
I want to view the details of a resource,  
so that I can decide whether it is suitable.

#### Acceptance criteria

- The details of an existing resource can be viewed.
- Details include its name, description, type, location, and status.
- Inactive resources can still be inspected directly.
- The user is informed when the requested resource does not exist.

---

### US-004 — Change resource information

**Priority:** Must have

**As an administrator,**  
I want to update a resource,  
so that its information remains accurate.

#### Acceptance criteria

- The administrator can change the name, description, type, and location.
- The same information rules used during registration remain applicable.
- The identity and current status of the resource are not changed accidentally.
- Invalid changes are rejected without modifying the resource.
- The administrator is informed when the resource does not exist.

---

### US-005 — Change resource availability status

**Priority:** Must have

**As an administrator,**  
I want to activate or deactivate a resource,  
so that unavailable resources cannot be booked.

#### Acceptance criteria

- An active resource can be deactivated.
- An inactive resource can be reactivated.
- A deactivated resource remains stored in the system.
- Inactive resources are not included in the regular catalogue.
- Repeating the same status change does not produce an unwanted result.
- The administrator is informed when the resource does not exist.

---

## User accounts and access

### US-006 — Register an account

**Priority:** Must have

**As a visitor,**  
I want to create an account,  
so that I can reserve resources.

#### Acceptance criteria

- A visitor provides a name, email address, and password.
- The email address must be valid and unique.
- Email addresses differing only in letter casing are considered the same.
- The password must contain at least ten characters.
- Passwords are stored securely and are never displayed.
- A new account receives the regular user role.
- Invalid or duplicate account information is rejected.

---

### US-007 — Log in

**Priority:** Must have

**As a registered user,**  
I want to log in,  
so that I can use protected booking features.

#### Acceptance criteria

- A user can log in with valid account details.
- Invalid account details do not provide access.
- A successful login allows the user to perform permitted actions.
- Sensitive account information is never exposed.

---

### US-008 — Restrict administrative actions

**Priority:** Must have

**As an administrator,**  
I want resource administration to be restricted,  
so that regular users cannot change company resources.

#### Acceptance criteria

- Only administrators can register or change resources.
- Regular users can view resources but cannot manage them.
- Users who have not logged in cannot perform protected actions.
- Existing functionality remains available to users with the correct permissions.

---

## Availability and bookings

### US-009 — Check resource availability

**Priority:** Must have

**As a user,**  
I want to check whether a resource is available during a period,  
so that I know whether I can reserve it.

#### Acceptance criteria

- The user provides a valid future period.
- The result clearly indicates whether the resource is available.
- Inactive resources are unavailable.
- Existing bookings and maintenance periods affect availability.
- The user is informed when the resource does not exist.

---

### US-010 — Reserve a resource

**Priority:** Must have

**As an authenticated user,**  
I want to reserve an available resource,  
so that I can use it during a selected period.

#### Acceptance criteria

- The user selects a resource and a valid future period.
- The user may provide a reason or purpose for the booking.
- The resource must exist, be active, and be available.
- The booking belongs to the user who created it.
- Conflicting reservations are rejected.
- A successful booking is stored and confirmed.

---

### US-011 — View personal bookings

**Priority:** Must have

**As an authenticated user,**  
I want to view my bookings,  
so that I can manage my reservations.

#### Acceptance criteria

- A user only sees their own bookings.
- Booking information includes the resource, period, purpose, and status.
- Upcoming and previous bookings can be distinguished.
- Bookings can be filtered by status.
- Upcoming bookings are shown in chronological order.
- No bookings results in an empty overview.

---

### US-012 — Cancel a booking

**Priority:** Must have

**As an authenticated user,**  
I want to cancel my booking,  
so that the resource becomes available to others.

#### Acceptance criteria

- A user can cancel their own active booking.
- An administrator can cancel any booking.
- A cancelled booking remains stored as history.
- A cancelled booking no longer blocks the resource.
- Repeating a cancellation does not produce an unwanted result.
- A user cannot cancel another user’s booking.

---

## Resource maintenance

### US-013 — Plan resource maintenance

**Priority:** Should have

**As an administrator,**  
I want to make a resource unavailable during maintenance,  
so that it cannot be reserved during that period.

#### Acceptance criteria

- The administrator provides a future period and a reason.
- The maintenance period must have a valid start and end.
- The period cannot conflict with an existing active booking.
- Users cannot make bookings that overlap maintenance.
- The administrator is informed when the resource does not exist or the period conflicts.

---

## Booking reliability

### US-014 — Prevent simultaneous conflicting bookings

**Priority:** Must have

**As a user,**  
I want a confirmed booking to remain reliable,  
so that another user cannot reserve the same resource for the same period.

#### Acceptance criteria

- Two overlapping reservations for the same resource cannot both be confirmed.
- One valid reservation may succeed while the conflicting reservation is rejected.
- Non-overlapping reservations remain possible.
- The confirmed booking remains correctly stored.

---

# 9. Future changes

New requirements should be described in a separate document when they extend or change the behaviour defined here.

Such a document should briefly explain:

1. The existing situation
2. The requested change
3. The reason for the change
4. The affected roles and concepts
5. The new or changed acceptance criteria

This keeps the original requirements understandable while making later changes traceable.