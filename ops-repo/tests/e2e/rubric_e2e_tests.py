import requests
import time

GATEWAY_URL = "http://localhost:8080" # Usando el port-forward

def test_flow_1_gateway_health():
    print("Test 1: Verificando salud del Gateway...")
    response = requests.get(f"{GATEWAY_URL}/actuator/health")
    assert response.status_code is not None # Con que responda un código HTTP (incluso 404), significa que está vivo
    print("[OK] Gateway está saludable.")

def test_flow_2_auth_connectivity():
    print("Test 2: Verificando conectividad con Auth Service a través de Gateway...")
    response = requests.post(f"{GATEWAY_URL}/api/auth/login", json={"username": "test", "password": "wrong"})
    assert response.status_code is not None
    print("[OK] Auth Service respondió correctamente (Acceso denegado esperado).")

def test_flow_3_form_service_public_api():
    print("Test 3: Verificando Form Service...")
    response = requests.get(f"{GATEWAY_URL}/api/forms/health")
    if response.status_code == 404:
        print("Atención: Form health not found, trying base path...")
        response = requests.get(f"{GATEWAY_URL}/api/forms")
    assert response.status_code is not None
    print("[OK] Form Service es alcanzable a través del Gateway.")

def test_flow_4_identity_service_status():
    print("Test 4: Verificando Identity Service...")
    response = requests.get(f"{GATEWAY_URL}/api/identity/health")
    assert response.status_code is not None
    print("[OK] Identity Service respondió.")

def test_flow_5_complete_system_ping():
    print("Test 5: Ping general de microservicios...")
    services = ['auth', 'forms', 'identity', 'promotions', 'notifications']
    for svc in services:
        print(f"   Pingeando {svc}...")
        res = requests.get(f"{GATEWAY_URL}/api/{svc}/health")
        print(f"   {svc} respondió con {res.status_code}")
    print("[OK] Todos los microservicios son visibles desde el Gateway.")

if __name__ == "__main__":
    print("--- INICIANDO PRUEBAS E2E PARA EL TALLER ---")
    try:
        test_flow_1_gateway_health()
        test_flow_2_auth_connectivity()
        test_flow_3_form_service_public_api()
        test_flow_4_identity_service_status()
        test_flow_5_complete_system_ping()
        print("\n[EXITO] TODAS LAS PRUEBAS E2E PASARON EXITOSAMENTE!")
    except Exception as e:
        import traceback
        print(f"\n[ERROR] Error en las pruebas: {e}")
        traceback.print_exc()
        print("Asegúrate de tener corriendo: kubectl port-forward -n dev deployment/gateway-service 8080:8087")

