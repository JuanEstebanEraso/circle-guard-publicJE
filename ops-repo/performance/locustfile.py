import uuid
from locust import HttpUser, task, between

class CircleGuardUser(HttpUser):
    wait_time = between(1, 5)
    
    # Pre-calculated header for a test user with 'identity:map' and 'identity:lookup' permissions
    # In a real scenario, we would get this from the Auth Service
    headers = {
        "Authorization": "Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0LXVzZXIiLCJwZXJtaXNzaW9ucyI6WyJpZGVudGl0eTptYXAiLCJpZGVudGl0eTpsb29rdXAiLCJwcm9tb3Rpb246dXBkYXRlIl19.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c"
    }

    @task(3)
    def map_identity(self):
        """Simulates users registering their real identity to get an anonymous ID"""
        real_email = f"user_{uuid.uuid4().hex[:8]}@example.com"
        self.client.post(
            "/identity/api/v1/identities/map",
            json={"realIdentity": real_email},
            headers=self.headers
        )

    @task(2)
    def lookup_identity(self):
        """Simulates administrative lookup of a real identity (High sensitivity)"""
        # Using a fixed ID for the sake of the test or a random UUID
        test_id = str(uuid.uuid4())
        self.client.get(
            f"/identity/api/v1/identities/lookup/{test_id}",
            headers=self.headers
        )

    @task(5)
    def update_health_status(self):
        """Simulates constant health status updates (The core high-traffic flow)"""
        test_id = str(uuid.uuid4())
        statuses = ["ACTIVE", "SUSPECT", "CONFIRMED"]
        import random
        status = random.choice(statuses)
        
        self.client.post(
            "/promotion/api/v1/health-status/update",
            json={
                "anonymousId": test_id,
                "status": status
            },
            headers=self.headers
        )

    @task(1)
    def get_visitor_id(self):
        """Public endpoint for new visitors"""
        self.client.post("/identity/api/v1/identities/visitor")
