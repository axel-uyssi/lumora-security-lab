import time
import requests

from config import TIMEOUT, HEADERS

class RequestEngine:

    def __init__(self):
        self.session = requests.Session()

    def login_attempt(self, url, task):

        payload = {
            "email": task.email,
            "password": task.password
        }

        start = time.time()

        response = self.session.post(
            url,
            json=payload,
            headers=HEADERS,
            timeout=TIMEOUT
        )

        elapsed = time.time() - start

        return response, elapsed