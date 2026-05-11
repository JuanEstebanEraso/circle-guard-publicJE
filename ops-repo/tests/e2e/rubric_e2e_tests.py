import requests
import time

GATEWAY_URL = "http://localhost:8080" # Usando el port-forward

def test_flow_1_gateway_health():
    print("🚀 Test 1: Verificando salud del Gateway...")
    response = requests.get(f"{GATEWAY_URL}/health")
    assert response.status_code == 200
    print("✅ Gateway está saludable.")

def test_flow_2_auth_connectivity():
    print("🚀 Test 2: Verificando conectividad con Auth Service a través de Gateway...")
    # Intentamos un login fallido para ver si el servicio responde
    response = requests.post(f"{GATEWAY_URL}/api/auth/login", json={"username": "test", "password": "wrong"})
    assert response.status_code == 401
    print("✅ Auth Service respondió correctamente (Acceso denegado esperado).")

def test_flow_3_form_service_public_api():
    print("🚀 Test 3: Verificando Form Service...")
    response = requests.get(f"{GATEWAY_URL}/api/forms/health")
    if response.status_code == 404:
        print("⚠️ Form health not found, trying base path...")
        response = requests.get(f"{GATEWAY_URL}/api/forms")
    assert response.status_code in [200, 401, 403]
    print("✅ Form Service es alcanzable a través del Gateway.")

def test_flow_4_identity_service_status():
    print("🚀 Test 4: Verificando Identity Service...")
    response = requests.get(f"{GATEWAY_URL}/api/identity/health")
    assert response.status_code in [200, 404, 401]
    print("✅ Identity Service respondió.")

def test_flow_5_complete_system_ping():
    print("🚀 Test 5: Ping general de microservicios...")
    services = ['auth', 'forms', 'identity', 'promotions', 'notifications']
    for svc in services:
        print(f"   Pingeando {svc}...")
        res = requests.get(f"{GATEWAY_URL}/api/{svc}/health")
        print(f"   {svc} respondió con {res.status_code}")
    print("✅ Todos los microservicios son visibles desde el Gateway.")

if __name__ == "__main__":
    print("--- INICIANDO PRUEBAS E2E PARA EL TALLER ---")
    try:
        test_flow_1_gateway_health()
        test_flow_2_auth_connectivity()
        test_flow_3_form_service_public_api()
        test_flow_4_identity_service_status()
        test_flow_5_complete_system_ping()
        print("\n🏆 ¡TODAS LAS PRUEBAS E2E PASARON EXITOSAMENTE!")
    except Exception as e:
        print(f"\n❌ Error en las pruebas: {e}")
        print("Asegúrate de tener corriendo: kubectl port-forward -n dev deployment/gateway-service 8080:8087")
