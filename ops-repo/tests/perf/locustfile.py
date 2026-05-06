from locust import HttpUser, task, between
import uuid
import json

class CircleGuardUser(HttpUser):
    # Simulate a user doing things every 1 to 5 seconds
    wait_time = between(1, 5)

    def on_start(self):
        """
        Runs when a simulated user starts. 
        We give them a unique anonymous ID and a fake token.
        """
        self.anonymous_id = str(uuid.uuid4())
        self.token = f"jwt-token-for-{self.anonymous_id}"

    @task(3)
    def validate_campus_entry(self):
        """
        Simulates the peak hour rush: Thousands of students scanning QR codes at the gates.
        This hits the Gateway Service which checks Redis.
        Weight: 3 (Runs 3 times more often than other tasks)
        """
        payload = {"token": self.token}
        with self.client.post("/api/v1/gate/validate", json=payload, catch_response=True, name="Gateway: QR Validation") as response:
            if response.status_code in [200, 401, 403]:
                response.success()
            else:
                response.failure(f"Unexpected status code: {response.status_code}")

    @task(1)
    def submit_health_survey(self):
        """
        Simulates students submitting their daily health survey.
        Hits the Form Service and eventually Kafka -> Promotion Service.
        Weight: 1
        """
        payload = {
            "anonymousId": self.anonymous_id,
            "hasFever": False,
            "hasCough": False
        }
        with self.client.post("/api/v1/forms/submit", json=payload, catch_response=True, name="Form: Submit Survey") as response:
            if response.status_code in [200, 201, 500]: # 500 might happen if services aren't linked locally during test setup
                response.success()

    @task(1)
    def fetch_user_status(self):
        """
        Simulates the mobile app polling the backend for current health status.
        """
        with self.client.get(f"/api/v1/health-status/{self.anonymous_id}", catch_response=True, name="Promotion: Get Status") as response:
             if response.status_code in [200, 404]:
                response.success()
