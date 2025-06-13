# Test per la funzionalità di difficoltà degli scenari
# Version 1.0 - Test del parametro difficulty

import requests
import json

# Configurazione
API_BASE_URL = "http://localhost:8000"

def test_difficulty_levels():
    """Test per ottenere i livelli di difficoltà disponibili"""
    print("🔍 Testing difficulty levels endpoint...")
    
    response = requests.get(f"{API_BASE_URL}/scenarios/difficulty-levels")
    
    if response.status_code == 200:
        data = response.json()
        print("✅ Difficulty levels retrieved successfully:")
        for level in data["difficulty_levels"]:
            print(f"   - {level['value']}: {level['description']}")
        print(f"   Default: {data['default']}")
    else:
        print(f"❌ Failed to get difficulty levels: {response.status_code}")

def test_scenario_generation_with_difficulty():
    """Test per generare scenari con diversi livelli di difficoltà"""
    
    base_request = {
        "description": "Paziente con dolore toracico in pronto soccorso",
        "scenario_type": "Advanced Scenario",
        "target": "Studenti di medicina"
    }
    
    difficulties = ["Facile", "Medio", "Difficile"]
    
    for difficulty in difficulties:
        print(f"\n🧪 Testing scenario generation with difficulty: {difficulty}")
        
        request_data = base_request.copy()
        request_data["difficulty"] = difficulty
        
        response = requests.post(
            f"{API_BASE_URL}/scenarios/generate-scenario",
            json=request_data,
            headers={"Content-Type": "application/json"}
        )
        
        if response.status_code == 200:
            print(f"✅ Scenario generated successfully for difficulty: {difficulty}")
            
            # Salva lo scenario per analisi
            scenario = response.json()
            filename = f"test_scenario_{difficulty.lower()}.json"
            with open(filename, 'w', encoding='utf-8') as f:
                json.dump(scenario, f, ensure_ascii=False, indent=2)
            print(f"   Scenario saved to: {filename}")
            
            # Mostra informazioni basic del scenario
            scenario_info = scenario.get("scenario", {})
            print(f"   Titolo: {scenario_info.get('titolo', 'N/A')}")
            print(f"   Patologia: {scenario_info.get('patologia', 'N/A')}")
            print(f"   Numero tempi: {len(scenario.get('tempi', []))}")
            
        else:
            print(f"❌ Failed to generate scenario for difficulty {difficulty}: {response.status_code}")
            print(f"   Error: {response.text}")

def test_scenario_generation_without_difficulty():
    """Test per verificare che il valore di default funzioni"""
    print(f"\n🧪 Testing scenario generation WITHOUT difficulty parameter...")
    
    request_data = {
        "description": "Simulazione di rianimazione cardiopolmonare",
        "scenario_type": "Patient Simulated Scenario",
        "target": "Infermieri"
    }
    
    response = requests.post(
        f"{API_BASE_URL}/scenarios/generate-scenario",
        json=request_data,
        headers={"Content-Type": "application/json"}
    )
    
    if response.status_code == 200:
        print("✅ Scenario generated successfully without difficulty parameter (using default)")
        scenario = response.json()
        
        # Salva lo scenario
        with open("test_scenario_default.json", 'w', encoding='utf-8') as f:
            json.dump(scenario, f, ensure_ascii=False, indent=2)
        print("   Scenario saved to: test_scenario_default.json")
        
    else:
        print(f"❌ Failed to generate scenario without difficulty: {response.status_code}")
        print(f"   Error: {response.text}")

if __name__ == "__main__":
    print("🚀 Starting difficulty feature tests...\n")
    
    try:
        # Test 1: Ottenere i livelli di difficoltà
        test_difficulty_levels()
        
        # Test 2: Generare scenari con diversi livelli
        test_scenario_generation_with_difficulty()
        
        # Test 3: Verificare il default
        test_scenario_generation_without_difficulty()
        
        print("\n🎉 All tests completed!")
        
    except requests.exceptions.ConnectionError:
        print("❌ Cannot connect to API. Make sure the server is running on port 8000")
    except Exception as e:
        print(f"❌ Unexpected error: {e}")
