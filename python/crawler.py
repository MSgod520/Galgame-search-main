import requests, json, os, hashlib

class TouchGalCrawler:
    def __init__(self, cache_dir="cache"):
        self.base_url = "https://www.touchgal.us/api"
        self.cache_dir = cache_dir
        os.makedirs(cache_dir, exist_ok=True)
        self.session = requests.Session()

    def search_game(self, keyword):
        payload = {
            "queryString": json.dumps([{"type": "keyword", "name": keyword}]),
            "limit": 15, "page": 1, "selectedType": "all",
            "searchOption": {"searchInIntroduction": True, "searchInAlias": True, "searchInTag": True},
            "selectedLanguage": "all", "selectedPlatform": "all", "sortField": "resource_update_time", "sortOrder": "desc",
            "selectedYears": [], "selectedMonths": []
        }
        cookies = {"kun-patch-setting-store|state|data|kunNsfwEnable": "all"}
        resp = self.session.post(f"{self.base_url}/search", json=payload, cookies=cookies)
        data = resp.json()
        if not isinstance(data, dict):
            raise Exception(f"API Error: {data}")
        return data.get("galgames", [])

    def get_downloads(self, patch_id):
        resp = self.session.get(f"{self.base_url}/patch/resource", params={"patchId": patch_id})
        return resp.json()