class ResponseAnalyzer:

    @staticmethod
    def analyze(response):

        status = response.status_code

        if status == 200:
            return "SUCCESS"

        elif status == 401:
            return "INVALID"

        elif status == 403:
            return "FORBIDDEN"

        elif status == 400:
            return "BAD_REQUEST"

        elif status == 404:
            return "NOT_FOUND"

        elif status == 423:
            return "LOCKED"

        elif status == 429:
            return "RATE_LIMIT"

        elif status >= 500:
            return "SERVER_ERROR"

        return f"UNKNOWN_{status}"