# FoodRescue

FoodRescue is a surplus food donation and redistribution platform.

## Requirements

- Java 17 or later
- Maven 3.6.3 or later
- PostgreSQL
- Node.js 20.19+ or 22.12+

## Run locally

1. Install Java 17+, Maven 3.6.3+, PostgreSQL, and Node.js 20.19+ or 22.12+.
2. Create a PostgreSQL database named `foodrescue`.
3. Provide the backend connection credentials and a generated JWT secret:

```sh
export DB_HOST=localhost
export DB_PORT=5432
export DB_NAME=foodrescue
export DB_USERNAME=your_database_user
export DB_PASSWORD=your_database_password
export JWT_SECRET="$(openssl rand -base64 32)"
export JWT_EXPIRATION=PT1H
export CORS_ALLOWED_ORIGINS=
mvn spring-boot:run
```

Generate a suitable secret locally with `openssl rand -base64 32`; do not commit it to source control.
The JWT secret is required at startup and must be Base64-encoded. `JWT_EXPIRATION` accepts a
Java duration value and defaults to one hour.

Hibernate schema management is set to `validate` in standard mode; execute the provided
`src/main/resources/schema.sql` script in your PostgreSQL database to create all required tables and indexes.
Alternatively, start the backend with `--spring.profiles.active=demo` to automatically create/update tables
and seed initial test accounts for all 4 roles (Donor, NGO, Volunteer, Admin).

The application listens on port `8080` by default. Set `SERVER_PORT` to override it.

`CORS_ALLOWED_ORIGINS` is a comma-separated allowlist of exact browser origins, for
example `https://foodrescue.example,https://admin.foodrescue.example`. Wildcard origins
are rejected. It defaults to empty, which is appropriate when using the Vite development
proxy or serving the frontend from the same origin as the API. Set it when a browser
frontend is hosted on a different origin; do not include a path or trailing slash.

## API documentation

With the application running, open Swagger UI at `http://localhost:8080/swagger-ui.html`
or retrieve the OpenAPI 3 document from `http://localhost:8080/v3/api-docs`.
The documentation endpoints are public. Use Swagger UI's **Authorize** action to
provide a JWT bearer token for protected API operations; registration and login
are documented as public.

## React frontend

The React, TypeScript, and Tailwind application is in `frontend/`.

```sh
cd frontend
cp .env.example .env
npm install
npm run dev
```

The Vite development server proxies `/api` to `http://localhost:8080`. Set
`VITE_PROXY_TARGET` in `.env` to use another backend host. For a deployed frontend,
set `VITE_API_URL` to the backend API base URL (for example,
`https://api.example.com/api/v1`) when building. For a separately hosted production
frontend, set the backend `CORS_ALLOWED_ORIGINS` to the exact frontend origin.

The login and registration pages call the backend auth API and store the returned
JWT session in browser `localStorage` so sign-in survives reloads. Because browser
storage is accessible to JavaScript, production deployments should mitigate
cross-site scripting and use HTTPS.

### Local end-to-end run

1. Start PostgreSQL with the initialized FoodRescue schema and start the backend as
   described above. Open Swagger UI at `http://localhost:8080/swagger-ui.html` to
   confirm the backend is responding.
2. In another terminal, run `cd frontend`, copy `.env.example` to `.env`, then run
   `npm install` and `npm run dev`.
3. Open the Vite URL printed by the dev server (normally `http://localhost:5173`).
   The development proxy forwards `/api` calls to the backend, so no browser CORS
   allowlist is needed for this local setup.
4. Register separate donor, NGO, and volunteer accounts. Donor users can create and
   publish listings; NGO users must have their organization and membership approved
   before claiming; volunteers can accept and update delivery tasks.
5. Run backend tests from the project root with `mvn test`. Run frontend checks from
   `frontend/` with `npm run build` and `npm run lint`.

Public registration does not permit ADMIN accounts. To provision a local administrator,
first register a dedicated account and then grant its role through a trusted PostgreSQL
administration session:

```sql
INSERT INTO user_roles (user_id, role)
SELECT id, 'ADMIN'
FROM users
WHERE email = lower('admin@example.com')
ON CONFLICT (user_id, role) DO NOTHING;
```

Verify the email and role in `users` and `user_roles` before using the account. In
production, provision administrators through the organization's controlled database
administration process; never expose role grants through public registration.

## Food donation and claim API

All endpoints below require a bearer token except registration and login.

- Donors create or update their profile with `PUT /api/v1/donor/profile`.
- Donors create drafts with `POST /api/v1/donor/listings`, publish with `POST
  /api/v1/donor/listings/{listingId}/publish`, edit with `PUT
  /api/v1/donor/listings/{listingId}`, and withdraw with `POST
  /api/v1/donor/listings/{listingId}/withdraw`.
- Donors can list, view, and delete their own draft listings under
  `/api/v1/donor/listings`.
- NGO users create an organization with `POST /api/v1/ngo/organizations`; new
  organizations and memberships start in pending-review states.
- Verified NGOs with active membership can browse `/api/v1/ngo/listings`, claim
  a listing with `POST /api/v1/ngo/listings/{listingId}/claims`, and manage
  claims under `/api/v1/ngo/claims`.
- NGO pickup claims can be completed through the claim API. Volunteer-delivery
  claims are completed through the delivery workflow.

Listing expiry is processed automatically once per minute. NGO organization
verification and membership approval are required before claims can be made.

## Volunteer delivery and notifications

- Volunteers create or update their profile with `PUT /api/v1/volunteer/profile`.
- Volunteers browse open delivery tasks at `GET /api/v1/volunteer/deliveries/open`,
  accept one with `POST /api/v1/volunteer/deliveries/{taskId}/accept`, and update
  progress through `/pickup`, `/delivered`, or `/fail`. An assignment can be
  cancelled before pickup through `/cancel`.
- Volunteers can view their task history at
  `GET /api/v1/volunteer/deliveries/mine`. Full task details and street addresses
  are available only to the assigned volunteer and the involved donor/NGO.
- Donors can view delivery tasks for their listings at `GET /api/v1/donor/deliveries`.
  Active NGO members can view their organization's tasks at
  `GET /api/v1/ngo/deliveries?organizationId={organizationId}`.
- Authenticated users can list in-app notifications at `GET /api/v1/notifications`,
  check `GET /api/v1/notifications/unread-count`, and mark notifications read
  through `PATCH /api/v1/notifications/{notificationId}/read` or
  `PATCH /api/v1/notifications/read-all`.

Delivery tasks snapshot pickup/drop-off addresses when a claim is made, preserving
the details needed for an accepted delivery if a profile address later changes.
Open task summaries expose only city/region; volunteer acceptance is first-come,
and a volunteer can have only one active assignment. Overdue delivery tasks are
processed by the same once-per-minute expiry job as listings and claims.

## Role dashboards and administration

The frontend dashboard provides donor listing and delivery tracking, NGO food
browsing/claims and delivery progress, volunteer task management, and
administrator review and platform metrics. Dashboard data is loaded from the
authenticated role-specific APIs described above.

Administrators can review pending NGO registrations and memberships and view
platform-wide counts through these ADMIN-only endpoints:

- `GET /api/v1/admin/dashboard/metrics`
- `GET /api/v1/admin/organizations/pending`
- `POST /api/v1/admin/organizations/{organizationId}/approve` or `/reject`
- `GET /api/v1/admin/memberships/pending`
- `POST /api/v1/admin/memberships/{membershipId}/approve` or `/reject`

Organization verification and membership activation are separate review
decisions; both are required before an NGO can claim food.
