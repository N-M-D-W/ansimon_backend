# Environment profile separation

## Goal

Run the same application with development-friendly local settings and
deployment-safe production settings without committing credentials.

## Profile layout

- `application.yml` contains only settings shared by every environment. It does
  not select an active profile and contains no local database defaults.
- `application-local.yml` provides local defaults for MySQL, enables formatted
  SQL logs, and preserves the current local development experience.
- `application-prod.yml` requires database and external-service values through
  environment variables. It does not provide credential defaults and disables
  verbose SQL formatting.

## Profile selection

The runtime selects the profile through `SPRING_PROFILES_ACTIVE`:

- Local: `local`
- Deployment: `prod`

No profile is hard-coded in a committed YAML file. Deployment systems must set
`SPRING_PROFILES_ACTIVE=prod` together with required secrets.

## Configuration ownership

Common server, actuator, vector-store, and external API endpoint structure
remains in `application.yml`. Environment-specific values move to the
corresponding profile file. `.env.example` documents the variables without
containing real credentials.

## Error handling and verification

Starting with `prod` and an omitted required database value must fail fast
rather than connecting with a development default. Verification will check the
resolved local and production configuration and run the Gradle test suite.
