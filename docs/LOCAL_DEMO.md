# Local synthetic login

Run the API with `mvn -f backend/api/pom.xml spring-boot:run` after `mvn install -DskipTests`; run the UI with `npm --prefix frontend run dev`. The API is at `http://127.0.0.1:8080` and the UI at `http://127.0.0.1:5173`.

All fixture accounts use password `demo-password`; they exist only under the `synthetic` Spring profile and must never be moved to production. Login creates an HttpOnly session cookie; `POST /api/auth/logout` invalidates it.

| Profile | Username |
| --- | --- |
| AM | `am.alex` |
| PO | `po.petra` |
| IA | `ia.ines` |
| Authorising Officer | `ao.aaron` |
| Finance | `finance.fran` |
| Provider contact | `provider.pavel` |
| CNU | `cnu.clara` or `cnu.niko` |
| Attendee | `attendee.aria` |
| Technical administrator | `admin.taylor` |
