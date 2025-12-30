import requests
import json

def inspect_banner(keyword="yuzusoft"):
    url = "https://www.touchgal.us/api/search"
    payload = {
        "queryString": json.dumps([{"type": "keyword", "name": keyword}]),
        "limit": 1, "page": 1, "selectedType": "all",
        "searchOption": {"searchInIntroduction": True, "searchInAlias": True, "searchInTag": True},
        "selectedLanguage": "all", "selectedPlatform": "all", "sortField": "resource_update_time", "sortOrder": "desc",
        "selectedYears": [], "selectedMonths": []
    }
    cookies = {"kun-patch-setting-store|state|data|kunNsfwEnable": "all"}
    
    try:
        resp = requests.post(url, json=payload, cookies=cookies)
        data = resp.json()
        games = data.get("galgames", [])
        if games:
            print(f"Banner value: {games[0].get('banner')}")
    except Exception as e:
        print(f"Error: {e}")

if __name__ == "__main__":
    inspect_banner()
