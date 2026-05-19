class Metrics:

    def __init__(self):

        self.total = 0
        self.success = 0
        self.failed = 0
        self.locked = 0
        self.rate_limited = 0

    def update(self, status):

        self.total += 1

        if status == "SUCCESS":
            self.success += 1

        elif status == "INVALID":
            self.failed += 1

        elif status == "LOCKED":
            self.locked += 1

        elif status == "RATE_LIMIT":
            self.rate_limited += 1