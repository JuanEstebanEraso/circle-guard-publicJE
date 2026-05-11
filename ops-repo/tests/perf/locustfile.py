from locust import HttpUser, task, between

class CircleGuardLoadTester(HttpUser):
    # Tiempo de espera entre tareas (simula pensamiento humano)
    wait_time = between(1, 3)

    @task(3)
    def test_gateway_health(self):
        """Prueba de carga sobre el punto de entrada principal"""
        self.client.get("/health")

    @task(2)
    def test_auth_login_attempt(self):
        """Simula intentos de login (alto consumo de CPU por hashing)"""
        self.client.post("/api/auth/login", json={
            "username": "user_stress_test",
            "password": "password123"
        })

    @task(1)
    def test_form_discovery(self):
        """Simula navegación hacia el servicio de formularios"""
        self.client.get("/api/forms")

    @task(1)
    def test_identity_check(self):
        """Simula verificación de identidad"""
        self.client.get("/api/identity")
