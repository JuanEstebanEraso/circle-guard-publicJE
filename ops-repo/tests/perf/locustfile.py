from locust import HttpUser, task, between

class CircleGuardLoadTester(HttpUser):
    # Tiempo de espera entre tareas (simula pensamiento humano)
    wait_time = between(1, 3)

    @task(3)
    def test_gateway_health(self):
        """Prueba de carga sobre el punto de entrada principal"""
        with self.client.get("/health", catch_response=True) as response:
            if response.status_code in [200, 404]:
                response.success()

    @task(2)
    def test_auth_login_attempt(self):
        """Simula intentos de login (alto consumo de CPU por hashing)"""
        with self.client.post("/api/auth/login", json={
            "username": "user_stress_test",
            "password": "password123"
        }, catch_response=True) as response:
            if response.status_code in [200, 401, 404]:
                response.success()

    @task(1)
    def test_form_discovery(self):
        """Simula navegación hacia el servicio de formularios"""
        with self.client.get("/api/forms", catch_response=True) as response:
            if response.status_code in [200, 401, 403, 404]:
                response.success()

    @task(1)
    def test_identity_check(self):
        """Simula verificación de identidad"""
        with self.client.get("/api/identity", catch_response=True) as response:
            if response.status_code in [200, 401, 403, 404]:
                response.success()
